terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.36"
    }
  }

  required_version = ">= 1.2.0"
}

provider "aws" {
  region  = "us-east-1"
  profile = "mtnfog"
}

data "aws_region" "current" {}
data "aws_caller_identity" "current" {}

locals {
  account_id = data.aws_caller_identity.current.account_id
}

resource "aws_iam_role" "ubi" {
  name = "ubiosisrole"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Sid    = ""
        Principal = {
          Service = "osis-pipelines.amazonaws.com"
        }
      },
    ]
  })
}

data "aws_iam_policy_document" "access_policy" {
  statement {
    effect = "Allow"

    principals {
      type        = "AWS"
      identifiers = ["${aws_iam_role.ubi.arn}"]
    }

    actions = ["es:*"]
  }

}

resource "aws_opensearch_domain" "opensearch_ubi" {

  domain_name    = "osi-ubi-domain"
  engine_version = "OpenSearch_2.17"

  cluster_config {
    instance_type = "t3.small.search"
  }

  encrypt_at_rest {
    enabled = true
  }

  domain_endpoint_options {
    enforce_https       = true
    tls_security_policy = "Policy-Min-TLS-1-2-2019-07"
  }

  node_to_node_encryption {
    enabled = true
  }

  ebs_options {
    ebs_enabled = true
    volume_size = 10
  }

  #access_policies = data.aws_iam_policy_document.access_policy.json

}

resource "aws_iam_policy" "ubi" {
  name        = "osis_role_policy"
  description = "Policy for OSIS pipeline role"
  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Action   = ["es:DescribeDomain"]
        Effect   = "Allow"
        Resource = "arn:aws:es:${data.aws_region.current.name}:${local.account_id}:domain/*"
      },
      {
        Action   = ["es:ESHttp*"]
        Effect   = "Allow"
        Resource = "arn:aws:es:${data.aws_region.current.name}:${local.account_id}:domain/osi-ubi-domain/*"
      },
      {
        Action   = ["s3:PutObject"]
        Effect   = "Allow"
        Resource = "arn:aws:s3:::${aws_s3_bucket.ubi_queries_events_bucket.id}/*"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "ubi" {
  role       = aws_iam_role.ubi.name
  policy_arn = aws_iam_policy.ubi.arn
}

resource "aws_cloudwatch_log_group" "ubi" {
  name              = "/aws/vendedlogs/OpenSearchIngestion/ubi-pipeline"
  retention_in_days = 14
  tags = {
    Name = "UBI OSIS Pipeline Log Group"
  }
}

resource "aws_s3_bucket" "ubi_queries_events_bucket" {
  bucket = "ubi-queries-events-sink"
}

resource "aws_osis_pipeline" "ubi_events_pipeline" {
  pipeline_name               = "ubi-pipeline"
  pipeline_configuration_body = <<-EOT
    version: "2"
    ubi-pipeline:
      source:
        http:
          path: "/ubi"
      processor:
        - date:
            from_time_received: true
            destination: "@timestamp"
      route:
        - ubi-events: '/type == "event"'
        - ubi-queries: '/type == "query"'
      sink:
        - opensearch:
            hosts: ["https://${aws_opensearch_domain.opensearch_ubi.endpoint}"]
            index: "ubi_events"
            aws:
              sts_role_arn: "${aws_iam_role.ubi.arn}"   
              region: "${data.aws_region.current.name}"
            routes: [ubi-events]
            template_type: index-template
            template_content: >
              {
                "template" : {
                  "mappings" : {
                    "properties": {
                      "application": { "type": "keyword", "ignore_above": 256 },
                      "action_name": { "type": "keyword", "ignore_above": 100 },
                      "client_id": { "type": "keyword", "ignore_above": 100 },
                      "query_id": { "type": "keyword", "ignore_above": 100 },
                      "message": { "type": "keyword", "ignore_above": 1024 },
                      "message_type": { "type": "keyword", "ignore_above": 100 },
                      "user_query":  { "type": "keyword" },
                      "timestamp": {
                        "type": "date",
                        "format":"strict_date_time",
                        "ignore_malformed": true, 
                        "doc_values": true
                      },
                      "event_attributes": {
                        "dynamic": true,
                        "properties": {
                          "position": {
                            "properties": {
                              "ordinal": { "type": "integer" },
                              "x": { "type": "integer" },
                              "y": { "type": "integer" },
                              "page_depth": { "type": "integer" },
                              "scroll_depth": { "type": "integer" },
                              "trail": { "type": "text",
                                "fields": { "keyword": { "type": "keyword", "ignore_above": 256 }
                                }
                              }
                            }
                          },
                          "object": {
                            "properties": {
                              "internal_id": { "type": "keyword" },
                              "object_id": { "type": "keyword", "ignore_above": 256 },
                              "object_id_field": { "type": "keyword", "ignore_above": 100 },
                              "name": { "type": "keyword", "ignore_above": 256 },
                              "description": { "type": "text",
                                "fields": { "keyword": { "type": "keyword", "ignore_above": 256 } }
                              },
                              "object_detail": { "type": "object" }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
        - s3:
            aws:
              sts_role_arn: "${aws_iam_role.ubi.arn}"
              region: "${data.aws_region.current.name}"
            bucket: "${aws_s3_bucket.ubi_queries_events_bucket.id}"
            object_key:
              path_prefix: ubi_events/
            threshold:
              event_collect_timeout: "60s"
            codec:
              ndjson:
            routes: [ubi-events]
        - opensearch:
            hosts: ["https://${aws_opensearch_domain.opensearch_ubi.endpoint}"]
            index: "ubi_queries"
            aws:
              sts_role_arn: "${aws_iam_role.ubi.arn}"   
              region: "${data.aws_region.current.name}"
            routes: [ubi-queries]
            template_type: index-template
            template_content: >
              {
                "template" : {
                  "mappings" : {
                    "properties": {
                      "timestamp": { "type": "date", "format": "strict_date_time" },
                      "query_id": { "type": "keyword", "ignore_above": 100 },
                      "query": { "type": "text" },
                      "query_response_id": { "type": "keyword", "ignore_above": 100 },
                      "query_response_hit_ids": { "type": "keyword" },
                      "user_query":  { "type": "keyword" },
                      "query_attributes": { "type": "flat_object" },
                      "client_id": { "type": "keyword", "ignore_above": 100 },
                      "application":  { "type":  "keyword", "ignore_above": 100 }
                    }
                  }
                }
              }
        - s3:
            aws:
              sts_role_arn: "${aws_iam_role.ubi.arn}"
              region: "${data.aws_region.current.name}"
            bucket: "${aws_s3_bucket.ubi_queries_events_bucket.id}"
            object_key:
              path_prefix: ubi_queries/
            threshold:
              event_collect_timeout: "60s"
            codec:
              ndjson:
            routes: [ubi-queries]

        EOT
  max_units                   = 1
  min_units                   = 1
  log_publishing_options {
    is_logging_enabled = true
    cloudwatch_log_destination {
      log_group = aws_cloudwatch_log_group.ubi.name
    }
  }
  tags = {
    Name = "UBI OpenSearch Ingestion Pipeline for UBI"
  }
}

output "opensearch_domain_endpoint" {
  value = aws_opensearch_domain.opensearch_ubi.endpoint
}

output "opensearch_ingest_pipeline_endpoint" {
  value = aws_osis_pipeline.ubi_events_pipeline.ingest_endpoint_urls
}

output "ingest_endpoint_url" {
  value = tolist(aws_osis_pipeline.ubi_events_pipeline.ingest_endpoint_urls)[0]
}
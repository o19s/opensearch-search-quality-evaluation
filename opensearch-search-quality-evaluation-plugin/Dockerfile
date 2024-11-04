FROM opensearchproject/opensearch:2.17.1

RUN /usr/share/opensearch/bin/opensearch-plugin install --batch https://github.com/opensearch-project/user-behavior-insights/releases/download/2.17.1.0/opensearch-ubi-2.17.1.0.zip

ADD ./build/distributions/search-quality-evaluation-plugin-2.17.1.0.zip  /tmp/search-quality-evaluation-plugin.zip
RUN /usr/share/opensearch/bin/opensearch-plugin install --batch file:/tmp/search-quality-evaluation-plugin.zip

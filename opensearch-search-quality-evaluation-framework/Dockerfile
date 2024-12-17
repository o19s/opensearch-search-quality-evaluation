FROM opensearchproject/opensearch:2.18.0

RUN /usr/share/opensearch/bin/opensearch-plugin install --batch https://github.com/opensearch-project/user-behavior-insights/releases/download/2.18.0.2/opensearch-ubi-2.18.0.2.zip

ADD ./build/distributions/search-quality-evaluation-plugin-0.0.1.zip  /tmp/search-quality-evaluation-plugin.zip
RUN /usr/share/opensearch/bin/opensearch-plugin install --batch file:/tmp/search-quality-evaluation-plugin.zip

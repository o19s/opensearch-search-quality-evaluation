FROM opensearchproject/opensearch:2.16.0

ADD ./build/distributions/search-quality-evaluation-plugin-2.16.0.0-SNAPSHOT.zip  /tmp/search-quality-evaluation-plugin.zip

RUN /usr/share/opensearch/bin/opensearch-plugin install --batch file:/tmp/search-quality-evaluation-plugin.zip

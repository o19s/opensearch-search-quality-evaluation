FROM opensearchproject/opensearch:2.17.1

ADD ./build/distributions/search-quality-evaluation-plugin-2.17.1.0-SNAPSHOT.zip  /tmp/search-quality-evaluation-plugin.zip
RUN /usr/share/opensearch/bin/opensearch-plugin install --batch file:/tmp/search-quality-evaluation-plugin.zip

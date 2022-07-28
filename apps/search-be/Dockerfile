FROM openjdk:11
EXPOSE 9898
ENV ELASTICSEARCH_HOST=192.168.1.24
ENV ELASTICSEARCH_PORT=9200
ENV GRPC_PORT=9898


RUN  mkdir -p /app
WORKDIR /app
COPY target/es_search_be-1.0.jar ./es_search_be-1.0.jar
CMD ["java","-jar","/app/es_search_be-1.0.jar","--es.host=${ELASTICSEARCH_HOST}","--es.port=${ELASTICSEARCH_PORT}","--grpc.server.port=${GRPC_PORT}"]
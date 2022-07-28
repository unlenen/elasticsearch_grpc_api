# Unlenen Elasticsearch GRPC Server and CLI

System has 3 main components. 
___

## GRPC Proto

### Architecture

Java Maven based protoc project to generate Java sources from 
proto file

##### Folder
- apps/search/proto

##### Proto File
- src/proto/search.proto

### Requirements
- Java 11
- Maven 3.8
- Internet connection for downloading maven packages

### Compile
```
 mvn install
```
___ 

## GRPC supported Search Backend

### Architecture
- Backend is Spring Boot application which provides GRPC Server for Frontend application.
- Written in Java 11
- Supports ttl cache
- EQUAL , NOT EQUAL , INCLUDE , REGEX support
- Multiple statement support
- Container friendly architecture

#### Code Base
##### GRPC Server
- unlenen.es.search.be.controller.SearchServiceGrpc
##### Elasticsearch Service 
- unlenen.es.search.be.service.ESSearchService
##### Cache Service
- unlenen.es.search.be.service.CachedSearchService
##### Entities
- unlenen.es.search.be.entity
##### JUnit Tests
- unlenen.es.search.be.entity.QueryTest

### Requirements
- Java 11
- Maven 3.8
- Internet connection for downloading maven packages
- ElasticSearch 7.17
- GRPC Proto jar

### Compile
```
 mvn install
```
### Run
```
java -jar target/es_search_be-1.0.jar --grpc.server.port=9898
```
### Docker Compile
```
docker build . -t unlenen/es_search_be:1.0
```

### Docker Run
```
docker run --name es_search_be -d -p 9898:9898 -e ELASTICSEARCH_HOST=192.168.1.24 unlenen/es_search_be:1.0
```

### Testing
```
mvn test
```

___

## GRPC supported Search Frontend

### Architecture
- Frontend is a classic CLI application which uses GRPC for connecting to Search Backend

#### Code Base
##### GRPC Client
- unlenen.es.search.fe.service.SearchService
##### Main View
- unlenen.es.search.fe.ui.MainView

### Requirements
- Java 11
- Maven 3.8
- Internet connection for downloading maven packages
- GRPC Proto jar

### Compile
```
 mvn install
```

### Run
```
java -jar target/es_search_fe-1.0.jar --grpc.client.unlenenEsSearchBe.address=static://localhost:9898 --search.ttl=10000
```
___

## Testing

### Installing Docker
```
cd scripts/docker
./install.sh
```

### Starting ElasticSearch Container
```
cd scripts/elasticsearch
./install.sh
```

### Script Configurations
```
cd test/scripts
cat ./base.sh
```

### Creating Index
```
cd test/scripts
./createIndex.sh
```

### Upload Mapping for Index
```
cd test/scripts
./uploadMappings.sh
```

### Upload Mock Doc Data
```
cd test/scripts
./uploadDoc.sh
```

### GRPC Server Test
```
cd test/grpc
./testGrpc.sh region EQUAL earth
```

### GRPC Server Load Test
```
cd test/grpc
./loadTest.sh 1000
```

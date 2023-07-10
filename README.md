# Kathena


Experiments around Spring Boot and Spring Data Reactive Repos

```bash
docker run -d --name elasticsearch -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" -e "xpack.security.enabled=false" elasticsearch:8.4.2
```

```bash
docker run -d --rm -p 27017-27019:27017-27019 --name mongodb mongo
```

```bash
docker run --rm --name zookeeper -p 2181:2181 -e ALLOW_ANONYMOUS_LOGIN=yes -d wurstmeister/zookeeper:latest
``` 

```bash
docker run --name kafka --rm -p 9092:9092 -e ADVERTISED_HOST=localhost -e KAFKA_ZOOKEEPER_CONNECT=host.docker.internal:2181 -e KAFKA_ADVERTISED_LISTENERS=OUTSIDE://host.docker.internal:9093,INSIDE://localhost:9092 -e KAFKA_LISTENERS=OUTSIDE://:9093,INSIDE://:9092 -e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=OUTSIDE:PLAINTEXT,INSIDE:PLAINTEXT -e KAFKA_INTER_BROKER_LISTENER_NAME=INSIDE -d wurstmeister/kafka:latest
```

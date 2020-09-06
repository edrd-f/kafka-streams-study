# Kafka Streams Study

## Setup

```sh
cp .env.sample .env # Change advertised hostname
docker-compose up
docker-compose exec kafka /opt/kafka/bin/kafka-topics.sh --create --topic service-metrics --bootstrap-server localhost:9092
./gradlew run
```

## Resetting stream states

```sh
docker-compose exec kafka /opt/kafka/bin/kafka-streams-application-reset.sh --application-id metrics-dashboard --input-topics service-metrics --bootstrap-servers localhost:9092 --zookeeper localhost:2181
```

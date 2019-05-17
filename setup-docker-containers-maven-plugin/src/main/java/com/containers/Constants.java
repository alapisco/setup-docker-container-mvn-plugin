package com.containers;


public final class Constants {

    public static final String TIMEOUT_PLACEHOLDER = "${TIMEOUT}";
    public static final String RETRY_INTERVAL_PLACEHOLDER = "${RETRY_INTERVAL}";
    public static final String DOCKER_BUILD_DIR_PLACEHOLDER = "${DOCKER_BUILD_DIR}";
    public static final String SERVICES_TO_RUN_PLACE_HOLDER = "${SERVICES_TO_RUN}";
    public static final String SERVICES_TO_RUN_WITH_PORT_PLACE_HOLDER = "${SERVICES_TO_RUN_WITH_PORT}";
    public static final String NETWORK_NAME_PLACEHOLDER = "${NETWORK_NAME}";
    public static final String MONGODB_INIT_FLAGS = "${MONGO_INIT_FLAGS}";


    public static final String BASE_DIRECTORY = "target/containers-maven-plugin-files";
    public static final String START_SERVICES_DOCKER_IMAGE_DIR = "start-services-image";

    public static final String CASSANDRA_SERVICE_NAME = "cassandra.test";
    public static final String ZOOKEEPER_SERVICE_NAME = "zookeeper.test";
    public static final String KAFKA_SERVICE_NAME = "kafka.test";
    public static final String SCHEMA_REGISTRY_SERVICE_NAME = "schema-registry.test";
    public static final String ELASTICSEARCH_SERVICE_NAME = "elasticsearch.test";
    public static final String MONGODB_SERVICE_NAME = "mongodb.test";

    public static final String CASSANDRA_SERVICE_PORT = "9042";
    public static final String ZOOKEEPER_SERVICE_PORT = "2181";
    public static final String KAFKA_SERVICE_PORT = "39092";
    public static final String SCHEMA_REGISTRY_SERVICE_PORT = "8051";
    public static final String ELASTICSEARCH_SERVICE_PORT = "9200";
    public static final String MONGODB_SERVICE_PORT = "27017";

    public static final String MAVEN_DEV_PROFILE_ID = "dev";

}
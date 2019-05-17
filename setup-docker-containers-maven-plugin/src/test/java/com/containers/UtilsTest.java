package com.containers;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.junit.Assert.assertEquals;

public class UtilsTest {

    int dockerServicesAvailabilityTimeoutMinutes = 10;
    int servicesRetryIntervalSeconds = 5;
    String networkName = "containers-net";
    String mongoInitFlags = "--bind_ip mongodb.test --port 27017";

    @Test
    public void generateComposerFileContent() {

        boolean dockerizeCassandra = true;
        boolean dockerizeZookeper = true;
        boolean dockerizeKafka = true;
        boolean dockerizeSchemaRegistry = true;
        boolean dockerizeElasticsearch = true;
        boolean dockerizeMongoDB = true;

        try {
            String composerFileContent = Utils.getDockerComposeFileContent(dockerizeCassandra, dockerizeZookeper, dockerizeKafka,
                dockerizeSchemaRegistry, dockerizeElasticsearch, dockerizeMongoDB, dockerServicesAvailabilityTimeoutMinutes,
                servicesRetryIntervalSeconds, networkName, mongoInitFlags);

            Path resourceDirectory = Paths.get("src/test/resources/docker-compose-parsed.yml");
            String expectedFileContent = IOUtils.toString(resourceDirectory.toUri(), StandardCharsets.UTF_8);

            assertThat(composerFileContent).isEqualTo(expectedFileContent);

        } catch (IOException e) {
            fail("Error while accessing yml file: "+e.getMessage());
        }

    }

    @Test
    public void getAllServicesToRun() {

        boolean dockerizeCassandra = true;
        boolean dockerizeZookeper = true;
        boolean dockerizeKafka = true;
        boolean dockerizeSchemaRegistry = true;
        boolean dockerizeElasticsearch = true;
        boolean dockerizeMongoDB = true;
        boolean includePort = true;

        String[] servicesToRun = Utils.getServicesToRun(dockerizeCassandra, dockerizeZookeper, dockerizeKafka, dockerizeSchemaRegistry,
            dockerizeElasticsearch, dockerizeMongoDB, includePort);
        assertThat(servicesToRun[0]).isEqualTo("cassandra.test:9042");
        assertThat(servicesToRun[1]).isEqualTo("zookeeper.test:2181");
        assertThat(servicesToRun[2]).isEqualTo("kafka.test:39092");
        assertThat(servicesToRun[3]).isEqualTo("schema-registry.test:8051");
        assertThat(servicesToRun[4]).isEqualTo("elasticsearch.test:9200");
        assertThat(servicesToRun[5]).isEqualTo("mongodb.test:27017");

        includePort = false;
        servicesToRun = Utils.getServicesToRun(dockerizeCassandra, dockerizeZookeper, dockerizeKafka, dockerizeSchemaRegistry,
            dockerizeElasticsearch, dockerizeMongoDB, includePort);
        assertThat(servicesToRun[0]).isEqualTo("cassandra.test");
        assertThat(servicesToRun[1]).isEqualTo("zookeeper.test");
        assertThat(servicesToRun[2]).isEqualTo("kafka.test");
        assertThat(servicesToRun[3]).isEqualTo("schema-registry.test");
        assertThat(servicesToRun[4]).isEqualTo("elasticsearch.test");
        assertThat(servicesToRun[5]).isEqualTo("mongodb.test");

    }


    @Test
    public void getSomeServicesToRun() {

        boolean dockerizeCassandra = true;
        boolean dockerizeZookeper = true;
        boolean dockerizeMongoDB = true;
        boolean dockerizeKafka = false;
        boolean dockerizeSchemaRegistry = false;
        boolean dockerizeElasticsearch = false;
        boolean includePort = true;

        String[] servicesToRun = Utils.getServicesToRun(dockerizeCassandra, dockerizeZookeper, dockerizeKafka, dockerizeSchemaRegistry,
            dockerizeElasticsearch, dockerizeMongoDB, includePort);
        assertThat(servicesToRun[0]).isEqualTo("cassandra.test:9042");
        assertThat(servicesToRun[1]).isEqualTo("zookeeper.test:2181");
        assertThat(servicesToRun[2]).isEqualTo("mongodb.test:27017");

        includePort = false;
        servicesToRun = Utils.getServicesToRun(dockerizeCassandra, dockerizeZookeper, dockerizeKafka, dockerizeSchemaRegistry,
            dockerizeElasticsearch, dockerizeMongoDB, includePort);
        assertThat(servicesToRun[0]).isEqualTo("cassandra.test");
        assertThat(servicesToRun[1]).isEqualTo("zookeeper.test");
        assertThat(servicesToRun[2]).isEqualTo("mongodb.test");

    }

    @Test
    public void getNoServicesToRun() {

        boolean dockerizeCassandra = false;
        boolean dockerizeZookeper = false;
        boolean dockerizeKafka = false;
        boolean dockerizeSchemaRegistry = false;
        boolean dockerizeElasticsearch = false;
        boolean dockerizeMongoDB = false;
        boolean includePort = true;

        String[] servicesToRun = Utils.getServicesToRun(dockerizeCassandra, dockerizeZookeper, dockerizeKafka, dockerizeSchemaRegistry,
            dockerizeElasticsearch, dockerizeMongoDB, includePort);
        assertThat(servicesToRun).isEmpty();

        includePort = false;
        servicesToRun = Utils.getServicesToRun(dockerizeCassandra, dockerizeZookeper, dockerizeKafka, dockerizeSchemaRegistry,
            dockerizeElasticsearch, dockerizeMongoDB, includePort);
        assertThat(servicesToRun).isEmpty();

    }

    @Test
    public void writePropertiesToFile() throws IOException {

        File temp = new File("temp.properties");
        Utils.writeGeneralPropertiesToFile(temp);

        List<String> properties = Files.readAllLines(temp.toPath());

        assertEquals(properties.get(0), "cassandra.container.address=cassandra.test");
        assertEquals(properties.get(1), "schema-registry.container.address=schema-registry.test");
        assertEquals(properties.get(2), "zookeeper.container.address=zookeeper.test");
        assertEquals(properties.get(3), "kafka.container.address=kafka.test");
        assertEquals(properties.get(4), "elasticsearch.container.address=elasticsearch.test");
        assertEquals(properties.get(5), "mongodb.container.address=mongodb.test");
        assertEquals(properties.get(6), "schema-registry.port=8051");
        assertEquals(properties.get(7), "cassandra.port=9042");
        assertEquals(properties.get(8), "zookeeper.port=2181");
        assertEquals(properties.get(9), "kafka.port=39092");
        assertEquals(properties.get(10), "elasticsearch.http.port=9200");
        assertEquals(properties.get(11), "elasticsearch.tcp.port=9300");
        assertEquals(properties.get(12), "mongodb.port=27017");

        temp.delete();

    }

    @Test
    public void writeDevProfilePropertiesToFile() throws IOException {

        File temp = new File("temp.dev.properties");
        Utils.writeDevProfilePropertiesToFile(temp);

        List<String> properties = Files.readAllLines(temp.toPath());

        assertEquals(properties.get(0), "docker.skip=false");
        assertEquals(properties.get(1), "createDependencyReducedPomForBuild=false");

        temp.delete();

    }


}

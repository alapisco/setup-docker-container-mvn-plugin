package com.containers;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class Utils {

    private static Process ongoingProcess;

    public static void destroyOngoingProcess(){
        if(ongoingProcess!=null){
            ongoingProcess.destroy();
        }
    }

    public static boolean  executeCommand(String command, File directory) throws IOException, InterruptedException{
        return executeCommand(command, directory, null);
    }

    public static boolean  executeCommand(String command, File directory, Integer timeoutMinutes) throws IOException, InterruptedException{

        ProcessBuilder dockerProcessBuilder = new ProcessBuilder("bash", "-c", command);
        dockerProcessBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        dockerProcessBuilder.redirectErrorStream(true);
        dockerProcessBuilder.directory(directory);
        Process dockerProcess = dockerProcessBuilder.start();
        ongoingProcess = dockerProcess;
        if(timeoutMinutes==null){
            return dockerProcess.waitFor()==0?true:false;
        }else{
            return dockerProcess.waitFor(timeoutMinutes, TimeUnit.MINUTES);
        }

    }

    public static String getServicesToRunString(Boolean dockerizeCassandra, Boolean dockerizeZookeper, Boolean dockerizeKafka,
        Boolean dockerizeSchemaRegistry, Boolean dockerizeElasticsearch, Boolean dockerizeMongoDB, boolean includePort){
        StringBuilder servicesSb = new StringBuilder();
        String[] services = getServicesToRun(dockerizeCassandra, dockerizeZookeper, dockerizeKafka, dockerizeSchemaRegistry,
            dockerizeElasticsearch, dockerizeMongoDB, includePort);

        for (String service : services) {
            servicesSb.append(service + " ");
        }

        return servicesSb.toString().trim();
    }

    public static String[] getServicesToRun(Boolean dockerizeCassandra, Boolean dockerizeZookeper, Boolean dockerizeKafka,
        Boolean dockerizeSchemaRegistry, Boolean dockerizeElasticsearch, Boolean dockerizeMongoDB, boolean includePort){
        ArrayList<String> servicesToRun = new ArrayList<>();

        if(dockerizeCassandra!=null && dockerizeCassandra){
            StringBuilder casandraServiceName = new StringBuilder(Constants.CASSANDRA_SERVICE_NAME);
            if(includePort){
                casandraServiceName.append(":");
                casandraServiceName.append(Constants.CASSANDRA_SERVICE_PORT);
            }
            servicesToRun.add(casandraServiceName.toString());
        }

        if(dockerizeZookeper!=null && dockerizeZookeper){
            StringBuilder zookeeperServiceName = new StringBuilder(Constants.ZOOKEEPER_SERVICE_NAME);
            if(includePort){
                zookeeperServiceName.append(":");
                zookeeperServiceName.append(Constants.ZOOKEEPER_SERVICE_PORT);
            }
            servicesToRun.add(zookeeperServiceName.toString());
        }

        if(dockerizeKafka!=null && dockerizeKafka){
            StringBuilder kafkaServiceName = new StringBuilder(Constants.KAFKA_SERVICE_NAME);
            if(includePort){
                kafkaServiceName.append(":");
                kafkaServiceName.append(Constants.KAFKA_SERVICE_PORT);
            }
            servicesToRun.add(kafkaServiceName.toString());
        }

        if(dockerizeSchemaRegistry!=null && dockerizeSchemaRegistry){
            StringBuilder schemaRegistryServiceName = new StringBuilder(Constants.SCHEMA_REGISTRY_SERVICE_NAME);
            if(includePort){
                schemaRegistryServiceName.append(":");
                schemaRegistryServiceName.append(Constants.SCHEMA_REGISTRY_SERVICE_PORT);
            }
            servicesToRun.add(schemaRegistryServiceName.toString());
        }

        if(dockerizeElasticsearch!=null && dockerizeElasticsearch){
            StringBuilder elasticsearchServiceName = new StringBuilder(Constants.ELASTICSEARCH_SERVICE_NAME);
            if(includePort){
                elasticsearchServiceName.append(":");
                elasticsearchServiceName.append(Constants.ELASTICSEARCH_SERVICE_PORT);
            }
            servicesToRun.add(elasticsearchServiceName.toString());
        }

        if(dockerizeMongoDB!=null && dockerizeMongoDB){
            StringBuilder mongoDbServiceName = new StringBuilder(Constants.MONGODB_SERVICE_NAME);
            if(includePort){
                mongoDbServiceName.append(":");
                mongoDbServiceName.append(Constants.MONGODB_SERVICE_PORT);
            }
            servicesToRun.add(mongoDbServiceName.toString());
        }

        return servicesToRun.toArray(new String[0]);
    }

    public static String getDockerComposeFileContent(Boolean dockerizeCassandra , Boolean dockerizeZookeper, Boolean dockerizeKafka,
        Boolean dockerizeSchemaRegistry, Boolean dockerizeElasticsearch, Boolean dockerizeMongoDB, int dockerServicesAvailabilityTimeoutMinutes,
        int servicesRetryIntervalSeconds, String networkName, String mongoInitFlags) throws IOException, PatternSyntaxException {

        String dockerComposeFileContent = IOUtils.toString(Utils.class.getResourceAsStream("docker-compose.yml"), StandardCharsets.UTF_8);

        String buildDirectory = "./" + Constants.START_SERVICES_DOCKER_IMAGE_DIR;

        dockerComposeFileContent = dockerComposeFileContent.replaceAll(Pattern.quote(Constants.DOCKER_BUILD_DIR_PLACEHOLDER), buildDirectory);

        String servicesToRun = getDependsOnSectionContent(dockerizeCassandra, dockerizeZookeper, dockerizeKafka, dockerizeSchemaRegistry,
            dockerizeElasticsearch, dockerizeMongoDB);
        dockerComposeFileContent = dockerComposeFileContent.replaceAll(Pattern.quote(Constants.SERVICES_TO_RUN_PLACE_HOLDER), servicesToRun);

        dockerComposeFileContent = dockerComposeFileContent.replaceAll(Pattern.quote(Constants.TIMEOUT_PLACEHOLDER),
            Integer.toString(dockerServicesAvailabilityTimeoutMinutes*60));

        dockerComposeFileContent = dockerComposeFileContent.replaceAll(Pattern.quote(Constants.RETRY_INTERVAL_PLACEHOLDER),
            Integer.toString(servicesRetryIntervalSeconds));

        dockerComposeFileContent = dockerComposeFileContent.replaceAll(Pattern.quote(Constants.NETWORK_NAME_PLACEHOLDER),networkName);

        String[] servicesToRunWithPort = getServicesToRun(dockerizeCassandra, dockerizeZookeper, dockerizeKafka,
            dockerizeSchemaRegistry, dockerizeElasticsearch, dockerizeMongoDB, true);
        StringBuilder servicesToRunWithPortStr = new StringBuilder();
        for (String service : servicesToRunWithPort) {
            servicesToRunWithPortStr.append(service);
            servicesToRunWithPortStr.append(" ");
        }
        dockerComposeFileContent = dockerComposeFileContent.replaceAll(Pattern.quote(Constants.SERVICES_TO_RUN_WITH_PORT_PLACE_HOLDER),
            servicesToRunWithPortStr.toString().trim());

        dockerComposeFileContent = dockerComposeFileContent.replaceAll(Pattern.quote(Constants.MONGODB_INIT_FLAGS), mongoInitFlags);

        return dockerComposeFileContent;
    }


    private static String getDependsOnSectionContent(Boolean dockerizeCassandra, Boolean dockerizeZookeper, Boolean dockerizeKafka,
        Boolean dockerizeSchemaRegistry, Boolean dockerizeElasticsearch, Boolean dockerizeMongoDB){

        String[] servicesToRun = getServicesToRun(dockerizeCassandra, dockerizeZookeper, dockerizeKafka, dockerizeSchemaRegistry, dockerizeElasticsearch, dockerizeMongoDB, false);

        if(servicesToRun.length==0){
            return "";
        }

        StringBuilder dependsOnSectionContent = new StringBuilder("");
        dependsOnSectionContent.append(System.lineSeparator());

        for (String service : servicesToRun ) {
            String serviceName = service.split(":")[0];
            dependsOnSectionContent.append("        - ").append(serviceName).append(System.lineSeparator());
        }

        dependsOnSectionContent.deleteCharAt(dependsOnSectionContent.length()-1);

        return dependsOnSectionContent.toString();
    }

    public static void writeGeneralPropertiesToFile(File propertiesFile) throws IOException{
        writeStringToFile(propertiesFile,"cassandra.container.address=cassandra.test");
        writeStringToFile(propertiesFile,"schema-registry.container.address=schema-registry.test");
        writeStringToFile(propertiesFile,"zookeeper.container.address=zookeeper.test");
        writeStringToFile(propertiesFile,"kafka.container.address=kafka.test");
        writeStringToFile(propertiesFile,"elasticsearch.container.address=elasticsearch.test");
        writeStringToFile(propertiesFile,"mongodb.container.address=mongodb.test");
        writeStringToFile(propertiesFile,"schema-registry.port=8051");
        writeStringToFile(propertiesFile,"cassandra.port=9042");
        writeStringToFile(propertiesFile,"zookeeper.port=2181");
        writeStringToFile(propertiesFile,"kafka.port=39092");
        writeStringToFile(propertiesFile,"elasticsearch.http.port=9200");
        writeStringToFile(propertiesFile,"elasticsearch.tcp.port=9300");
        writeStringToFile(propertiesFile,"mongodb.port=27017");
    }

    public static void writeDevProfilePropertiesToFile(File propertiesFile) throws IOException{
        writeStringToFile(propertiesFile,"docker.skip=false");
        writeStringToFile(propertiesFile,"createDependencyReducedPomForBuild=false");
    }

    private static void writeStringToFile(File file, String str) throws IOException{
        FileUtils.writeStringToFile(file, str+"\n", StandardCharsets.UTF_8,true);
    }

}





package com.containers;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Mojo(name = "run-containers")
@Slf4j
public class StartDockerServices extends AbstractMojo {

    Logger log = LoggerFactory.getLogger(StartDockerServices.class);


    @Parameter(readonly = true, defaultValue = "${project}")
    private MavenProject project;

    @Parameter(defaultValue = "containers-net")
    private String networkName;

    @Parameter(defaultValue = "false")
    private Boolean dockerizeElasticsearch;

    @Parameter(defaultValue = "false")
    private Boolean dockerizeCassandra;

    @Parameter(defaultValue = "false")
    private Boolean dockerizeZookeper;

    @Parameter(defaultValue = "false")
    private Boolean dockerizeKafka;

    @Parameter(defaultValue = "10")
    private Boolean dockerizeSchemaRegistry;

    @Parameter(defaultValue = "false")
    private Boolean dockerizeMongoDB;

    @Parameter(defaultValue = "60")
    private Integer dockerPullTimeoutMinutes;


    public void execute() throws MojoExecutionException {



        String servicesToRun = Utils.getServicesToRunString(dockerizeCassandra, dockerizeZookeper, dockerizeKafka,
            dockerizeSchemaRegistry, dockerizeElasticsearch, dockerizeMongoDB, false);

        if ( servicesToRun.isEmpty() ) {
            log.info("No containers configured to run . . .");
        }else if ( dockerizeKafka && ( dockerizeSchemaRegistry == null || !dockerizeSchemaRegistry ) ){
            log.warn("Kafka container will be started without Schema Registry. It might cause problems with serialization.");
        }
        else {
            try {

                File executionDirectory = new File(project.getBasedir(),Constants.BASE_DIRECTORY);

                log.info("Pulling container(s): " + servicesToRun );
                log.info("Docker pull timeout set to : " + dockerPullTimeoutMinutes + " minutes" );
                String dockerPullCommand = "docker-compose pull  " + servicesToRun;

                boolean finishedOnTime = Utils.executeCommand(dockerPullCommand, executionDirectory,  dockerPullTimeoutMinutes);

                if(!finishedOnTime){
                    Utils.destroyOngoingProcess();
                    throw new MojoExecutionException(dockerPullTimeoutMinutes + " minutes threshold for docker-compose pull reached.");
                }else{
                    log.info("Containers pulled");
                    log.info("Creating Network : " + networkName );
                    String createNetworkCommand = "docker network create " + networkName;
                    Utils.executeCommand(createNetworkCommand, executionDirectory);

                    log.info("Starting container(s): " + servicesToRun );
                    String dockerComposeRunCommand =  "docker-compose run --rm start.services.test";
                    boolean successfulExecution = Utils.executeCommand(dockerComposeRunCommand, executionDirectory);

                    if(successfulExecution){
                        log.info("Containers ready");
                    }
                    else {
                        throw new MojoExecutionException("Error while executing " + dockerComposeRunCommand);
                    }

                }

            } catch (IOException | InterruptedException e) {
                throw new MojoExecutionException("Problem while running docker services", e);
            }
        }
    }
}

package com.containers;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Mojo(name = "stop-containers")
@Slf4j
public class StopDockerContainers extends AbstractMojo {

    Logger log = LoggerFactory.getLogger(StopDockerContainers.class);


    @Parameter(readonly = true, defaultValue = "${project}")
    private MavenProject project;

    @Parameter(defaultValue = "containers-net")
    private String networkName;

    @Parameter(defaultValue = "false")
    private Boolean dockerizeCassandra;

    @Parameter(defaultValue = "false")
    private Boolean dockerizeElasticsearch;

    @Parameter(defaultValue = "false")
    private Boolean dockerizeZookeper;

    @Parameter(defaultValue = "false")
    private Boolean dockerizeKafka;

    @Parameter(defaultValue = "false")
    private Boolean dockerizeSchemaRegistry;

    @Parameter(defaultValue = "false")
    private Boolean dockerizeMongoDB;

    public void execute() throws MojoExecutionException {
        try {

            File executionDirectory = new File(project.getBasedir(),Constants.BASE_DIRECTORY);

            log.info("Stopping containers...");
            String stopContainersCommand = "docker rm -f " +
                Utils.getServicesToRunString(dockerizeCassandra, dockerizeZookeper, dockerizeKafka, dockerizeSchemaRegistry,
                    dockerizeElasticsearch, dockerizeMongoDB, false);
            Utils.executeCommand(stopContainersCommand, executionDirectory);

            log.info("Removing network " + networkName);
            String removeNetworkCommand = "docker network  rm " + networkName;
            Utils.executeCommand(removeNetworkCommand, executionDirectory);
        } catch ( Exception e) {
            throw new MojoExecutionException("Problem stopping docker containers", e);
        }
    }
}

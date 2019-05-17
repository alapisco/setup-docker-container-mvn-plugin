package com.containers;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.model.Profile;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Mojo(name = "setup")
@Slf4j
public class GenerateDockerSetupFiles extends AbstractMojo {

    Logger log = LoggerFactory.getLogger(GenerateDockerSetupFiles.class);


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

    @Parameter(defaultValue = "false")
    private Boolean dockerizeSchemaRegistry;

    @Parameter(defaultValue = "false")
    private Boolean dockerizeMongoDB;

    @Parameter(defaultValue = "10")
    private Integer dockerServicesAvailabilityTimeoutMinutes;

    @Parameter(defaultValue = "5")
    private Integer servicesRetryIntervalSeconds;

    @Parameter(defaultValue = "--bind_ip " + Constants.MONGODB_SERVICE_NAME + " --port " + Constants.MONGODB_SERVICE_PORT)
    private String mongoDbInitFlags;

    public void execute() throws MojoExecutionException {


        File baseDirectory = new File(project.getBasedir(),Constants.BASE_DIRECTORY);
        File dockerBuildDirectory  = new File(baseDirectory, Constants.START_SERVICES_DOCKER_IMAGE_DIR);

        File xapiItContainersMavenPluginPropertiesFile = new File(baseDirectory,"containers-maven-plugin.properties");
        File dockerComposeFile = new File(baseDirectory,"docker-compose.yml");
        File dockerFile = new File(dockerBuildDirectory, "Dockerfile");
        File entrypointFile=new File(dockerBuildDirectory, "entrypoint.sh");

        try {

            // delete setup files left by other executions, if any
            Files.deleteIfExists(xapiItContainersMavenPluginPropertiesFile.toPath());
            Files.deleteIfExists(dockerComposeFile.toPath());
            Files.deleteIfExists(dockerFile.toPath());
            Files.deleteIfExists(entrypointFile.toPath());
            Files.deleteIfExists(dockerBuildDirectory.toPath());
            Files.deleteIfExists(baseDirectory.toPath());

            dockerBuildDirectory.mkdirs();

            Utils.writeGeneralPropertiesToFile(xapiItContainersMavenPluginPropertiesFile);

            List<Profile> profiles =  project.getActiveProfiles();
            for (Profile profile: profiles) {
                if(profile.getId().equals(Constants.MAVEN_DEV_PROFILE_ID)){
                    log.info("Generating properties for profile id: " + profile.getId());
                    Utils.writeDevProfilePropertiesToFile(xapiItContainersMavenPluginPropertiesFile);
                }
            }

            String dockerComposeFileContent = Utils.getDockerComposeFileContent(dockerizeCassandra , dockerizeZookeper, dockerizeKafka,
                dockerizeSchemaRegistry,dockerizeElasticsearch, dockerizeMongoDB, dockerServicesAvailabilityTimeoutMinutes, servicesRetryIntervalSeconds, networkName, mongoDbInitFlags);
            FileUtils.writeStringToFile(dockerComposeFile, dockerComposeFileContent, StandardCharsets.UTF_8);

            String dockerFileContent = IOUtils.toString(Utils.class.getResourceAsStream("Dockerfile"), StandardCharsets.UTF_8);
            FileUtils.writeStringToFile(dockerFile, dockerFileContent, StandardCharsets.UTF_8);

            String entrypointFileContent = IOUtils.toString(Utils.class.getResourceAsStream("entrypoint.sh"), StandardCharsets.UTF_8);
            FileUtils.writeStringToFile(entrypointFile, entrypointFileContent, StandardCharsets.UTF_8);
            Set<PosixFilePermission> permissions = new HashSet<>();
            permissions.add(PosixFilePermission.OWNER_READ);
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
            Files.setPosixFilePermissions(Paths.get(entrypointFile.getAbsolutePath()),permissions);
            
        } catch (IOException  | PatternSyntaxException e) {
            throw new MojoExecutionException("Problem generating docker setup files",e);
        }
        finally {
            xapiItContainersMavenPluginPropertiesFile.deleteOnExit();
            dockerComposeFile.deleteOnExit();
            dockerFile.deleteOnExit();
            entrypointFile.deleteOnExit();
            dockerBuildDirectory.deleteOnExit();
        }
    }
}

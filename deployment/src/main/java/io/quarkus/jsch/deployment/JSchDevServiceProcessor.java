package io.quarkus.jsch.deployment;

import java.util.Map;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.dev.devservices.GlobalDevServicesConfig;

@BuildSteps(onlyIfNot = IsNormal.class, onlyIf = GlobalDevServicesConfig.Enabled.class)
public class JSchDevServiceProcessor {

    @BuildStep
    DevServicesResultBuildItem startDevServices(JSchDevServiceConfig config) {
        if (!config.enabled()) {
            return null;
        }

        DockerImageName dockerImageName = DockerImageName.parse(config.image());
        GenericContainer container = new GenericContainer<>(dockerImageName)
                .withEnv("USER_NAME", config.username())
                .withEnv("USER_PASSWORD", config.password())
                .withEnv("PASSWORD_ACCESS", "true")
                .withExposedPorts(config.port())
                .withReuse(config.reuse());

        container.start();

        Map<String, String> configOverrides = Map.of(
                "quarkus.jsch.session.host", container.getHost(),
                "quarkus.jsch.session.port", container.getMappedPort(config.port()).toString(),
                "quarkus.jsch.session.username", config.username(),
                "quarkus.jsch.session.password", config.password(),
                "quarkus.jsch.session.config.StrictHostKeyChecking", "no");

        return new DevServicesResultBuildItem.RunningDevService(JSchProcessor.FEATURE, container.getContainerId(),
                container::close, configOverrides)
                .toBuildItem();
    }
}

package io.quarkus.jsch.runtime;

import java.util.Map;

import io.quarkus.jsch.JSchSession;
import io.quarkus.runtime.annotations.ConfigDocMapKey;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefaults;
import io.smallrye.config.WithParentName;
import io.smallrye.config.WithUnnamedKey;

@ConfigMapping(prefix = "quarkus.jsch.session")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface JSchSessionsRuntimeConfig {

    /**
     * JSch sessions.
     */
    @ConfigDocMapKey("session-name")
    @WithParentName
    @WithDefaults
    @WithUnnamedKey(JSchSession.DEFAULT_SESSION_NAME)
    Map<String, JSchSessionRuntimeConfig> sessions();

}

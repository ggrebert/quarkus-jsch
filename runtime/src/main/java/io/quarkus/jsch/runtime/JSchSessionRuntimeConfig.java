package io.quarkus.jsch.runtime;

import java.util.Map;
import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigDocMapKey;
import io.quarkus.runtime.annotations.ConfigGroup;
import io.smallrye.config.WithDefault;

@ConfigGroup
public interface JSchSessionRuntimeConfig {

    /**
     * The host to use for the JSch session.
     */
    @WithDefault("localhost")
    public String host();

    /**
     * The port to use for the JSch session.
     */
    @WithDefault("22")
    public int port();

    /**
     * The username to use for the JSch session.
     */
    public Optional<String> username();

    /**
     * The password to use for the JSch session.
     */
    public Optional<String> password();

    /**
     * The private key to use for the JSch session.
     */
    public Optional<String> key();

    /**
     * The passphrase to use for the JSch session.
     */
    public Optional<String> passphrase();

    /**
     * The configuration to use for the JSch session.
     */
    @ConfigDocMapKey("config-name")
    public Map<String, String> config();

    /**
     * Mock the JSch session in dev and test mode.
     */
    @WithDefault("true")
    public boolean mock();

}

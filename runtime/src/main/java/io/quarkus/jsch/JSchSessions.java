package io.quarkus.jsch;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.annotation.PreDestroy;

import org.eclipse.microprofile.config.ConfigProvider;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.Slf4jLogger;

import io.quarkus.arc.Arc;
import io.quarkus.jsch.runtime.JSchSessionRuntimeConfig;
import io.quarkus.jsch.runtime.JSchSessionsRuntimeConfig;
import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.configuration.ConfigurationException;
import io.smallrye.config.SmallRyeConfig;

public class JSchSessions {

    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

    public JSchSessions() {
        JSch.setLogger(new Slf4jLogger());
    }

    /**
     * Get a session by name
     *
     * @param sessionName the session name
     * @return the session
     */
    public static Session fromName(String sessionName) {
        return Arc.container().instance(JSchSessions.class).get().getSession(sessionName);
    }

    /**
     * Disconnect all sessions
     */
    public static void shutdown() {
        Arc.container().instance(JSchSessions.class).get().disconnect();
    }

    /**
     * Get or create a session by name
     *
     * @param sessionName the session name
     * @return the session
     */
    public Session getSession(String sessionName) {
        return sessions.computeIfAbsent(sessionName, this::createSession);
    }

    /**
     * Disconnect all sessions
     */
    @PreDestroy
    public void disconnect() {
        sessions.values().forEach(Session::disconnect);
    }

    private Session createSession(String sessionName) {
        try {
            JSchSessionRuntimeConfig config = getConfig(sessionName);
            JSch jsch = new JSch();
            String baseProperty = this.getBaseProperty(sessionName);
            Session session = jsch.getSession(config.username().orElse(null), config.host(), config.port());

            config.password().ifPresent(session::setPassword);

            config.key().ifPresent(k -> {
                try {
                    if (config.passphrase().isPresent()) {
                        jsch.addIdentity(k, config.passphrase().get());
                    } else {
                        jsch.addIdentity(k);
                    }
                } catch (Exception e) {
                    throw new ConfigurationException("Failed to add identity", e, Set.of(
                            baseProperty + "key",
                            baseProperty + "passphrase"));
                }
            });

            config.config().forEach(session::setConfig);
            session.connect();

            return session;
        } catch (JSchException e) {
            throw new ConfigurationException("Failed to create session: " + sessionName, e);
        }
    }

    private String getBaseProperty(String sessionName) {
        String baseProperty = "quarkus.jsch.sessions.";
        if (!sessionName.equals(JSchSession.DEFAULT_SESSION_NAME)) {
            baseProperty += sessionName + ".";
        }

        return baseProperty;
    }

    private JSchSessionRuntimeConfig getConfig(String sessionName) {
        JSchSessionsRuntimeConfig configs = ConfigProvider.getConfig().unwrap(SmallRyeConfig.class)
                .getConfigMapping(JSchSessionsRuntimeConfig.class);

        if (isDevOrTest() && !JSchSession.DEFAULT_SESSION_NAME.equals(sessionName)) {
            // return the default session except if the mock flag is set to false
            JSchSessionRuntimeConfig c = configs.sessions().get(sessionName);
            if (c == null || c.mock()) {
                return configs.sessions().get(JSchSession.DEFAULT_SESSION_NAME);
            }

            return c;
        }
        return configs.sessions().computeIfAbsent(sessionName, n -> {
            throw new ConfigurationException("Session not found: " + sessionName);
        });
    }

    private boolean isDevOrTest() {
        return LaunchMode.current().isDevOrTest()
                || "test".equals(ConfigProvider.getConfig().getValue("quarkus.profile", String.class));
    }
}

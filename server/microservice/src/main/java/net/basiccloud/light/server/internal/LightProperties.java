package net.basiccloud.light.server.internal;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * LightProperties
 */
@SuppressWarnings("unused")
@ConfigurationProperties(prefix = "light", locations = "classpath:light.properties")
class LightProperties {

    private int port;
    private int sslPort;
    private String registry;
    private boolean skipRegistry;
    private boolean developModel;

    public boolean isSkipRegistry() {
        return skipRegistry;
    }

    public void setSkipRegistry(boolean skipRegistry) {
        this.skipRegistry = skipRegistry;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getSslPort() {
        return sslPort;
    }

    public void setSslPort(int sslPort) {
        this.sslPort = sslPort;
    }

    public String getRegistry() {
        return registry;
    }

    public void setRegistry(String registry) {
        this.registry = registry;
    }

    public boolean isDevelopModel() {
        return developModel;
    }

    public void setDevelopModel(boolean developModel) {
        this.developModel = developModel;
    }
}

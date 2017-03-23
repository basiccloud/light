package net.basiccloud.light.server.core.internal;

/**
 * proto service metadata
 */
public class ServiceMetadata {

    private final String group;
    private final String name;
    private final String version;
    private final boolean enableSsl;

    private ServiceMetadata(String group, String name, String version, boolean enableSsl) {
        this.group = group;
        this.name = name;
        this.version = version;
        this.enableSsl = enableSsl;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getGroup() {
        return group;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public boolean isEnableSsl() {
        return enableSsl;
    }

    @Override
    public String toString() {
        return "ServiceMetadata{" +
                "group='" + group + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", enableSsl='" + enableSsl + '\'' +
                '}';
    }

    public static final class Builder {
        private String group;
        private String name;
        private String version;
        private boolean enableSsl;

        private Builder() {
        }

        public Builder withGroup(String group) {
            this.group = group;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withVersion(String version) {
            this.version = version;
            return this;
        }

        public Builder withEnableSsl(boolean enableSsl) {
            this.enableSsl = enableSsl;
            return this;
        }

        public ServiceMetadata build() {
            return new ServiceMetadata(group, name, version, enableSsl);
        }
    }
}

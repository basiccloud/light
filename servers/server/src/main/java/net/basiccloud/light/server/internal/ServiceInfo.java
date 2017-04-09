package net.basiccloud.light.server.internal;

import io.grpc.ServerServiceDefinition;

import net.basiccloud.light.server.core.internal.ServiceMetadata;

import org.springframework.stereotype.Service;

/**
 * Light service Metadata info
 */
@Service
class ServiceInfo {

    private Object springBean;
    private Class<?> annotationType;
    private Class<?> protoClass;
    private ServiceMetadata serviceMetadata;
    private ServerServiceDefinition serverServiceDefinition;

    private ServiceInfo() {

    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Object getSpringBean() {
        return springBean;
    }

    public Class<?> getProtoClass() {
        return protoClass;
    }

    public ServiceMetadata getServiceMetadata() {
        return serviceMetadata;
    }

    public ServerServiceDefinition getServerServiceDefinition() {
        return serverServiceDefinition;
    }

    public Class<?> getAnnotationType() {
        return annotationType;
    }

    public static final class Builder {
        private Object springBean;
        private Class<?> annotationType;
        private Class<?> protoClass;
        private ServiceMetadata serviceMetadata;
        private ServerServiceDefinition serverServiceDefinition;

        private Builder() {
        }

        public static Builder aServiceInfo() {
            return new Builder();
        }

        public Builder withSpringBean(Object springBean) {
            this.springBean = springBean;
            return this;
        }

        public Builder withAnnotationType(Class<?> annotationType) {
            this.annotationType = annotationType;
            return this;
        }

        public Builder withProtoClass(Class<?> protoClass) {
            this.protoClass = protoClass;
            return this;
        }

        public Builder withServiceMetadata(ServiceMetadata serviceMetadata) {
            this.serviceMetadata = serviceMetadata;
            return this;
        }

        public Builder withServerServiceDefinition(ServerServiceDefinition serverServiceDefinition) {
            this.serverServiceDefinition = serverServiceDefinition;
            return this;
        }

        public ServiceInfo build() {
            ServiceInfo serviceInfo = new ServiceInfo();
            serviceInfo.protoClass = this.protoClass;
            serviceInfo.serverServiceDefinition = this.serverServiceDefinition;
            serviceInfo.serviceMetadata = this.serviceMetadata;
            serviceInfo.springBean = this.springBean;
            serviceInfo.annotationType = this.annotationType;
            return serviceInfo;
        }
    }
}

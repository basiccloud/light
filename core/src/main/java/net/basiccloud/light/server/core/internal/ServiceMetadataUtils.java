package net.basiccloud.light.server.core.internal;

import com.google.protobuf.Descriptors;


import java.util.List;

import light.Descriptor;


/**
 * ServiceMetadataUtils.
 */
public class ServiceMetadataUtils {

    public static ServiceMetadata getServiceMetadata(Class<?> protoClass) {
        Descriptors.FileDescriptor fileDescriptor;
        try {
            fileDescriptor = ProtoUtil.getFileDescriptor(protoClass);
        } catch (Exception e) {
            String message = "proto class is failed ,curr protoClass =" + protoClass.getName();
            throw new LightRuntimeException(message, e);
        }

        List<Descriptors.ServiceDescriptor> services = ProtoUtil.getServiceName(fileDescriptor);
        if (services.size() != 1) {
            throw new LightRuntimeException("proto file is failed");
        }
        String serviceName = services.get(0).getName();
        String version = (String) ProtoUtil
                .getServiceOption(fileDescriptor, Descriptor.version.getDescriptor(), serviceName);
        String group =
                (String) ProtoUtil.getServiceOption(fileDescriptor, Descriptor.group.getDescriptor(), serviceName);
        boolean enableSsl = (boolean) ProtoUtil
                .getServiceOption(fileDescriptor, Descriptor.enableSsl.getDescriptor(), serviceName);
        return ServiceMetadata.newBuilder().withEnableSsl(enableSsl)
                .withGroup(group).withName(serviceName).withVersion(version).build();
    }
}

package net.basiccloud.light.server.internal;

import com.google.common.collect.Lists;

import net.basiccloud.light.server.LightService;
import net.basiccloud.light.server.core.internal.LightRuntimeException;
import net.basiccloud.light.server.core.internal.ServiceMetadata;
import net.basiccloud.light.server.core.internal.ServiceMetadataUtils;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;

/**
 * scan spring context,find all class has @LightService annotation
 */
@Component
class ServiceScan {

    @Autowired
    private ApplicationContext applicationContext;

    private List<ServiceInfo> allService = Lists.newArrayList();

    void init() {
        Collection<Object> serviceBeans = applicationContext.getBeansWithAnnotation(LightService.class)
                .values();
        Collection<Object> buildInServiceBeans = applicationContext.getBeansWithAnnotation(LightBuildInService.class)
                .values();
        validate(serviceBeans.size());

        //TODO andy refactor this code
        serviceBeans.stream().findFirst().ifPresent(springBean -> {
            Class<?> targetClass = AopUtils.getTargetClass(springBean);
            LightService annotation = AnnotationUtils.findAnnotation(targetClass, LightService.class);
            ServiceMetadata serviceMetadata = ServiceMetadataUtils.getServiceMetadata(annotation.protoClass());
            ServerServiceDefinition serverServiceDefinition = ((BindableService) springBean).bindService();

            ServiceInfo serviceInfo = ServiceInfo.newBuilder().withAnnotationType(LightService.class).withProtoClass(
                    annotation.protoClass()).withServerServiceDefinition(serverServiceDefinition).withServiceMetadata(
                    serviceMetadata).withSpringBean(springBean).build();
            allService.add(serviceInfo);
        });

        buildInServiceBeans.forEach(springBean -> {
            Class<?> targetClass = AopUtils.getTargetClass(springBean);
            LightBuildInService annotation = AnnotationUtils.findAnnotation(targetClass, LightBuildInService.class);
            ServiceMetadata serviceMetadata = ServiceMetadataUtils.getServiceMetadata(annotation.protoClass());
            ServerServiceDefinition serverServiceDefinition = ((BindableService) springBean).bindService();

            ServiceInfo serviceInfo = ServiceInfo.newBuilder().withAnnotationType(
                    LightBuildInService.class).withProtoClass(
                    annotation.protoClass()).withServerServiceDefinition(serverServiceDefinition).withServiceMetadata(
                    serviceMetadata).withSpringBean(springBean).build();
            allService.add(serviceInfo);
        });
    }

    List<ServiceInfo> getAllService() {
        return allService;
    }

    private void validate(int countOfService) {
        if (countOfService == 0) {
            throw new LightRuntimeException("no bean with @GrpcGateway or @GrpcService found, at least provide one!");
        }

        if (countOfService > 1) {
            throw new RuntimeException(
                    "only allow one @GrpcService exist in a GRPC server, now there are " + countOfService);
        }
    }
}

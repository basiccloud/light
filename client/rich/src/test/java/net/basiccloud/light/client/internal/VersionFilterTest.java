package net.basiccloud.light.client.internal;

import net.basiccloud.registry.ServiceInstance;
import net.basiccloud.registry.ServiceInstanceData;
import net.basiccloud.registry.ServiceInstanceStatus;
import net.basiccloud.registry.Version;

import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class VersionFilterTest {
    @Test
    public void filter() throws Exception {
        Version version = Version.valueOf("0.0.1");
        ServiceInstance serviceInstance = ServiceInstance.newBuilder()
                .setGroup("group")
                .setIp("ip")
                .setPort(80)
                .setService("service")
                .setData(ServiceInstanceData.newBuilder()
                        .setServiceVersion("0.0.1")
                        .setFrameworkVersion("0.0.1")
                        .addGrpcPort(80).addHttpPort(88)
                        .build())
                .setStatus(ServiceInstanceStatus.newBuilder().asOnline().build())
                .build();
        List<ServiceInstance> serviceStatusList = Lists.newArrayList();
        serviceStatusList.add(serviceInstance);

        VersionFilter.filter(version, serviceStatusList, Priority.defaultPriorities(1));
    }

    @Test
    public void filter_fail() throws Exception {
        Version version = Version.valueOf("0.0.1");
        ServiceInstance serviceInstance = ServiceInstance.newBuilder()
                .setGroup("group")
                .setIp("ip")
                .setPort(80)
                .setService("service")
                .setData(ServiceInstanceData.newBuilder()
                        .setServiceVersion("1.0.1")
                        .setFrameworkVersion("0.0.1")
                        .addGrpcPort(80).addHttpPort(88)
                        .build())
                .setStatus(ServiceInstanceStatus.newBuilder().asOnline().build())
                .build();
        List<ServiceInstance> serviceStatusList = Lists.newArrayList();
        serviceStatusList.add(serviceInstance);

        Throwable throwable = Assertions.catchThrowable(
                () -> VersionFilter.filter(version, serviceStatusList, Priority.defaultPriorities(1)));
        assertThat(throwable).isInstanceOf(RuntimeException.class).hasMessageContaining(
                "version filter not found match");
    }

    @Test
    public void filter_fail2() throws Exception {
        Version version = Version.valueOf("0.0.1");
        ServiceInstance serviceInstance = ServiceInstance.newBuilder()
                .setGroup("group")
                .setIp("ip")
                .setPort(80)
                .setService("service")
                .setData(ServiceInstanceData.newBuilder()
                        .setServiceVersion("1.0.1")
                        .setFrameworkVersion("0.0.1")
                        .addGrpcPort(80).addHttpPort(88)
                        .build())
                .setStatus(ServiceInstanceStatus.newBuilder().asOnline().build())
                .build();
        List<ServiceInstance> serviceStatusList = Lists.newArrayList();
        serviceStatusList.add(serviceInstance);

        Throwable throwable = Assertions.catchThrowable(
                () -> VersionFilter.filter(version, serviceStatusList, Priority.defaultPriorities(2)));
        assertThat(throwable).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(
                        "serviceStatusList size must be equal priorities length, but now serviceStatusList size");
    }
}
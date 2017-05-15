package net.basiccloud.light.server.internal;

import com.google.common.collect.Lists;

import net.basiccloud.light.server.LightService;
import net.basiccloud.light.server.utils.IpUtil;
import net.basiccloud.registry.RegisterId;
import net.basiccloud.registry.ServiceInstance;
import net.basiccloud.registry.ServiceInstanceData;
import net.basiccloud.registry.ServiceInstanceStatus;
import net.basiccloud.registry.ServiceRegistryServer;
import net.basiccloud.registry.Version;
import net.basiccloud.registry.WorkMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import io.grpc.Server;
import io.grpc.internal.ServerImpl;
import io.grpc.netty.NettyServerBuilder;

/**
 * this class will scan @LightService and publish service
 */
@Component
class LightStarter implements CommandLineRunner, DisposableBean {


    private static Logger logger = LoggerFactory.getLogger(LightStarter.class);

    @Autowired(required = false)
    private ServiceRegistryServer<RegisterId> serviceRegistryServer;
    @Autowired
    private ServiceScan serviceScan;
    @Autowired
    private LightProperties lightProperties;

    private static List<RegisterId> registerIds = Lists.newArrayList();
    private static List<Server> servers = Lists.newArrayList();

    @Override
    public void run(String... strings) throws Exception {
        logger.info("start run");
        serviceScan.init();

        if (lightProperties.isSkipRegistry()) {
            logger.warn("**** registry is skipped: light.skipRegistry is set to true ****");
        }

        List<ServiceInfo> allService = serviceScan.getAllService();
        NettyServerBuilder nettyServerBuilder = NettyServerBuilder.forPort(lightProperties.getPort());
        allService.forEach(serviceInfo -> {
            nettyServerBuilder.addService(serviceInfo.getServerServiceDefinition());
            if (serviceInfo.getAnnotationType() == LightService.class) {
                RegisterId registry = registry(serviceInfo.getServiceMetadata().getGroup(),
                        serviceInfo.getServiceMetadata().getName(), serviceInfo.getServiceMetadata().getVersion());
                registerIds.add(registry);
            }
        });
        ServerImpl server = nettyServerBuilder.build();
        server.start();
        servers.add(server);
        logger.info("Success to start Light Server on port " + lightProperties.getPort());
        server.awaitTermination();
    }

    private RegisterId registry(String group, String name, String version) {
        ServiceInstanceData serviceInstanceData = ServiceInstanceData.newBuilder().setServiceType
                (ServiceInstanceData.LIGHT_SERVICE_TYPE).setServiceVersion(Version.valueOf(version))
                .setFrameworkVersion("0.1.0").setWorkMode(WorkMode.NORMAL).build();

        ServiceInstanceStatus serviceInstanceStatus = ServiceInstanceStatus.newBuilder().setStatus(
                ServiceInstanceStatus.Status.ONLINE).setComment("registry service").setLastUpdateTime(System
                .currentTimeMillis()).build();

        Optional<String> ipFromSocket = IpUtil.getIpFromSocket();
        if (ipFromSocket.isPresent() & serviceRegistryServer != null) {
            ServiceInstance serviceInstance = ServiceInstance.newBuilder().setData(serviceInstanceData).setGroup(
                    group).setService(name).setIp(ipFromSocket.get()).setPort(lightProperties.getPort()).setStatus
                    (serviceInstanceStatus)
                    .build();
            return serviceRegistryServer.register(serviceInstance);
        }
        throw new RuntimeException("can not get local ip");
    }

    @Override
    public void destroy() throws Exception {
        servers.forEach(Server::shutdown);
        registerIds.forEach(registerId -> serviceRegistryServer.deregister(registerId));
    }

    private String getFrameworkVersion() {
        Properties properties = new Properties();
        try {
            InputStream resourceAsStream = LightStarter.class.getResourceAsStream(
                    "/META-INF/maven/net.basiccloud.light/light-server/pom.properties");
            properties.load(resourceAsStream);
            return properties.getProperty("version");
        } catch (IOException e) {
            throw new RuntimeException("can not find framework version", e);
        }
    }
}

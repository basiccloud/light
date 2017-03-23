package net.basiccloud.light.server.internal;

import com.google.common.collect.Maps;

import net.basiccloud.etcd.registry.ServiceRegistryFactoryEtcdImpl;
import net.basiccloud.etcd.registry.internal.ServiceRegistryConnectionEtcdImpl;
import net.basiccloud.registry.RegisterId;
import net.basiccloud.registry.ServiceRegistryConnection;
import net.basiccloud.registry.ServiceRegistryServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

/**
 * light configuration
 */
@Configuration
@ComponentScan(value = "net.basiccloud.light.*")
@EnableConfigurationProperties(LightProperties.class)
public class LightConfiguration {

    @SuppressWarnings("unchecked")
    @Bean
    @Conditional(RegistrySkippedConditional.class)
    public ServiceRegistryServer<RegisterId> serviceRegistryServer(LightProperties lightProperties) {
        HashMap<String, String> params = Maps.newHashMap();
        params.put("registryUrl", lightProperties.getRegistry());
        ServiceRegistryConnection connection = ServiceRegistryConnectionEtcdImpl.buildFromParameters(params);
        connection.connect();
        return new ServiceRegistryFactoryEtcdImpl().getServer(connection);
    }

    public static class RegistrySkippedConditional implements Condition {
        private Logger logger = LoggerFactory.getLogger(RegistrySkippedConditional.class);

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            Properties properties = new Properties();
            InputStream inputStream = null;
            try {
                File file = context.getResourceLoader().getResource("light.properties").getFile();
                inputStream = new FileInputStream(file);
                properties.load(inputStream);
            } catch (IOException e) {
                logger.error("fail to find light properties in class path");
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        logger.warn("fail to close inputStream", e);
                    }
                }
            }
            String skipRegistry = properties.getProperty("light.skipRegistry");
            logger.debug("skip registry is {}", skipRegistry);
            return skipRegistry.equals("false");
        }
    }
}

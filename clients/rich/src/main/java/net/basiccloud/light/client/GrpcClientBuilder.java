package net.basiccloud.light.client;

import com.google.common.base.Strings;
import io.grpc.Channel;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.util.RoundRobinLoadBalancerFactory;
import net.basiccloud.light.client.internal.AbstractClientBuilder;
import net.basiccloud.light.client.internal.LightNameResolverProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GrpcClientBuilder<T> extends AbstractClientBuilder<GrpcClientBuilder<T>, T> {

    private static Logger logger = LoggerFactory.getLogger(GrpcClientBuilder.class);

    private GrpcClientBuilder(Class<T> clientClass, boolean sync) {
        super(clientClass, sync);
        registry = System.getenv("LIGHT_REGISTRY");
    }

    @Override
    protected void prepareChannel() {
        originalChannel = createChannel();
    }

    @SuppressWarnings("unchecked")
    public static <T> GrpcClientBuilder<T> sync(Class<T> clientClass) {
        return new GrpcClientBuilder(clientClass, true);
    }

    private Channel createChannel() {
        NettyChannelBuilder nettyChannelBuilder;
        if (Strings.isNullOrEmpty(directAddress)) {
            String target = LightNameResolverProvider.SCHEME + "://" + serviceMetadata.getGroup() + "." +
                    serviceMetadata.getName();
            nettyChannelBuilder = NettyChannelBuilder.forTarget(target).loadBalancerFactory(
                    RoundRobinLoadBalancerFactory.getInstance());
        } else {
            logger.warn("**** use direct Address {} to access service {}, skip registry ****", directAddress,
                    serviceMetadata.getName());
            nettyChannelBuilder = NettyChannelBuilder.forAddress(getSocketAddressFromDirectAddress());
        }

        nettyChannelBuilder.usePlaintext(true);
        return nettyChannelBuilder.build();
    }
}

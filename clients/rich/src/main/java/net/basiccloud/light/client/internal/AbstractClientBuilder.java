package net.basiccloud.light.client.internal;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.basiccloud.etcd.registry.internal.ServiceRegistryClientEtcdImpl;
import net.basiccloud.etcd.registry.internal.ServiceRegistryConnectionEtcdImpl;
import net.basiccloud.light.server.core.internal.LightRuntimeException;
import net.basiccloud.light.server.core.internal.ServiceMetadata;
import net.basiccloud.light.server.core.internal.ServiceMetadataUtils;
import net.basiccloud.registry.ServiceRegistryClient;
import net.basiccloud.registry.ServiceRegistryConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;

import io.grpc.Channel;

@SuppressWarnings({"unused", "unchecked"})
public abstract class AbstractClientBuilder<Builder extends AbstractClientBuilder, T> {

    private static Logger logger = LoggerFactory.getLogger(AbstractClientBuilder.class);

    private final boolean sync;

    private final Class<T> clientClass;
    private Class protoClass;
    private Class grpcOutClass;

    static List<ServiceMetadata> serviceMetadataList = Lists.newArrayList();

    protected ServiceMetadata serviceMetadata;
    protected String registry;
    protected String directAddress;

    protected Channel originalChannel;

    static ServiceRegistryClient serviceRegistryClient;

    protected AbstractClientBuilder(Class<T> clientClass, boolean sync) {
        this.clientClass = clientClass;
        this.sync = sync;

        this.directAddress = System.getenv("LIGHT_DIRECT_ADDRESS");
    }


    public final Class<T> getClientClass() {
        return this.clientClass;
    }


    public Builder protoClass(Class<?> protoClass) {
        Preconditions.checkArgument(protoClass != null, "proto class can not must be null");
        this.protoClass = protoClass;
        return (Builder) this;
    }

    public Builder useDirectAddress(String directAddress) {
        this.directAddress = directAddress;
        return (Builder) this;
    }

    public Builder useRegistry(String registry) {
        this.registry = registry;
        return (Builder) this;
    }

    public T build() {
        //create registry client
        if (Strings.isNullOrEmpty(directAddress)) {
            Map<String, String> map = Maps.newHashMap();
            map.put("registryUrl", registry);
            ServiceRegistryConnection connection = ServiceRegistryConnectionEtcdImpl.buildFromParameters(map);
            connection.connect();
            serviceRegistryClient = new ServiceRegistryClientEtcdImpl(connection);
        }

        scanMetadata();

        Method newStubMethod = getNewStubMethod();
        prepareChannel();

        return createClientStub(newStubMethod, originalChannel);
    }


    private void scanMetadata() {
        grpcOutClass = getGrpcOutClass(clientClass);
        serviceMetadata = ServiceMetadataUtils.getServiceMetadata(protoClass);

        serviceMetadataList.add(serviceMetadata);
    }

    protected abstract void prepareChannel();

    private Class getGrpcOutClass(Class<?> clazz) {
        String grpcOutClassName = ClassNameUtil.getGrpcOutClassName(clazz.getName());
        try {
            return Class.forName(grpcOutClassName);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("can't found grpc out class named " + grpcOutClassName, e);
        }
    }

    private Method getNewStubMethod() {
        final String stubType = this.sync ? "newBlockingStub" : "newFeatureStub";
        try {
            return grpcOutClass.getMethod(stubType, Channel.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(
                    "can't find " + stubType + " method, please check if protoClass is set correctly");
        }
    }

    private T createClientStub(Method newStubMethod, Channel channel) {
        try {
            return (T) newStubMethod.invoke(grpcOutClass, channel);
        } catch (Exception e) {
            logger.error("fail to create client stub:", e);
            throw new IllegalStateException("fail to build", e);
        }
    }

    protected InetSocketAddress getSocketAddressFromDirectAddress() {
        Preconditions.checkNotNull(directAddress);
        String[] split = directAddress.split(":");
        if (split.length < 2) {
            throw new LightRuntimeException("wrong direct Address" +
                    "current " + "address =" + directAddress);
        }
        return InetSocketAddress.createUnresolved(split[0], Integer.parseInt(split[1]));
    }
}

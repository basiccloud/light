package net.basiccloud.light.client.internal;

import com.google.common.collect.Lists;

import net.basiccloud.light.server.core.internal.ServiceMetadata;
import net.basiccloud.registry.ServiceInstance;
import net.basiccloud.registry.Version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import javax.annotation.concurrent.GuardedBy;

import io.grpc.Attributes;
import io.grpc.NameResolver;
import io.grpc.ResolvedServerInfo;
import io.grpc.ResolvedServerInfoGroup;
import io.grpc.Status;
import io.grpc.internal.GrpcUtil;
import io.grpc.internal.SharedResourceHolder;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static net.basiccloud.light.client.internal.AbstractClientBuilder.serviceRegistryClient;

class LightNameResolver extends NameResolver {

    private static Logger logger = LoggerFactory.getLogger(LightNameResolver.class);

    private final String authority;
    private final SharedResourceHolder.Resource<ExecutorService> executorResource;
    private final String group;
    private final String service;


    @GuardedBy("this")
    private boolean shutdown;
    @GuardedBy("this")
    private boolean resolving;
    @GuardedBy("this")
    private Listener listener;
    @GuardedBy("this")
    private ExecutorService executor;
    @GuardedBy("this")
    private ServiceMetadata serviceMetadata;


    LightNameResolver(String target) {
        String[] split = target.split("\\.");
        this.group = split[0];
        this.service = split[1];
        URI nameUri = URI.create("//" + target);
        this.authority = nameUri.getAuthority();
        this.executorResource = GrpcUtil.SHARED_CHANNEL_EXECUTOR;
        this.executor = SharedResourceHolder.get(executorResource);
        Optional<ServiceMetadata> first = AbstractClientBuilder.serviceMetadataList.stream().filter(
                serviceMetadata -> serviceMetadata.getGroup()
                        .equals(group) && serviceMetadata.getName().equals(service)).findFirst();
        if (first.isPresent()) {
            serviceMetadata = first.get();
        } else {
            throw new RuntimeException("fail to find service metadata for service " + this.group + "." + this.service);
        }
    }

    @Override
    public String getServiceAuthority() {
        return authority;
    }

    @Override
    public void refresh() {
        checkState(listener != null, "not started");
        resolve();
    }

    @Override
    public void start(Listener listener) {
        checkState(this.listener == null, "already started");
        this.listener = checkNotNull(listener, "listener");
        serviceRegistryClient.watch(group, service, Version.valueOf(serviceMetadata.getVersion()), list -> {
            logger.debug("service instance {}.{} updated", group, service);
            refresh();
        });
        resolve();
    }

    @Override
    public void shutdown() {
        if (shutdown) {
            return;
        }
        shutdown = true;
        if (executor != null) {
            executor = SharedResourceHolder.release(executorResource, executor);
        }
    }

    private void resolve() {
        if (resolving || shutdown) {
            return;
        }
        executor.execute(resolutionRunnable);
    }

    private Runnable resolutionRunnable = new Runnable() {
        @Override
        public void run() {
            Listener savedListener;
            synchronized (LightNameResolver.this) {
                if (shutdown) {
                    return;
                }
                savedListener = listener;
                resolving = true;
            }
            try {
                List<ResolvedServerInfoGroup> list;
                try {
                    list = lookupResolvedServiceInfo();
                } catch (Exception e) {
                    savedListener.onError(Status.UNAVAILABLE.withCause(e));
                    return;
                }
                savedListener.onUpdate(list, Attributes.EMPTY);
            } finally {
                synchronized (LightNameResolver.this) {
                    resolving = false;
                }
            }
        }
    };

    private List<ResolvedServerInfoGroup> lookupResolvedServiceInfo() {
        //1.look up service instance in registry
        List<ServiceInstance> availableInstanceList = serviceRegistryClient.discover(group, service);
        if (availableInstanceList.isEmpty()) {
            throw new RuntimeException(" can not find " + group + "." + service + " instance in registry");
        }
        final int[] priorities = Priority.defaultPriorities(availableInstanceList.size());
        //2.version filter
        VersionFilter.filter(Version.valueOf(serviceMetadata.getVersion()), availableInstanceList, priorities);

        List<ResolvedServerInfoGroup> list = Lists.newArrayList();
        for (int i = 0; i < availableInstanceList.size(); i++) {
            if (priorities[i] > 0) {
                ServiceInstance serviceInstance = availableInstanceList.get(i);
                int port = serviceInstance.getPort();
                InetSocketAddress inetSocketAddress = new InetSocketAddress(serviceInstance.getIp(), port);
                ResolvedServerInfo resolvedServerInfo = new ResolvedServerInfo(inetSocketAddress, Attributes.EMPTY);

                ResolvedServerInfoGroup serverInfoGroup = ResolvedServerInfoGroup.builder().add(
                        resolvedServerInfo).build();
                list.add(serverInfoGroup);
            }
        }
        return list;
    }
}

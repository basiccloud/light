package net.basiccloud.light.client.internal;


import com.google.common.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

import io.grpc.Attributes;
import io.grpc.ConnectivityStateInfo;
import io.grpc.EquivalentAddressGroup;
import io.grpc.LoadBalancer;
import io.grpc.ResolvedServerInfo;
import io.grpc.ResolvedServerInfoGroup;
import io.grpc.Status;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.grpc.ConnectivityState.IDLE;
import static io.grpc.ConnectivityState.READY;
import static io.grpc.ConnectivityState.TRANSIENT_FAILURE;

public class RandomLoadBalancerFactory extends LoadBalancer.Factory {
    private static final RandomLoadBalancerFactory INSTANCE =
            new RandomLoadBalancerFactory();

    private RandomLoadBalancerFactory() {
    }

    public static RandomLoadBalancerFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public LoadBalancer newLoadBalancer(LoadBalancer.Helper helper) {
        return new RandomLoadBalancer(helper);
    }

    static class RandomLoadBalancer extends LoadBalancer {
        private final Helper helper;
        private final Map<EquivalentAddressGroup, Subchannel> subchannels =
                new HashMap<EquivalentAddressGroup, Subchannel>();

        @VisibleForTesting
        static final Attributes.Key<AtomicReference<ConnectivityStateInfo>> STATE_INFO =
                Attributes.Key.of("state-info");

        RandomLoadBalancer(Helper helper) {
            this.helper = checkNotNull(helper, "helper");
        }

        @Override
        public void handleResolvedAddresses(List<ResolvedServerInfoGroup> servers, Attributes attributes) {
            Set<EquivalentAddressGroup> currentAddrs = subchannels.keySet();
            Set<EquivalentAddressGroup> latestAddrs =
                    resolvedServerInfoGroupToEquivalentAddressGroup(servers);
            Set<EquivalentAddressGroup> addedAddrs = setsDifference(latestAddrs, currentAddrs);
            Set<EquivalentAddressGroup> removedAddrs = setsDifference(currentAddrs, latestAddrs);

            // Create new subchannels for new addresses.
            for (EquivalentAddressGroup addressGroup : addedAddrs) {
                // NB(lukaszx0): we don't merge `attributes` with `subchannelAttr` because subchannel
                // doesn't need them. They're describing the resolved server list but we're not taking
                // any action based on this information.
                Attributes subchannelAttrs = Attributes.newBuilder()
                        // NB(lukaszx0): because attributes are immutable we can't set new value for the key
                        // after creation but since we can mutate the values we leverge that and set
                        // AtomicReference which will allow mutating state info for given channel.
                        .set(STATE_INFO, new AtomicReference<ConnectivityStateInfo>(
                                ConnectivityStateInfo.forNonError(IDLE)))
                        .build();

                Subchannel subchannel = checkNotNull(helper.createSubchannel(addressGroup, subchannelAttrs),
                        "subchannel");
                subchannels.put(addressGroup, subchannel);
                subchannel.requestConnection();
            }

            // Shutdown subchannels for removed addresses.
            for (EquivalentAddressGroup addressGroup : removedAddrs) {
                Subchannel subchannel = subchannels.remove(addressGroup);
                subchannel.shutdown();
            }

            updatePicker(getAggregatedError());
        }

        @Override
        public void handleNameResolutionError(Status error) {
            updatePicker(error);
        }


        @Override
        public void handleSubchannelState(Subchannel subchannel, ConnectivityStateInfo stateInfo) {
            if (!subchannels.containsValue(subchannel)) {
                return;
            }
            if (stateInfo.getState() == IDLE) {
                subchannel.requestConnection();
            }
            getSubchannelStateInfoRef(subchannel).set(stateInfo);
            updatePicker(getAggregatedError());
        }

        @Override
        public void shutdown() {
            for (Subchannel subchannel : getSubchannels()) {
                subchannel.shutdown();
            }
        }

        private static <T> Set<T> setsDifference(Set<T> a, Set<T> b) {
            Set<T> aCopy = new HashSet<T>(a);
            aCopy.removeAll(b);
            return aCopy;
        }

        private void updatePicker(@Nullable Status error) {
            List<Subchannel> activeList = filterNonFailingSubchannels(getSubchannels());
            helper.updatePicker(new RandomLoadBalancerFactory.Picker(activeList, error));
        }

        /**
         * Filters out non-ready subchannels.
         */
        private static List<Subchannel> filterNonFailingSubchannels(
                Collection<Subchannel> subchannels) {
            List<Subchannel> readySubchannels = new ArrayList<Subchannel>(subchannels.size());
            for (Subchannel subchannel : subchannels) {
                if (getSubchannelStateInfoRef(subchannel).get().getState() == READY) {
                    readySubchannels.add(subchannel);
                }
            }
            return readySubchannels;
        }

        private static AtomicReference<ConnectivityStateInfo> getSubchannelStateInfoRef(
                Subchannel subchannel) {
            return checkNotNull(subchannel.getAttributes().get(STATE_INFO), "STATE_INFO");
        }

        Collection<Subchannel> getSubchannels() {
            return subchannels.values();
        }

        @Nullable
        private Status getAggregatedError() {
            Status status = null;
            for (Subchannel subchannel : getSubchannels()) {
                ConnectivityStateInfo stateInfo = getSubchannelStateInfoRef(subchannel).get();
                if (stateInfo.getState() != TRANSIENT_FAILURE) {
                    return null;
                }
                status = stateInfo.getStatus();
            }
            return status;
        }
    }

    /**
     * Converts list of {@link ResolvedServerInfoGroup} to {@link EquivalentAddressGroup} set.
     */
    private static Set<EquivalentAddressGroup> resolvedServerInfoGroupToEquivalentAddressGroup(
            List<ResolvedServerInfoGroup> groupList) {
        Set<EquivalentAddressGroup> addrs = new HashSet<EquivalentAddressGroup>();
        for (ResolvedServerInfoGroup group : groupList) {
            for (ResolvedServerInfo server : group.getResolvedServerInfoList()) {
                addrs.add(new EquivalentAddressGroup(server.getAddress()));
            }
        }
        return addrs;
    }

    @VisibleForTesting
    static final class Picker extends LoadBalancer.SubchannelPicker {
        @Nullable
        private final Status status;
        private final List<LoadBalancer.Subchannel> list;
        private final int size;
        private final ThreadLocalRandom random = ThreadLocalRandom.current();

        Picker(List<LoadBalancer.Subchannel> list, @Nullable Status status) {
            this.list = Collections.unmodifiableList(list);
            this.size = list.size();
            this.status = status;
        }

        @Override
        public LoadBalancer.PickResult pickSubchannel(LoadBalancer.PickSubchannelArgs args) {
            if (size > 0) {
                return LoadBalancer.PickResult.withSubchannel(nextSubchannel());
            }

            if (status != null) {
                return LoadBalancer.PickResult.withError(status);
            }

            return LoadBalancer.PickResult.withNoResult();
        }

        private LoadBalancer.Subchannel nextSubchannel() {
            if (size == 0) {
                throw new NoSuchElementException();
            }
            return list.get(random.nextInt(size));
        }

        @VisibleForTesting
        List<LoadBalancer.Subchannel> getList() {
            return list;
        }

        @VisibleForTesting
        Status getStatus() {
            return status;
        }
    }
}

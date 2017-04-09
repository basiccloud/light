package net.basiccloud.light.client.internal;


import net.basiccloud.registry.ServiceInstance;
import net.basiccloud.registry.Version;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

class VersionFilter {

    static void filter(Version version, List<ServiceInstance> serviceStatusList, int[] priorities) {

        checkArgument(serviceStatusList.size() == priorities.length,
                "serviceStatusList size must be equal priorities length, but now serviceStatusList size = "
                        + serviceStatusList.size() + " and priorities length = " + priorities.length);
        for (int i = 0; i < serviceStatusList.size(); i++) {
            if (!serviceStatusList.get(i).getData().getServiceVersion().isBackCompatibleWith(version)) {
                priorities[i] = -10;
            }
        }

        if (!Priority.hasAvailableInstance(priorities)) {
            throw new RuntimeException(
                    "version filter not found match " + serviceStatusList.get(0).getService() + " instance");
        }
    }

}

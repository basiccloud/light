package net.basiccloud.light.client.internal;

import java.net.URI;

import javax.annotation.Nullable;

import io.grpc.Attributes;
import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Light Name Resolver Provider
 */
public class LightNameResolverProvider extends NameResolverProvider {

    public static final String SCHEME = "light";

    @Override
    protected boolean isAvailable() {
        return true;
    }

    @Override
    protected int priority() {
        return 3;
    }

    @Nullable
    @Override
    public NameResolver newNameResolver(URI targetUri, Attributes params) {
        if (SCHEME.equals(targetUri.getScheme())) {
            String targetPath = checkNotNull(targetUri.getAuthority(), "target authority must be not " + "null");
            return new LightNameResolver(targetPath);
        }
        return null;
    }

    @Override
    public String getDefaultScheme() {
        return SCHEME;
    }
}

package net.basiccloud.light.client.internal;

public class ClassNameUtil {

    private static final String GRPC_CLASS_FLAG = "Grpc";

    public static String getGrpcOutClassName(String grpcServiceName) {
        final int index = grpcServiceName.lastIndexOf(GRPC_CLASS_FLAG + "$");
        if (index == -1) {
            throw new IllegalStateException(
                    "can't getChannel grpc out class getName from grpc services getName: grpcServiceName=" + grpcServiceName);
        }

        return grpcServiceName.substring(0, index + GRPC_CLASS_FLAG.length());
    }

    public static String getDefaultServiceName(String grpcOutClassSimpleName) {
        if (!grpcOutClassSimpleName.contains(GRPC_CLASS_FLAG)) {
            throw new IllegalStateException(
                    "can't getChannel default grpc services getName from grpc out getName: grpcOutClassSimpleName=" + grpcOutClassSimpleName);
        }
        return grpcOutClassSimpleName.substring(0, grpcOutClassSimpleName.length() - GRPC_CLASS_FLAG.length());
    }
}

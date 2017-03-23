package net.basiccloud.light.server.core.internal;

import com.google.protobuf.Descriptors;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Scan Grpc Proto file Util.
 */
class ProtoUtil {

    private final static String GET_DESCRIPTOR_METHOD = "getDescriptor";

    /**
     * get option value
     *
     * @param fileDescriptor fileDescriptor
     * @param mathDescriptor mathDescriptor
     * @param serviceName    serviceName
     * @return value option value
     */
    static Object getServiceOption(Descriptors.FileDescriptor fileDescriptor,
                                   Descriptors.FieldDescriptor mathDescriptor, String serviceName) {
        return fileDescriptor.findServiceByName(serviceName).getOptions().getField(mathDescriptor);
    }

    /**
     * 获取service集合
     *
     * @param fileDescriptor fileDescriptor
     * @return service集合
     */
    static List<Descriptors.ServiceDescriptor> getServiceName(Descriptors.FileDescriptor fileDescriptor) {
        return fileDescriptor.getServices();
    }

    /**
     * 获取proto描述文件对象
     *
     * @param protoClass protoClass
     * @return proto描述文件对象
     * @throws Exception 未找到getDescriptor方法
     */
    static Descriptors.FileDescriptor getFileDescriptor(Class<?> protoClass) throws Exception {
        Method method = protoClass.getMethod(GET_DESCRIPTOR_METHOD);
        return (Descriptors.FileDescriptor) method.invoke(null);
    }
}

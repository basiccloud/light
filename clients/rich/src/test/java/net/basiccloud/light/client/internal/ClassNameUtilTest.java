package net.basiccloud.light.client.internal;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;


public class ClassNameUtilTest {
    @Test
    public void getGrpcOutClassName() throws Exception {
        String prefixName = "Abcd";
        String name = prefixName + "Grpc";
        String className = ClassNameUtil.getGrpcOutClassName(name + "$");
        assertThat(className).isNotEmpty();
        assertThat(className).isEqualTo(name);
    }

    @Test
    public void getGrpcOutClassName_notLightName() throws Exception {
        String prefixName = "Abcd";
        String name = prefixName + "Grpc";
        Throwable throwable = Assertions.catchThrowable(() -> ClassNameUtil.getGrpcOutClassName(name));
        assertThat(throwable).isInstanceOf(IllegalStateException.class).hasMessageContaining(
                "can't getChannel grpc out class getName from grpc services getName: grpcServiceName=" + name);
    }

    @Test
    public void getDefaultServiceName() throws Exception {
        String prefixName = "Abcd";
        String outName = prefixName + "Grpc";
        String defaultServiceName = ClassNameUtil.getDefaultServiceName(outName);
        assertThat(defaultServiceName).isEqualTo(prefixName);
    }

    @Test
    public void getDefaultServiceName_NotLightOutName() throws Exception {
        String prefixName = "Abcd";

        Throwable throwable = catchThrowable(() -> ClassNameUtil.getDefaultServiceName(prefixName));
        assertThat(throwable).isInstanceOf(IllegalStateException.class).hasMessage(
                        "can't getChannel default grpc services getName from grpc out getName: grpcOutClassSimpleName=" + prefixName);
    }
}
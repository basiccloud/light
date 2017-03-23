package net.basiccloud.light.client;

import net.basiccloud.light.core.EchoRequest;
import net.basiccloud.light.core.EchoResponse;
import net.basiccloud.light.core.PerformanceBaselineMessageProto;
import net.basiccloud.light.core.PerformanceBaselineProto;
import net.basiccloud.light.core.PerformanceBaselineServiceGrpc.PerformanceBaselineServiceBlockingStub;
import net.basiccloud.light.core.TouchRequest;
import net.basiccloud.light.core.TouchResponse;

import org.junit.Test;

public class ClientTest {

    @Test
    public void test01() {
        PerformanceBaselineServiceBlockingStub client = GrpcClientBuilder.sync(
                PerformanceBaselineServiceBlockingStub.class)
                .protoClass(PerformanceBaselineProto.class).useRegistry("etcd://127.0.0.1").build();
        EchoResponse echoResponse = client.echo(EchoRequest.newBuilder().setContent("aaaaa").build());
        System.out.println(echoResponse);
    }
}

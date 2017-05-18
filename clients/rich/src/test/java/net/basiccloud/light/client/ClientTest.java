package net.basiccloud.light.client;

import net.basiccloud.light.core.EchoRequest;
import net.basiccloud.light.core.EchoResponse;
import net.basiccloud.light.core.PerformanceBaselineProto;
import net.basiccloud.light.core.PerformanceBaselineServiceGrpc.PerformanceBaselineServiceBlockingStub;

import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.LinkedBlockingQueue;

public class ClientTest {
    @Ignore("need server start and etcd server")
    @Test
    public void test01() {
        PerformanceBaselineServiceBlockingStub client = GrpcClientBuilder.sync(
                PerformanceBaselineServiceBlockingStub.class)
                .protoClass(PerformanceBaselineProto.class).useRegistry("etcd://127.0.0.1").build();
        EchoResponse echoResponse = client.echo(EchoRequest.newBuilder().setContent("aaaaa").build());
        System.out.println(echoResponse);
    }

    @Ignore("need server start")
    @Test
    public void test4Direct() {
        PerformanceBaselineServiceBlockingStub client = GrpcClientBuilder.sync(
                PerformanceBaselineServiceBlockingStub.class)
                .protoClass(PerformanceBaselineProto.class).useDirectAddress("127.0.0.1:1080").build();

        EchoResponse echoResponse = client.echo(EchoRequest.newBuilder().setContent("aaaaa").build());
        System.out.println(echoResponse);
    }

    @Test
    public void test() throws InterruptedException {
        LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<String>();
        queue.put("aaaaa");
        System.out.println(queue.size());
        String take = queue.take();
        System.out.println(take);
    }

}

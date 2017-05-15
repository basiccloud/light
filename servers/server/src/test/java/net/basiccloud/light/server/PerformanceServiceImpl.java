package net.basiccloud.light.server;


import net.basiccloud.light.core.EchoRequest;
import net.basiccloud.light.core.EchoResponse;
import net.basiccloud.light.core.PerformanceBaselineProto;
import net.basiccloud.light.core.PerformanceBaselineServiceGrpc;
import net.basiccloud.light.core.TouchRequest;
import net.basiccloud.light.core.TouchResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.stub.StreamObserver;

/**
 * test service
 */
@SuppressWarnings("unused")
@LightService(protoClass = PerformanceBaselineProto.class)
class PerformanceServiceImpl extends PerformanceBaselineServiceGrpc.PerformanceBaselineServiceImplBase {

    private static Logger logger = LoggerFactory.getLogger(PerformanceServiceImpl.class);

    @Override
    public void touch(TouchRequest request, StreamObserver<TouchResponse> responseObserver) {
        responseObserver.onNext(TouchResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void echo(EchoRequest request, StreamObserver<EchoResponse> responseObserver) {
        logger.info("request:" + request);
        responseObserver.onNext(EchoResponse.newBuilder().setContent(request.getContent()).build());
        responseObserver.onCompleted();
    }
}

package nl.toefel.tictactoe.server;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class RequestLogInterceptor implements ServerInterceptor {
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
        ServerCall<ReqT, RespT> call,
        Metadata headers,
        ServerCallHandler<ReqT, RespT> next) {
        String time = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString();
        String methodName = call.getMethodDescriptor().getFullMethodName();
        System.out.println(time + ": " + methodName);
        return next.startCall(call, headers);
    }
}

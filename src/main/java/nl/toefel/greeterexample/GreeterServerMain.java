package nl.toefel.greeterexample;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import nl.toefel.grpc.basic.GreeterServiceGrpc;

import java.io.IOException;

import static nl.toefel.grpc.basic.BasicExample.GreetingRequest;
import static nl.toefel.grpc.basic.BasicExample.GreetingResponse;

public class GreeterServerMain {

    static class GreeterService extends GreeterServiceGrpc.GreeterServiceImplBase {
        @Override
        public void greet(GreetingRequest request, StreamObserver<GreetingResponse> responseObserver) {
            GreetingResponse response = GreetingResponse.newBuilder()
                .setGreeting("Hello " + request.getName())
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(8080)
            .addService(new GreeterService())
            .build();
        server.start();
        System.out.println("GreeterService Listening on port 8080");
        server.awaitTermination();
    }
}

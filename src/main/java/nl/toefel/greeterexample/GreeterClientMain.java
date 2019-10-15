package nl.toefel.greeterexample;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import nl.toefel.grpc.basic.GreeterServiceGrpc;

import static nl.toefel.grpc.basic.BasicExample.GreetingRequest;
import static nl.toefel.grpc.basic.BasicExample.GreetingResponse;
import static nl.toefel.grpc.basic.GreeterServiceGrpc.GreeterServiceBlockingStub;
import static nl.toefel.grpc.basic.GreeterServiceGrpc.newBlockingStub;

public class GreeterClientMain {
  public static void main(String[] args) throws InterruptedException {
    ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080)
        .usePlaintext()
        .build();

    GreeterServiceBlockingStub client = newBlockingStub(channel);
    GreeterServiceGrpc.newBlockingStub(channel);
    GreetingRequest request = GreetingRequest.newBuilder()
        .setName("JDriven")
        .build();

    GreetingResponse response = client.greet(request);
    System.out.println(response.getGreeting());
    Thread.sleep(1000);
  }
}

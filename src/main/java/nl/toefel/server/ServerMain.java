package nl.toefel.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class ServerMain {
    public static void main(String[] args) throws IOException, InterruptedException {
        Server service = ServerBuilder.forPort(8080)
            .addService(new TicTacToeGame())
            .build()
            .start();

        Runtime.getRuntime().addShutdownHook(new Thread(service::shutdownNow));
        System.out.println("Started listening for rpc calls on 8080...");
        service.awaitTermination();
    }
}

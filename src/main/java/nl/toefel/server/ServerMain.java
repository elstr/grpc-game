package nl.toefel.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import nl.toefel.server.state.ServerState;

import java.io.IOException;

public class ServerMain {
    public static void main(String[] args) throws IOException, InterruptedException {
        ServerState state = new ServerState();
        Server service = ServerBuilder.forPort(8080)
            .addService(new TicTacToeGame(state))
            .build()
            .start();

        Runtime.getRuntime().addShutdownHook(new Thread(service::shutdownNow));
        System.out.println("Started listening for rpc calls on 8080...");
        service.awaitTermination();
    }
}

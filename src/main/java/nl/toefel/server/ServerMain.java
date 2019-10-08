package nl.toefel.server;

import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import nl.toefel.server.state.ServerState;

import java.io.IOException;

public class ServerMain {
    public static void main(String[] args) throws IOException, InterruptedException {
        ServerState state = new ServerState();
        Server service = NettyServerBuilder.forPort(8080)
            .intercept(new RequestLogInterceptor())
            .addService(new TicTacToeGame(state))
            .build()
            .start();

        Runtime.getRuntime().addShutdownHook(new Thread(service::shutdownNow));
        System.out.println("Started listening for rpc calls on 8080...");
        service.awaitTermination();
    }
}

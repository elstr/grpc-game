package nl.toefel.server;

import io.grpc.stub.StreamObserver;
import nl.toefel.grpc.game.TicTacToeGrpc;
import nl.toefel.grpc.game.TicTacToeOuterClass.ListPlayersRequest;
import nl.toefel.grpc.game.TicTacToeOuterClass.ListPlayersResponse;
import nl.toefel.grpc.game.TicTacToeOuterClass.Player;

public class TicTacToeGame extends TicTacToeGrpc.TicTacToeImplBase {
    @Override
    public void listPlayers(ListPlayersRequest request, StreamObserver<ListPlayersResponse> responseObserver) {
        System.out.println("Received listPlayers call");
        Player player1 = Player.newBuilder()
            .setId("1")
            .setName("Touzani")
            .setJoinTimestamp(System.currentTimeMillis())
            .setWins(0)
            .build();

        Player player2 = Player.newBuilder()
            .setId("2")
            .setName("Frenkie")
            .setJoinTimestamp(System.currentTimeMillis())
            .setWins(0)
            .build();

        ListPlayersResponse listPlayersResponse = ListPlayersResponse.newBuilder()
            .addPlayers(player1)
            .addPlayers(player2)
            .build();

        responseObserver.onNext(listPlayersResponse);
        responseObserver.onCompleted();
    }
}

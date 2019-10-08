package nl.toefel.client.controller;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import nl.toefel.client.state.ClientState;
import nl.toefel.client.view.Modals;
import nl.toefel.grpc.game.TicTacToeOuterClass;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static nl.toefel.grpc.game.TicTacToeGrpc.newBlockingStub;
import static nl.toefel.grpc.game.TicTacToeGrpc.newStub;
import static nl.toefel.grpc.game.TicTacToeOuterClass.*;

public class GrpcController {

    // Executor to run all tasks coming from the UI on worker threads
    private final Executor executor = Executors.newFixedThreadPool(8);

    // Contains the game state used by the JavaFX application
    private final ClientState state;

    public GrpcController(ClientState state) {
        this.state = state;
    }

    public void connectToServer(String host, String port) {
        showDialogOnError(() -> {
            ManagedChannel grpcConnection = ManagedChannelBuilder
                .forAddress(host, Integer.parseInt(port))
                .usePlaintext()
                .build();

            // test the connection, because gRPC lazily connects
            newBlockingStub(grpcConnection).testConnection(TestConnectionRequest.newBuilder().build());

            state.setGrpcConnection(grpcConnection);
        });
    }

    public void createPlayer(String playerName) {
        showDialogOnError(() -> {
            var request = CreatePlayerRequest.newBuilder().setName(playerName).build();
            Player player = newBlockingStub(state.getGrpcConnection()).createPlayer(request);
            state.setMyself(player);

            initializeGameStream();
        });
    }

    public void listPlayers() {
        showDialogOnError(() -> {
            var listPlayersRequest = ListPlayersRequest.newBuilder().build();
            ListPlayersResponse listPlayersResponse = newBlockingStub(state.getGrpcConnection()).listPlayers(listPlayersRequest);
            state.replaceAllPlayers(listPlayersResponse.getPlayersList());
        });
    }

    public void initializeGameStream() {
        showDialogOnError(() -> {
            var choiceStreamObserver = newStub(state.getGrpcConnection()).playGame(
                new StreamObserver<>() {
                    @Override
                    public void onNext(GameEvent gameEvent) {
                        System.out.println("Received game event " + gameEvent);
                        switch (gameEvent.getType()) {
                            case CHALLENGE_GAME:
                            case START_GAME:
                                state.getGameStates().put(gameEvent.getGameId(), gameEvent);
                                break;
                            case BOARD_MOVE:
                            case END_GAME:
                            case UNRECOGNIZED:
                            default:
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                    }

                    @Override
                    public void onCompleted() {
                        System.out.println("On Completed");
                    }
                }
            );
            state.setGameCommandStream(choiceStreamObserver);
        });
    }

    public void addTestGame() {

        Player p1 = Player.newBuilder().setId("1").setName("a").build();
        Player p2 = Player.newBuilder().setId("2").setName("b").build();

        state.getGameStates().put("1", GameEvent.newBuilder()
            .setGameId("1")
            .setType(TicTacToeOuterClass.EventType.START_GAME)
            .setNextPlayer(p1)
            .setPlayerO(p1)
            .setPlayerX(p2)
            .setBoard(createEmptyBoard())
            .build());
    }

    private TicTacToeOuterClass.Board createEmptyBoard() {
        return TicTacToeOuterClass.Board.newBuilder()
            .addRows(createEmptyRow())
            .addRows(createEmptyRow())
            .addRows(createEmptyRow())
            .build();
    }

    private TicTacToeOuterClass.BoardRow createEmptyRow() {
        return TicTacToeOuterClass.BoardRow.newBuilder().addColumns("").addColumns("").addColumns("").build();
    }

    public void challengePlayer(Player opponent) {
        GameCommand challengePlayerCommand = GameCommand.newBuilder()
            .setChallengePlayer(ChallengePlayer.newBuilder()
                .setFromPlayer(state.getMyself())
                .setToPlayer(opponent))
            .build();
        if (state.getGameCommandStream() != null) {
            System.out.println("Sending command: " + challengePlayerCommand);
            state.getGameCommandStream().onNext(challengePlayerCommand);
        } else {
            Modals.showPopup("Game command stream null", "The game command stream is null, connection lost?");
        }
    }

    public void showDialogOnError(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable t) {
            // Status gives an idea of the error cause (like UNAVAILABLE or DEADLINE_EXCEEDED)
            // it also contains the underlying exception
            Status status = Status.fromThrowable(t);
            Modals.showGrpcError("Error", status);
        }
    }
}

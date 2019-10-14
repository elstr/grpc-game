package nl.toefel.tictactoe.client.controller;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import nl.toefel.tictactoe.client.auth.PlayerIdCredentials;
import nl.toefel.tictactoe.client.state.ClientState;
import nl.toefel.tictactoe.client.view.Modals;
import nl.toefel.grpc.game.TicTacToeOuterClass;
import nl.toefel.grpc.game.TicTacToeOuterClass.BoardMove;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static nl.toefel.grpc.game.TicTacToeGrpc.newBlockingStub;
import static nl.toefel.grpc.game.TicTacToeGrpc.newStub;
import static nl.toefel.grpc.game.TicTacToeOuterClass.CreatePlayerRequest;
import static nl.toefel.grpc.game.TicTacToeOuterClass.GameCommand;
import static nl.toefel.grpc.game.TicTacToeOuterClass.GameEvent;
import static nl.toefel.grpc.game.TicTacToeOuterClass.ListPlayersRequest;
import static nl.toefel.grpc.game.TicTacToeOuterClass.ListPlayersResponse;
import static nl.toefel.grpc.game.TicTacToeOuterClass.Player;
import static nl.toefel.grpc.game.TicTacToeOuterClass.StartGame;
import static nl.toefel.grpc.game.TicTacToeOuterClass.TestConnectionRequest;

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

  public void initializeGameStream() {
    showDialogOnError(() -> {
      var commandStreamObserver = newStub(state.getGrpcConnection())
          .withCallCredentials(new PlayerIdCredentials(state.getMyself().getId()))
          .playGame(
          new StreamObserver<>() {
            @Override
            public void onNext(GameEvent gameEvent) {
              state.getGameStates().put(gameEvent.getGameId(), gameEvent);
            }

            @Override
            public void onError(Throwable throwable) {
              throwable.printStackTrace();
            }

            @Override
            public void onCompleted() {
              Modals.showPopup("Completed", "game event stream completed");
            }
          }
      );
      state.setGameCommandStream(commandStreamObserver);
    });
  }

  public void listPlayers() {
    showDialogOnError(() -> {
      var listPlayersRequest = ListPlayersRequest.newBuilder().build();
      ListPlayersResponse listPlayersResponse = newBlockingStub(state.getGrpcConnection())
          .withCallCredentials(new PlayerIdCredentials(state.getMyself().getId()))
          .listPlayers(listPlayersRequest);
      state.replaceAllPlayers(listPlayersResponse.getPlayersList());
    });
  }

  public void startGameAgainstPlayer(Player opponent) {
    GameCommand startGameCommand = GameCommand.newBuilder()
        .setStartGame(StartGame.newBuilder()
            .setFromPlayer(state.getMyself())
            .setToPlayer(opponent))
        .build();
    StreamObserver<GameCommand> gameCommandStream = state.getGameCommandStream();
    if (gameCommandStream != null) {
      gameCommandStream.onNext(startGameCommand);
    } else {
      Modals.showPopup("Game command stream null", "The game command stream is null, connection lost?");
    }
  }

  public void makeBoardMove(BoardMove move) {
    StreamObserver<GameCommand> gameCommandStream = state.getGameCommandStream();
    if (gameCommandStream != null) {
      gameCommandStream.onNext(GameCommand.newBuilder().setBoardMove(move).build());
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

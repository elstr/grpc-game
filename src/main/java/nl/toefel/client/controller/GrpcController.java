package nl.toefel.client.controller;

import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import nl.toefel.client.state.UIGameState;
import nl.toefel.client.view.Modals;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static nl.toefel.grpc.game.TicTacToeGrpc.TicTacToeBlockingStub;
import static nl.toefel.grpc.game.TicTacToeGrpc.newBlockingStub;
import static nl.toefel.grpc.game.TicTacToeOuterClass.CreatePlayerRequest;
import static nl.toefel.grpc.game.TicTacToeOuterClass.ListPlayersRequest;
import static nl.toefel.grpc.game.TicTacToeOuterClass.ListPlayersResponse;
import static nl.toefel.grpc.game.TicTacToeOuterClass.Player;

public class GrpcController {

  // Executor to run all tasks coming from the UI on worker threads
  private final Executor executor = Executors.newFixedThreadPool(8);

  // Contains the game state used by the JavaFX application
  private final UIGameState state;

  // GRPC connection
  private ManagedChannel channel;

  public GrpcController(UIGameState state) {
    this.state = state;
  }

  public void connect(String host, int port) throws ControllerException {
    channel = ManagedChannelBuilder
        .forAddress(host, port)
        .usePlaintext()
        .build();

    ConnectivityState state = channel.getState(true);

    if (state != ConnectivityState.READY) {
      System.out.println("connection: not ready " + state);
    }
  }

  public void createPlayer(String playerName) {
    showDialogOnError(() -> {
      var request = CreatePlayerRequest.newBuilder().setName(playerName).build();
      Player player = newBlockingStub(channel).createPlayer(request);
      Modals.showPopup("Success", "Created player: " + player.toString());
    });
  }

  public void listPlayers() {
    showDialogOnError(() -> {
      var listPlayersRequest = ListPlayersRequest.newBuilder().build();
      ListPlayersResponse listPlayersResponse = newBlockingStub(channel).listPlayers(listPlayersRequest);
      state.replaceAllPlayers(listPlayersResponse.getPlayersList());
    });
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

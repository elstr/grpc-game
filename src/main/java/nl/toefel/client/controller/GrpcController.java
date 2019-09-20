package nl.toefel.client.controller;

import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import nl.toefel.client.state.ClientState;
import nl.toefel.client.view.Modals;
import nl.toefel.grpc.game.TicTacToeOuterClass;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static nl.toefel.grpc.game.TicTacToeGrpc.newBlockingStub;
import static nl.toefel.grpc.game.TicTacToeOuterClass.*;
import static nl.toefel.grpc.game.TicTacToeOuterClass.CreatePlayerRequest;
import static nl.toefel.grpc.game.TicTacToeOuterClass.ListPlayersRequest;
import static nl.toefel.grpc.game.TicTacToeOuterClass.ListPlayersResponse;
import static nl.toefel.grpc.game.TicTacToeOuterClass.Player;

public class GrpcController {

  // Executor to run all tasks coming from the UI on worker threads
  private final Executor executor = Executors.newFixedThreadPool(8);

  // Contains the game state used by the JavaFX application
  private final ClientState state;

  public GrpcController(ClientState state) {
    this.state = state;
  }

  public void connectToServer(String host, String port) throws ControllerException {
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
      Modals.showPopup("Success", "Created player: " + player.toString());
    });
  }

  public void listPlayers() {
    showDialogOnError(() -> {
      var listPlayersRequest = ListPlayersRequest.newBuilder().build();
      ListPlayersResponse listPlayersResponse = newBlockingStub(state.getGrpcConnection()).listPlayers(listPlayersRequest);
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

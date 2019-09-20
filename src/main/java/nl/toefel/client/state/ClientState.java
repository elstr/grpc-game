package nl.toefel.client.state;

import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import nl.toefel.client.controller.ControllerException;

import java.util.List;

import static nl.toefel.grpc.game.TicTacToeOuterClass.Player;

/**
 * Holds all the state used by the client application.
 */
public class ClientState {

  // GRPC connection
  private ManagedChannel grpcConnection;

  // The current player
  private Player myself = null;

  // All the players
  private final ObservableList<Player> players = FXCollections.observableArrayList();

  public void setGrpcConnection(ManagedChannel grpcConnection) {
    this.grpcConnection = grpcConnection;
  }

  public ManagedChannel getGrpcConnection() {
    return grpcConnection;
  }

  public Player getMyself() {
    return myself;
  }

  public void replaceAllPlayers(List<Player> newPlayers) {
    // dispatch on UI thread
    Platform.runLater(() -> {
      players.clear();
      players.addAll(newPlayers);
    });
  }

  // Only call this on UI thread!
  public ObservableList<Player> getPlayers() {
    return players;
  }
}

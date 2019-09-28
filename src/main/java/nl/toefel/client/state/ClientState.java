package nl.toefel.client.state;

import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import nl.toefel.client.view.Modals;
import nl.toefel.grpc.game.TicTacToeOuterClass;
import nl.toefel.grpc.game.TicTacToeOuterClass.GameState;

import java.util.List;

import static nl.toefel.grpc.game.TicTacToeOuterClass.Player;

/**
 * Holds all the state used by the client application.
 */
public class ClientState {

  // GRPC connection
  private ManagedChannel grpcConnection;

  // The current player
  private Player myself;

  // Observable GRPC connection holder
  private final SimpleObjectProperty<ManagedChannel> grpcConnectionProperty = new SimpleObjectProperty<>(null);

  // Observable Current player holder
  private final SimpleObjectProperty<Player> myselfProperty = new SimpleObjectProperty<>(null);

  // Observable list with all the players
  private final ObservableList<Player> players = FXCollections.observableArrayList();

  // The state of the current active game
  private final SimpleObjectProperty<GameState> gameStateProperty = new SimpleObjectProperty<>(null);

  public void setGrpcConnection(ManagedChannel grpcConnection) {
    this.grpcConnection = grpcConnection;
    this.grpcConnectionProperty.set(this.grpcConnection);
    this.grpcConnection.notifyWhenStateChanged(ConnectivityState.READY, () -> {
      reset();
      Modals.showGrpcError("Disconnected", Status.CANCELLED);
    });
  }

  private void reset() {
    Platform.runLater(() -> {
      grpcConnection.shutdownNow();
      grpcConnection = null;
      grpcConnectionProperty.set(null);
      myself = null;
      myselfProperty.set(null);
      players.clear();
      gameStateProperty.set(null);
    });
  }

  public ManagedChannel getGrpcConnection() {
    return grpcConnection;
  }

  public SimpleObjectProperty<ManagedChannel> getGrpcConnectionProperty() {
    return grpcConnectionProperty;
  }

  public Player getMyself() {
    return myself;
  }

  public void setMyself(Player myself) {
    this.myself = myself;
    this.myselfProperty.set(this.myself);
  }

  public SimpleObjectProperty<Player> getMyselfProperty() {
    return myselfProperty;
  }

  public SimpleObjectProperty<Player> myselfPropertyProperty() {
    return myselfProperty;
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

  public SimpleObjectProperty<GameState> getGameStateProperty() {
    return gameStateProperty;
  }
}

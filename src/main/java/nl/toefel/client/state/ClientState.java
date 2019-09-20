package nl.toefel.client.state;

import io.grpc.ManagedChannel;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

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

  public void setGrpcConnection(ManagedChannel grpcConnection) {
    this.grpcConnection = grpcConnection;
    this.grpcConnectionProperty.set(this.grpcConnection);
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
}

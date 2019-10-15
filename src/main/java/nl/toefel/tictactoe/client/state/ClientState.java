package nl.toefel.tictactoe.client.state;

import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import nl.toefel.tictactoe.client.view.Modals;
import nl.toefel.grpc.game.TicTacToeOuterClass.GameEvent;

import java.util.LinkedHashMap;
import java.util.List;

import static nl.toefel.grpc.game.TicTacToeOuterClass.GameCommand;
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

  // Maps gameId to the latest game event, insertion ordered
  private final ObservableMap<String, GameEvent> gameStates = FXCollections.observableMap(new LinkedHashMap<>());

  // The command event stream to send game commands on (like challenge player, or a board move)
  private final SimpleObjectProperty<StreamObserver<GameCommand>> gameCommandStream = new SimpleObjectProperty<>(null);

  public void setGrpcConnection(ManagedChannel grpcConnection) {
    this.grpcConnection = grpcConnection;
    this.grpcConnectionProperty.set(this.grpcConnection);
    this.grpcConnection.notifyWhenStateChanged(ConnectivityState.READY, () -> {
      reset();
      Modals.showGrpcError("Disconnected", Status.CANCELLED);
    });
  }

  public void disconnect() {
    this.reset();
  }

  private void reset() {
    Platform.runLater(() -> {
      grpcConnection.shutdownNow();
      grpcConnection = null;
      grpcConnectionProperty.set(null);
      myself = null;
      myselfProperty.set(null);
      players.clear();
      gameCommandStream.set(null);
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

  public void setPlayers(List<Player> newPlayers) {
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

  public StreamObserver<GameCommand> getGameCommandStream() {
    return gameCommandStream.get();
  }

  public void setGameCommandStream(StreamObserver<GameCommand> gameCommandStream) {
    this.gameCommandStream.set(gameCommandStream);
  }

  public ObservableMap<String, GameEvent> getGameStates() {
    return gameStates;
  }

  public void onGameStreamError(Throwable throwable) {
    Modals.showGrpcError("Error received from the game stream", Status.fromThrowable(throwable));
    this.reset();
  }

  public void onGameStreamCompleted() {
    Modals.showPopup("Game stream completed", "Closing connections");
    this.reset();
  }
}

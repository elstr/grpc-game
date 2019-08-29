package nl.toefel.client.state;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

import static nl.toefel.grpc.game.TicTacToeOuterClass.Player;

public class UIGameState {
  private final ObservableList<Player> players = FXCollections.observableArrayList();

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

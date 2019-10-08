package nl.toefel.client.view;

import javafx.beans.InvalidationListener;
import javafx.collections.ObservableMap;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import nl.toefel.grpc.game.TicTacToeOuterClass.GameEvent;

import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

public class GamesTabComponent extends TabPane {



  public GamesTabComponent(ObservableMap<String, GameEvent> gameStates) {
    gameStates.addListener((InvalidationListener) x -> {
      createTabs(gameStates);
    });
  }

  private void createTabs(ObservableMap<String, GameEvent> gameStates) {
    Map<String, Tab> tabsById = getTabs().stream().collect(toMap(Tab::getText, Function.identity()));

    for (GameEvent event : gameStates.values()) {
      Tab tab = tabsById.computeIfAbsent(event.getGameId(), Tab::new);
      GameComponent gameComponent =
      getTabs().add()
    }

  }
}

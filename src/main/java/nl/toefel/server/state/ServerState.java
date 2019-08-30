package nl.toefel.server.state;

import nl.toefel.grpc.game.TicTacToeOuterClass.Game;
import nl.toefel.grpc.game.TicTacToeOuterClass.Player;

import java.util.ArrayList;
import java.util.List;

public class ServerState {
  private int playerIdSequence = 0;
  private int gameIdSequence = 0;
  private List<Player> players = new ArrayList<>();
  private List<Game> games = new ArrayList<>();

  public List<Player> getPlayers() {
    return new ArrayList<>(players);
  }

  public List<Game> getGames() {
    return new ArrayList<>(games);
  }

  public Player createPlayer(String name) {
    if(players.stream().anyMatch(p -> p.getName().equals(name))) {
      throw new AlreadyExistsException("player with name " + name + " already exists");
    }

    Player player = Player.newBuilder()
        .setId("" + playerIdSequence++)
        .setName(name)
        .setJoinTimestamp(System.currentTimeMillis())
        .setWins(0)
        .build();

    players.add(player); // and publish

    return player;
  }
}

package nl.toefel.server.state;

import io.grpc.stub.StreamObserver;
import nl.toefel.grpc.game.TicTacToeOuterClass.GameCommand;
import nl.toefel.grpc.game.TicTacToeOuterClass.GameEvent;
import nl.toefel.grpc.game.TicTacToeOuterClass.Player;

public class PlayerWithStreams {
  // set after Identify command
  private Player player;
  private StreamObserver<GameCommand> commandFromPlayer;
  private StreamObserver<GameEvent> gameEventToPlayer;

  public PlayerWithStreams(Player player, StreamObserver<GameCommand> commandFromPlayer, StreamObserver<GameEvent> gameEventToPlayer) {
    this.player = player;
    this.commandFromPlayer = commandFromPlayer;
    this.gameEventToPlayer = gameEventToPlayer;
  }

  // only available after identify command
  public Player getPlayer() {
    return player;
  }

  public void setPlayer(Player player) {
    this.player = player;
  }

  public StreamObserver<GameCommand> getCommandStream() {
    return commandFromPlayer;
  }

  public StreamObserver<GameEvent> getEventStream() {
    return gameEventToPlayer;
  }
}

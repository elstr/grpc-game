package nl.toefel.tictactoe.server.state;

import io.grpc.stub.StreamObserver;
import nl.toefel.grpc.game.TicTacToeOuterClass.GameCommand;
import nl.toefel.grpc.game.TicTacToeOuterClass.GameEvent;
import nl.toefel.grpc.game.TicTacToeOuterClass.Player;

public class PlayerWithIO {
  private final Player player;
  private final StreamObserver<GameCommand> commandFromPlayer;
  private final StreamObserver<GameEvent> gameEventToPlayer;

  public PlayerWithIO(Player player, StreamObserver<GameCommand> commandFromPlayer, StreamObserver<GameEvent> gameEventToPlayer) {
    this.player = player;
    this.commandFromPlayer = commandFromPlayer;
    this.gameEventToPlayer = gameEventToPlayer;
  }

  public Player getPlayer() {
    return player;
  }

  public StreamObserver<GameCommand> getCommandStream() {
    return commandFromPlayer;
  }

  public StreamObserver<GameEvent> getEventStream() {
    return gameEventToPlayer;
  }
}

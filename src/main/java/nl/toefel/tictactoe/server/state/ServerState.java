package nl.toefel.tictactoe.server.state;

import io.grpc.stub.StreamObserver;
import nl.toefel.grpc.game.TicTacToeOuterClass.Identify;
import nl.toefel.grpc.game.TicTacToeOuterClass.Player;
import nl.toefel.grpc.game.TicTacToeOuterClass.StartGame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static nl.toefel.grpc.game.TicTacToeOuterClass.Board;
import static nl.toefel.grpc.game.TicTacToeOuterClass.BoardMove;
import static nl.toefel.grpc.game.TicTacToeOuterClass.BoardRow;
import static nl.toefel.grpc.game.TicTacToeOuterClass.EventType;
import static nl.toefel.grpc.game.TicTacToeOuterClass.GameCommand;
import static nl.toefel.grpc.game.TicTacToeOuterClass.GameEvent;

public class ServerState {
  private AtomicInteger playerIdSequence = new AtomicInteger(1);
  private List<Player> players = new ArrayList<>();
  private List<PlayerWithStreams> playerWithStreams = new ArrayList<>();
  private AtomicInteger gameIdSequence = new AtomicInteger(1);
  // a game event contains the latest state of a game
  private Map<String, GameEvent> games = new HashMap<>();


  public List<Player> getPlayers() {
    return new ArrayList<>(players);
  }

  public Player createPlayer(String name) {
    if (players.stream().anyMatch(p -> p.getName().equals(name))) {
      throw new AlreadyExistsException("player with name " + name + " already exists");
    }

    Player player = Player.newBuilder()
        .setId(String.valueOf(playerIdSequence.getAndIncrement()))
        .setName(name)
        .setJoinTimestamp(System.currentTimeMillis())
        .setWins(0)
        .build();

    players.add(player); // and publish

    return player;
  }

  public void trackSteam(StreamObserver<GameCommand> gameCommandStream, StreamObserver<GameEvent> responseObserver) {
    PlayerWithStreams playerWithChannels = new PlayerWithStreams(null, gameCommandStream, responseObserver);
    this.playerWithStreams.add(playerWithChannels);
  }

  public void onGameCommand(StreamObserver<GameCommand> commandStream, GameCommand command) {
    System.out.println(command);
    switch (command.getCommandCase()) {
      case IDENTIFY:
        // Identify is required because when opening the stream we do not know which player is opening the stream,
        // When another player wants to challenge another player, we need to find the GameEvent stream that leads to the player.
        // TODO change to metadata
        linkPlayerToStream(commandStream, command.getIdentify());
        break;
      case START_GAME:
        startGameBetweenTwoPlayers(commandStream, command.getStartGame());
        break;
      case BOARD_MOVE:
        processPlayerMove(commandStream, command.getBoardMove());
        break;
      default:
        System.out.println("Unknown command " + command);
    }
  }

  private void linkPlayerToStream(StreamObserver<GameCommand> commandStream, Identify identifyCommand) {
    playerWithStreams.stream()
        .filter(it -> it.getCommandStream() == commandStream)
        .findFirst()
        .ifPresentOrElse(
            it -> it.setPlayer(identifyCommand.getPlayer()),
            () -> System.out.println("no stream found to link player to, player: " + identifyCommand));
  }

  private void startGameBetweenTwoPlayers(StreamObserver<GameCommand> commandStream, StartGame startGameCommand) {
    Optional<PlayerWithStreams> fromPlayer = findPlayerWithStreamByPlayerId(startGameCommand.getFromPlayer().getId());
    Optional<PlayerWithStreams> toPlayer = findPlayerWithStreamByPlayerId(startGameCommand.getToPlayer().getId());

    if (fromPlayer.isPresent() && toPlayer.isPresent()) {
      GameEvent event = createNewGame(startGameCommand);
      games.put(event.getGameId(), event);

      fromPlayer.get().getEventStream().onNext(event);
      toPlayer.get().getEventStream().onNext(event);
    } else {
      System.out.println("Could not find to or from player streams for which a start game was requested");
      System.out.println("fromPlayer: " + fromPlayer);
      System.out.println("toPlayer: " + toPlayer);
    }
  }

  private void processPlayerMove(StreamObserver<GameCommand> commandStream, BoardMove boardMove) {
    GameEvent gameEvent = games.get(boardMove.getGameId());
    Optional<PlayerWithStreams> playerMakingMove = findPlayerWithStreamByCommandStream(commandStream);

    if (gameEvent == null) {
      System.out.println("Requested board move for nonexisting game " + boardMove);
    } else if (playerMakingMove.isEmpty()) {
      System.out.println("Did not find player making the move");
    } else if (!eq(gameEvent.getNextPlayer(), playerMakingMove.get().getPlayer())) {
      System.out.println("Player going before his turn: " +  playerMakingMove.get().getPlayer());
    } else {
      GameEvent newEvent = gameEvent.toBuilder()
          .setType(EventType.BOARD_MOVE)
          .setNextPlayer(determineNextPlayer(gameEvent))
          .setBoard(updateBoardWithMove(playerMakingMove.get().getPlayer(), gameEvent, boardMove))
          .build();

      games.put(boardMove.getGameId(), newEvent);

      findPlayerWithStreamByPlayerId(gameEvent.getPlayerO().getId()).ifPresent(it -> it.getEventStream().onNext(newEvent));
      findPlayerWithStreamByPlayerId(gameEvent.getPlayerX().getId()).ifPresent(it -> it.getEventStream().onNext(newEvent));
    }
  }

  private Player determineNextPlayer(GameEvent gameEvent) {
    if (eq(gameEvent.getPlayerO(), gameEvent.getNextPlayer())) {
      return gameEvent.getPlayerX();
    } else {
      return gameEvent.getPlayerO();
    }
  }

  private Board updateBoardWithMove(Player player, GameEvent currentGameState, BoardMove boardMove) {
    String sign = eq(player, currentGameState.getPlayerO()) ? "O" : "X";
    BoardRow updatedRow = currentGameState.getBoard().getRows(boardMove.getRow()).toBuilder()
        .setColumns(boardMove.getColumn(), sign)
        .build();
    return currentGameState.getBoard().toBuilder()
        .setRows(boardMove.getRow(), updatedRow)
        .build();
  }


  private boolean eq(Player p1, Player p2) {
    return p1.getId().equals(p2.getId());
  }

  private GameEvent createNewGame(StartGame startGameCommand) {
    return GameEvent.newBuilder()
        .setGameId(String.valueOf(gameIdSequence.getAndIncrement()))
        .setType(EventType.START_GAME)
        .setNextPlayer(startGameCommand.getFromPlayer())
        .setPlayerO(startGameCommand.getFromPlayer())
        .setPlayerX(startGameCommand.getToPlayer())
        .setBoard(createEmptyBoard())
        .build();
  }

  private Optional<PlayerWithStreams> findPlayerWithStreamByPlayerId(String playerId) {
    return playerWithStreams.stream()
        .filter(it -> it.getPlayer() != null)
        .filter(it -> playerId.equals(it.getPlayer().getId()))
        .findFirst();
  }

  private Optional<PlayerWithStreams> findPlayerWithStreamByCommandStream(StreamObserver<GameCommand> commandStream) {
    return playerWithStreams.stream()
        .filter(it -> commandStream == it.getCommandStream())
        .findFirst();
  }

  private Board createEmptyBoard() {
    return Board.newBuilder()
        .addRows(createEmptyRow())
        .addRows(createEmptyRow())
        .addRows(createEmptyRow())
        .build();
  }

  private BoardRow createEmptyRow() {
    return BoardRow.newBuilder().addColumns("").addColumns("").addColumns("").build();
  }
}

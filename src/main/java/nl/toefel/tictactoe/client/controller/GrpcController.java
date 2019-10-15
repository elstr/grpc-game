package nl.toefel.tictactoe.client.controller;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import nl.toefel.grpc.game.TicTacToeOuterClass.BoardMove;
import nl.toefel.tictactoe.client.auth.PlayerIdCredentials;
import nl.toefel.tictactoe.client.state.ClientState;
import nl.toefel.tictactoe.client.view.Modals;

import static nl.toefel.grpc.game.TicTacToeGrpc.newBlockingStub;
import static nl.toefel.grpc.game.TicTacToeGrpc.newStub;
import static nl.toefel.grpc.game.TicTacToeOuterClass.CreatePlayerRequest;
import static nl.toefel.grpc.game.TicTacToeOuterClass.GameCommand;
import static nl.toefel.grpc.game.TicTacToeOuterClass.GameEvent;
import static nl.toefel.grpc.game.TicTacToeOuterClass.ListPlayersRequest;
import static nl.toefel.grpc.game.TicTacToeOuterClass.ListPlayersResponse;
import static nl.toefel.grpc.game.TicTacToeOuterClass.Player;
import static nl.toefel.grpc.game.TicTacToeOuterClass.StartGame;
import static nl.toefel.grpc.game.TicTacToeOuterClass.TestConnectionRequest;

/**
 * Controller that receives events from the window, like clicks on buttons and clicks within game fields.
 */
public class GrpcController implements Controller {

  // Contains the game state used by the JavaFX application
  private final ClientState state;

  public GrpcController(ClientState state) {
    this.state = state;
  }

  @Override
  public void connectToServer(String host, String port) {
     //EXERCISE 1 starting point
      ManagedChannel grpcConnection = ManagedChannelBuilder
          .forAddress(host, Integer.parseInt(port))
          .usePlaintext()
          .build();

      // test the connection, because gRPC lazily connects
      newBlockingStub(grpcConnection).testConnection(TestConnectionRequest.newBuilder().build());

      state.setGrpcConnection(grpcConnection);
  }

  @Override
  public void joinGame(String playerName) {
    createPlayer(playerName);
    initializeGameStream();
  }

  private void createPlayer(String playerName) {
    //EXERCISE 2 starting point
    var request = CreatePlayerRequest.newBuilder().setName(playerName).build();
    Player player = newBlockingStub(state.getGrpcConnection()).createPlayer(request);
    state.setMyself(player);
  }

  @Override
  public void listPlayers() {
    var listPlayersRequest = ListPlayersRequest.newBuilder().build();
    ListPlayersResponse listPlayersResponse = newBlockingStub(state.getGrpcConnection())
        .withCallCredentials(new PlayerIdCredentials(state.getMyself().getId()))
        .listPlayers(listPlayersRequest);
    state.setPlayers(listPlayersResponse.getPlayersList());
  }

  private void initializeGameStream() {
      var commandStreamObserver = newStub(state.getGrpcConnection())
          .withCallCredentials(new PlayerIdCredentials(state.getMyself().getId()))
          .playGame(
          new StreamObserver<>() {
            @Override
            public void onNext(GameEvent gameEvent) {
              state.getGameStates().put(gameEvent.getGameId(), gameEvent);
            }

            @Override
            public void onError(Throwable throwable) {
              state.onGameStreamError(throwable);
            }

            @Override
            public void onCompleted() {
              state.onGameStreamCompleted();
            }
          }
      );
      state.setGameCommandStream(commandStreamObserver);
  }

  @Override
  public void startGameAgainstPlayer(Player opponent) {
    GameCommand startGameCommand = GameCommand.newBuilder()
        .setStartGame(StartGame.newBuilder()
            .setFromPlayer(state.getMyself())
            .setToPlayer(opponent))
        .build();
    StreamObserver<GameCommand> gameCommandStream = state.getGameCommandStream();
    if (gameCommandStream != null) {
      gameCommandStream.onNext(startGameCommand);
    } else {
      Modals.showPopup("Game command stream null", "The game command stream is null, connection lost?");
    }
  }

  @Override
  public void makeBoardMove(BoardMove move) {
    StreamObserver<GameCommand> gameCommandStream = state.getGameCommandStream();
    if (gameCommandStream != null) {
      gameCommandStream.onNext(GameCommand.newBuilder().setBoardMove(move).build());
    }
  }
}

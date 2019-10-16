package nl.toefel.tictactoe.client.controller;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import nl.toefel.grpc.game.TicTacToeOuterClass.BoardMove;
import nl.toefel.tictactoe.client.auth.PlayerIdCredentials;
import nl.toefel.tictactoe.client.state.ClientState;

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
     //EXERCISE 1 starting point, see https://github.com/toefel18/grpc-game
  }

  @Override
  public void joinGame(String playerName) {
    createPlayer(playerName);
    initializeGameStream();
  }

  private void createPlayer(String playerName) {
    //EXERCISE 2 starting point, see https://github.com/toefel18/grpc-game
  }

  private void initializeGameStream() {
    // EXERCISE 3 starting point, see https://github.com/toefel18/grpc-game
  }

  @Override
  public void listPlayers() {
    // EXERCISE 4 starting point, see https://github.com/toefel18/grpc-game
  }

  @Override
  public void startGameAgainstPlayer(Player opponent) {
    // EXERCISE 5 starting point, see https://github.com/toefel18/grpc-game
  }

  @Override
  public void makeBoardMove(BoardMove move) {
    // EXERCISE 6 starting point, see https://github.com/toefel18/grpc-game
  }
}

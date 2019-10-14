package nl.toefel.tictactoe.server.controller;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import nl.toefel.grpc.game.TicTacToeGrpc;
import nl.toefel.grpc.game.TicTacToeOuterClass.CreatePlayerRequest;
import nl.toefel.grpc.game.TicTacToeOuterClass.GameCommand;
import nl.toefel.grpc.game.TicTacToeOuterClass.GameEvent;
import nl.toefel.grpc.game.TicTacToeOuterClass.ListPlayersRequest;
import nl.toefel.grpc.game.TicTacToeOuterClass.ListPlayersResponse;
import nl.toefel.grpc.game.TicTacToeOuterClass.Player;
import nl.toefel.grpc.game.TicTacToeOuterClass.TestConnectionRequest;
import nl.toefel.grpc.game.TicTacToeOuterClass.TestConnectionResponse;
import nl.toefel.tictactoe.server.state.AlreadyExistsException;
import nl.toefel.tictactoe.server.state.AutoClosableLocker;
import nl.toefel.tictactoe.server.state.ServerState;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TicTacToeServerController extends TicTacToeGrpc.TicTacToeImplBase {

  private final Lock lock;
  private final ServerState state;

  public TicTacToeServerController(ServerState state) {
    this.state = state;
    this.lock = new ReentrantLock();
  }

  @Override
  public void testConnection(TestConnectionRequest request, StreamObserver<TestConnectionResponse> responseObserver) {
    responseObserver.onNext(TestConnectionResponse.newBuilder().build());
    responseObserver.onCompleted();
  }

  @Override
  public void createPlayer(CreatePlayerRequest request, StreamObserver<Player> responseObserver) {
    withLockAndErrorHandling(() -> {
      Player player = state.createNewPlayer(request.getName());
      responseObserver.onNext(player);
      responseObserver.onCompleted();
    }, responseObserver);
  }

  @Override
  public void listPlayers(ListPlayersRequest request, StreamObserver<ListPlayersResponse> responseObserver) {
    withLockAndErrorHandling(() -> {
      ListPlayersResponse response = ListPlayersResponse.newBuilder().addAllPlayers(state.getJoinedPlayers()).build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    }, responseObserver);
  }

  // TODO wrap calls to state in locks
  @Override
  public StreamObserver<GameCommand> playGame(StreamObserver<GameEvent> responseObserver) {
    StreamObserver<GameCommand> gameCommandStream = new StreamObserver<GameCommand>() {
      @Override
      public void onNext(GameCommand command) {
        state.onGameCommand(command);
      }

      @Override
      public void onError(Throwable t) {
        state.unjoinPlayer();
      }

      @Override
      public void onCompleted() {
        state.unjoinPlayer();
      }
    };

    // Register this game stream so we we can distribute events from other players to it at a later time.
    state.joinPlayerAndTrack(gameCommandStream, responseObserver);

    return gameCommandStream;
  }

  /**
   * Runs code within the global lock and handles errors by re-throwing them as a grpc Status which is understood
   * by the grpc system.
   *
   * @param function
   * @param responseObserver
   */
  public void withLockAndErrorHandling(Runnable function, StreamObserver<?> responseObserver) {
    try (var ignored = new AutoClosableLocker(lock)) {
      function.run();
    } catch (AlreadyExistsException e) {
      log(e);
      responseObserver.onError(Status.ALREADY_EXISTS.withDescription(e.getMessage()).asRuntimeException());
    } catch (Exception e) {
      log(e);
      responseObserver.onError(Status.UNKNOWN.withDescription(e.getMessage()).withCause(e).asRuntimeException());
    }
  }

  private void log(Exception e) {
    log(e.getClass().getSimpleName() + ": " + e.getMessage());
  }

  private void log(String msg) {
    System.out.println(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString() + ": " + msg);
  }
}

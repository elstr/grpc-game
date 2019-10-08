package nl.toefel.server;

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
import nl.toefel.server.state.AlreadyExistsException;
import nl.toefel.server.state.AutoClosableLocker;
import nl.toefel.server.state.ServerState;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TicTacToeGame extends TicTacToeGrpc.TicTacToeImplBase {

  private final Lock lock;
  private final ServerState state;

  public TicTacToeGame(ServerState state) {
    this.state = state;
    this.lock = new ReentrantLock();
  }

  @Override
  public void testConnection(TestConnectionRequest request, StreamObserver<TestConnectionResponse> responseObserver) {
    responseObserver.onNext(TestConnectionResponse.newBuilder().build());
    responseObserver.onCompleted();
  }

  @Override
  public void listPlayers(ListPlayersRequest request, StreamObserver<ListPlayersResponse> responseObserver) {
    withLockAndErrorHandling(() -> {
      ListPlayersResponse response = ListPlayersResponse.newBuilder().addAllPlayers(state.getPlayers()).build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    }, responseObserver);
  }

  @Override
  public void createPlayer(CreatePlayerRequest request, StreamObserver<Player> responseObserver) {
    withLockAndErrorHandling(() -> {
      Player player = state.createPlayer(request.getName());
      responseObserver.onNext(player);
      responseObserver.onCompleted();
    }, responseObserver);
  }

  @Override
  public StreamObserver<GameCommand> playGame(StreamObserver<GameEvent> responseObserver) {
    StreamObserver<GameCommand> gameCommandStream = new StreamObserver<GameCommand>() {
      @Override
      public void onNext(GameCommand command) {
        state.onGameCommand(this, command);
      }

      @Override
      public void onError(Throwable t) {
        t.printStackTrace();
      }

      @Override
      public void onCompleted() {
        System.out.println("on completed");
      }
    };

    state.trackSteam(gameCommandStream, responseObserver);
    return gameCommandStream;
  }

  /**
   * Runs code within the global lock and handles errors by re-throwing them as a grpc Status which is understood
   * by the grpc system.
   *
   * TODO move lock to state or create GrpcController decorator
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

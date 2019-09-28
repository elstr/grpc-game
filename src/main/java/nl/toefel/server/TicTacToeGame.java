package nl.toefel.server;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import nl.toefel.grpc.game.TicTacToeGrpc;
import nl.toefel.grpc.game.TicTacToeOuterClass.*;
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
    log("test connection call received");
    responseObserver.onNext(TestConnectionResponse.newBuilder().build());
    responseObserver.onCompleted();
  }

  @Override
  public void listPlayers(ListPlayersRequest request, StreamObserver<ListPlayersResponse> responseObserver) {
    log("listPlayers call received");
    withLockAndErrorHandling(() -> {
      ListPlayersResponse response = ListPlayersResponse.newBuilder().addAllPlayers(state.getPlayers()).build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    }, responseObserver);
  }

  @Override
  public void createPlayer(CreatePlayerRequest request, StreamObserver<Player> responseObserver) {
    log("createPlayer call received");
    withLockAndErrorHandling(() -> {
      Player player = state.createPlayer(request.getName());
      responseObserver.onNext(player);
      responseObserver.onCompleted();
    }, responseObserver);
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

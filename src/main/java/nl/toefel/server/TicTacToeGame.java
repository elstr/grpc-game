package nl.toefel.server;

import io.grpc.Server;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import nl.toefel.grpc.game.TicTacToeGrpc;
import nl.toefel.grpc.game.TicTacToeOuterClass;
import nl.toefel.grpc.game.TicTacToeOuterClass.ListPlayersRequest;
import nl.toefel.grpc.game.TicTacToeOuterClass.ListPlayersResponse;
import nl.toefel.grpc.game.TicTacToeOuterClass.Player;
import nl.toefel.server.state.AlreadyExistsException;
import nl.toefel.server.state.AutoClosableLocker;
import nl.toefel.server.state.ServerState;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static nl.toefel.grpc.game.TicTacToeOuterClass.*;
import static nl.toefel.grpc.game.TicTacToeOuterClass.CreatePlayerRequest;
import static nl.toefel.grpc.game.TicTacToeOuterClass.ListGamesRequest;
import static nl.toefel.grpc.game.TicTacToeOuterClass.ListGamesResponse;

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
  public void listGames(ListGamesRequest request, StreamObserver<ListGamesResponse> responseObserver) {
    withLockAndErrorHandling(() -> {
      ListGamesResponse response = ListGamesResponse.newBuilder().addAllGames(state.getGames()).build();
      responseObserver.onNext(response);
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
      responseObserver.onError(Status.ALREADY_EXISTS.withDescription(e.getMessage()).asRuntimeException());
    } catch (Exception e) {
      responseObserver.onError(Status.UNKNOWN.withDescription(e.getMessage()).withCause(e).asRuntimeException());
    }
  }
}

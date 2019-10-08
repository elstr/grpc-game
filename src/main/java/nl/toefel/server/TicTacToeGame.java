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
    return new StreamObserver<>() {
      @Override
      public void onNext(GameCommand command) {
        System.out.println(command);
        switch (command.getCommandCase()){
          case CHALLENGE_PLAYER:
            GameEvent event = GameEvent.newBuilder()
                .setGameId("1")
                .setType(EventType.START_GAME)
                .setNextPlayer(command.getChallengePlayer().getFromPlayer())
                .setPlayerO(command.getChallengePlayer().getFromPlayer())
                .setPlayerX(command.getChallengePlayer().getToPlayer())
                .setBoard(createEmptyBoard())
                .build();
            responseObserver.onNext(event);
            break;
          case ACCEPT_CHALLENGE:
            break;
          case BOARD_MOVE:
            break;
          case END_GAME:
            break;
          case COMMAND_NOT_SET:
            break;
        }

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

      @Override
      public void onError(Throwable t) {
        t.printStackTrace();
      }

      @Override
      public void onCompleted() {
        System.out.println("on completed");
      }
    };
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

package nl.toefel.tictactoe.client.controller;

import io.grpc.Status;
import nl.toefel.tictactoe.client.view.Modals;

import static nl.toefel.grpc.game.TicTacToeOuterClass.*;

/**
 * Decorator that forwards all methods to the wrapped controller and shows a JavaFX dialog when an exception is caught.
 */
public class DialogOnErrorClientControllerDecorator implements TicTacToeClientController {
  private final TicTacToeClientController controller;

  public DialogOnErrorClientControllerDecorator(TicTacToeClientController controller) {
    this.controller = controller;
  }

  @Override
  public void connectToServer(String host, String port) {
    showDialogOnError(() -> controller.connectToServer(host, port));
  }

  @Override
  public void createPlayer(String playerName) {
    showDialogOnError(() -> controller.createPlayer(playerName));
  }

  @Override
  public void initializeGameStream() {
    showDialogOnError(controller::initializeGameStream);
  }

  @Override
  public void listPlayers() {
   showDialogOnError(controller::listPlayers);
  }

  @Override
  public void startGameAgainstPlayer(Player opponent) {
    showDialogOnError(() -> controller.startGameAgainstPlayer(opponent));
  }

  @Override
  public void makeBoardMove(BoardMove move) {
    showDialogOnError(() -> controller.makeBoardMove(move));
  }

  public void showDialogOnError(Runnable runnable) {
    try {
      runnable.run();
    } catch (Throwable t) {
      // Status gives an idea of the error cause (like UNAVAILABLE or DEADLINE_EXCEEDED)
      // it also contains the underlying exception
      Status status = Status.fromThrowable(t);
      Modals.showGrpcError("Error", status);
    }
  }
}
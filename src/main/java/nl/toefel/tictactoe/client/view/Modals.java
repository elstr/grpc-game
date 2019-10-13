package nl.toefel.tictactoe.client.view;

import io.grpc.Status;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Modals {

  public static void showPopup(String title, String message) {
    Platform.runLater(() -> {
      Stage stage = new Stage();
      stage.setScene(new Scene(new Label(message)));
      stage.setTitle(title);
      stage.initModality(Modality.WINDOW_MODAL);
      stage.show();
    });
  }

  public static void showGrpcError(String title, Status grpcStatus) {
    Platform.runLater(() -> {
      Stage stage = new Stage();
      var msg = "" + grpcStatus.getCode().name() + " " + grpcStatus.getDescription();
      if (grpcStatus.getCause() != null) {
        msg += "\nCause: " + grpcStatus.getCause().getMessage();
      }
      stage.setScene(new Scene(new Label(msg)));
      stage.setTitle(title);
      stage.initModality(Modality.WINDOW_MODAL);
      stage.show();
    });
  }
}

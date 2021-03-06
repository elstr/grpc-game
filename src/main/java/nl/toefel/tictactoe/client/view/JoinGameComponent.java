package nl.toefel.tictactoe.client.view;

import io.grpc.ManagedChannel;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.util.function.Consumer;

import static nl.toefel.grpc.game.TicTacToeOuterClass.Player;

public class JoinGameComponent extends HBox {
    private TextField playerNameTxt = new TextField();
    private Label playerNameLbl = new Label("Player name:");
    private Button createPlayerBtn = new Button("Join game");
    private Label stateLbl = new Label("State: not joined");

    public JoinGameComponent(Consumer<String> joinServerCallback,
                             SimpleObjectProperty<ManagedChannel> grpcConnectionProperty,
                             SimpleObjectProperty<Player> myselfProperty) {

        this.setPadding(new Insets(5.0));
        createPlayerBtn.setOnAction(e -> joinServerCallback.accept(playerNameTxt.getText()));
        createPlayerBtn.setDisable(true);
        setAlignment(Pos.BASELINE_CENTER);
        setSpacing(10.0);

        this.getChildren().addAll(playerNameLbl, playerNameTxt, createPlayerBtn, stateLbl);

        disableOnNoConnectionOrAlreadyJoined(grpcConnectionProperty, myselfProperty);
    }

    private void disableOnNoConnectionOrAlreadyJoined(SimpleObjectProperty<ManagedChannel> grpcConnectionProperty, SimpleObjectProperty<Player> myselfProperty) {
        grpcConnectionProperty.addListener((property, oldConnection, newConnection) -> {
            createPlayerBtn.setDisable(newConnection == null);
        });

        myselfProperty.addListener((property, oldMyself, newMyself) -> {
            createPlayerBtn.setDisable(newMyself != null);
            if (newMyself != null) {
                stateLbl.setText("State: joined as " + newMyself.getName());
            } else {
                stateLbl.setText("State: not joined");
            }
        });
    }
}

package nl.toefel.client.view;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.util.function.Consumer;

public class CreatePlayerComponent extends HBox {
    private TextField playerNameTxt = new TextField();
    private Label playerNameLbl = new Label("Player name:");
    private Button createPlayerBtn = new Button("Join");

    public CreatePlayerComponent(Consumer<String> createPlayerCallback) {
        this.setPadding(new Insets(5.0));
        createPlayerBtn.setOnAction(e -> createPlayerCallback.accept(playerNameTxt.getText()));
        this.getChildren().addAll(playerNameLbl, playerNameTxt, createPlayerBtn);
    }
}

package nl.toefel.client.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import nl.toefel.util.TriConsumer;

import java.util.function.Consumer;

public class JoinGameComponent extends HBox {
    private TextField playerNameTxt = new TextField();
    private TextField serverIpTxt = new TextField("127.0.0.1");
    private TextField serverPortTxt = new TextField("8080");
    private Label playerNameLbl = new Label("Player name:");
    private Label serverIpLbl = new Label("IP:");
    private Label serverPortLbl = new Label("Port:");

    private Button createPlayerBtn = new Button("Join");

    public JoinGameComponent(TriConsumer<String, String, String> joinServerCallback) {
        this.setPadding(new Insets(5.0));
        createPlayerBtn.setOnAction(e -> joinServerCallback.accept(serverIpTxt.getText(), serverPortTxt.getText(), playerNameTxt.getText()));
        setAlignment(Pos.BASELINE_CENTER);
        setSpacing(10.0);
        this.getChildren().addAll(serverIpLbl, serverIpTxt, serverPortLbl, serverPortTxt, playerNameLbl, playerNameTxt, createPlayerBtn);
    }
}

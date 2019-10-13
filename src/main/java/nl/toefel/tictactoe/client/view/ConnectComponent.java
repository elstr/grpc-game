package nl.toefel.tictactoe.client.view;

import io.grpc.ManagedChannel;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.util.function.BiConsumer;

public class ConnectComponent extends HBox {
    private TextField serverIpTxt = new TextField("127.0.0.1");
    private TextField serverPortTxt = new TextField("8080");
    private Label serverIpLbl = new Label("IP:");
    private Label serverPortLbl = new Label("Port:");
    private Button connectBtn = new Button("Connect");
    private Label stateLbl = new Label("State: not connected");

    public ConnectComponent(BiConsumer<String, String> joinServerCallback, SimpleObjectProperty<ManagedChannel> grpcConnectionProperty) {
        this.setPadding(new Insets(5.0));
        connectBtn.setOnAction(e -> joinServerCallback.accept(serverIpTxt.getText(), serverPortTxt.getText()));
        setAlignment(Pos.BASELINE_CENTER);
        setSpacing(10.0);

        this.getChildren().addAll(serverIpLbl, serverIpTxt, serverPortLbl, serverPortTxt, connectBtn, stateLbl);

        grpcConnectionProperty.addListener((property, oldChannel, newChannel) -> {
            if (newChannel != null) {
                updateState(true, "State: " + newChannel.getState(true).name());
            } else {
                updateState(false, "State: not connected");
            }
        });
    }

    private void updateState(boolean disable, String stateLabelValue) {
        stateLbl.setText(stateLabelValue);
        serverIpTxt.setDisable(disable);
        serverPortTxt.setDisable(disable);
        connectBtn.setDisable(disable);
    }
}

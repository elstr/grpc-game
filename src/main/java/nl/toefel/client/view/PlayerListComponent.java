package nl.toefel.client.view;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import static nl.toefel.grpc.game.TicTacToeOuterClass.Player;

public class PlayerListComponent extends VBox {
    private Label playersLbl = new Label("All Players");
    private Button refreshBtn = new Button("Refresh");

    public PlayerListComponent(ObservableList<Player> players, Runnable refreshCallback) {
        this.setPadding(new Insets(5.0));
        refreshBtn.setOnAction(e -> refreshCallback.run());
        ListView<Player> playerList = new ListView<>(players);
        playerList.setOrientation(Orientation.VERTICAL);
        this.getChildren().addAll(refreshBtn, playersLbl, playerList);
    }
}

package nl.toefel.tictactoe.client.view;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;

import static nl.toefel.grpc.game.TicTacToeOuterClass.Player;

public class PlayerListComponent extends VBox {
    private Label playersLbl = new Label("All Players");
    private Button fetchPlayersBtn = new Button("Fetch players");
    private TableView<Player> playersTable;
    private Button startGameBtn = new Button("Start game");
    private SimpleObjectProperty<Player> myselfProperty;

    public PlayerListComponent(ObservableList<Player> players,
                               Runnable fetchPlayersCallback,
                               SimpleObjectProperty<Player> myselfProperty,
                               Consumer<Player> challengePlayerCallback) {
        this.myselfProperty = myselfProperty;
        this.setPadding(new Insets(5.0));
        fetchPlayersBtn.setOnAction(e -> fetchPlayersCallback.run());
        fetchPlayersBtn.setDisable(true);
        playersTable = createPlayerTableView(players);
        playersTable.setDisable(true);
        startGameBtn.setOnAction(e -> challengePlayer(challengePlayerCallback));
        startGameBtn.setDisable(true);
        this.getChildren().addAll(fetchPlayersBtn, playersLbl, playersTable, startGameBtn);
        VBox.setVgrow(playersTable, Priority.ALWAYS);

        enableWhenPlayerHasJoined(myselfProperty);
    }

    private void challengePlayer(Consumer<Player> challengePlayerCallback) {
        Player selectedOpponent = playersTable.getSelectionModel().getSelectedItem();
        Player myself = myselfProperty.get();
        if (myself != null && selectedOpponent != null && String.valueOf(myself.getName()).equals(selectedOpponent.getName())) {
            Modals.showPopup("Error", "You cannot play a game against yourself!\nOpen another window with a different player");
        } else {
            challengePlayerCallback.accept(selectedOpponent);
        }
    }

    private TableView<Player> createPlayerTableView(ObservableList<Player> players) {
        TableView<Player> playerTable = new TableView<>(players);

        var nameColumn = new TableColumn<Player, String>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        var joinTimeColumn = new TableColumn<Player, Long>("Join Time");
        joinTimeColumn.setCellValueFactory(new PropertyValueFactory<>("joinTimestamp"));
        joinTimeColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Player, Long> call(TableColumn<Player, Long> param) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(Long joinTimestamp, boolean empty) {
                        super.updateItem(joinTimestamp, empty);
                        if (empty || joinTimestamp == null) {
                            setText(null);
                        } else {
                            LocalTime time = LocalDateTime
                                .ofInstant(Instant.ofEpochMilli(joinTimestamp), ZoneId.systemDefault())
                                .toLocalTime()
                                .truncatedTo(ChronoUnit.SECONDS);
                            setText(time.toString());
                        }
                    }
                };
            }
        });

        var winsColumn = new TableColumn<Player, String>("Wins");
        winsColumn.setCellValueFactory(new PropertyValueFactory<>("wins"));

        playerTable.getColumns().addAll(nameColumn, joinTimeColumn, winsColumn);
        return playerTable;
    }

    private void enableWhenPlayerHasJoined(SimpleObjectProperty<Player> myselfProperty) {
        myselfProperty.addListener((property, oldMyself, newMyself) -> {
            fetchPlayersBtn.setDisable(newMyself == null);
            playersTable.setDisable(newMyself == null);
            startGameBtn.setDisable(newMyself == null);
        });
    }
}

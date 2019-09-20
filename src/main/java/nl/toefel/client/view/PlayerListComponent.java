package nl.toefel.client.view;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import static nl.toefel.grpc.game.TicTacToeOuterClass.Player;

public class PlayerListComponent extends VBox {
  private Label playersLbl = new Label("All Players");
  private Button refreshBtn = new Button("Refresh");

  public PlayerListComponent(ObservableList<Player> players, Runnable refreshCallback) {
    this.setPadding(new Insets(5.0));
    refreshBtn.setOnAction(e -> refreshCallback.run());
    TableView<Player> playerTable = createPlayerTableView(players);
    this.getChildren().addAll(refreshBtn, playersLbl, playerTable);
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
}

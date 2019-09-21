package nl.toefel.client.view;

import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.util.List;

import static nl.toefel.grpc.game.TicTacToeOuterClass.*;

public class GameComponent extends VBox {
    private Label gameLbl = new Label("Tic Tac Toe game");
    private Label playerXLbl = new Label("Player X: ");
    private Label playerOLbl = new Label("Player Y: ");
    private Label statusLbl = new Label("Status: no game active");
    private GridPane gridPane = new GridPane();

    private SimpleObjectProperty<Player> myselfProperty;

    public GameComponent(SimpleObjectProperty<Player> myselfProperty,
                         SimpleObjectProperty<GameState> gameStateProperty) {
        this.myselfProperty = myselfProperty;
        this.setPadding(new Insets(5.0));
        gridPane.setPadding(new Insets(5.0));
        gridPane.setGridLinesVisible(true);

        gameStateProperty.addListener((property, oldState, newState) -> renderGameState(newState));

        this.getChildren().addAll(gameLbl, playerXLbl, playerOLbl, statusLbl, gridPane);
    }

    private void renderGameState(GameState gameState) {
        renderPlayerNames(gameState);
        renderBoard(gameState);
    }

    private void renderPlayerNames(GameState gameState) {
        playerOLbl.setText("Player O: " + gameState.getPlayerO().getName());
        playerXLbl.setText("Player X: " + gameState.getPlayerX().getName());
        String status = "Status: " + gameState.getStatus();
        Player nextPlayer = gameState.getNextPlayer();
        if (nextPlayer != null) {
            status += ", next player: " + nextPlayer.getName();
            Player myself = myselfProperty.get();
            if (myself != null && String.valueOf(myself.getName()).equals(nextPlayer.getName())) {
                status += "(YOU)";
            }
        }
        statusLbl.setText(status);
    }

    private void renderBoard(GameState gameState) {
        gridPane.getChildren().clear();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                String cell = getCellFromState(gameState, col, row);
                switch (cell) {
                    case "X":
                    case "x":
                        gridPane.add(newCross(), col, row);
                        break;
                    case "O":
                    case "o":
                        gridPane.add(newCircle(), col, row);
                        break;
                    default:
                        gridPane.add(newPlaceholder(col, row), col, row);
                }
            }
        }
    }

    private String getCellFromState(GameState gameState, int col, int row) {
        List<BoardRow> rows = gameState.getBoard().getRowsList();
        if (row > rows.size()) {
            return " ";
        }
        List<String> columns = rows.get(row).getColumnsList();
        if (col > columns.size()) {
            return " ";
        }
        return columns.get(col);
    }

    private Group newCross() {
        Line line1 = new Line(0.0, 0.0, 120.0, 120.0);
        Line line2 = new Line(0.0, 120.0, 120.0, 0.0);
        line1.setStrokeWidth(3.0);
        line2.setStrokeWidth(3.0);
        return new Group(line1, line2);
    }

    private Circle newCircle() {
        Circle circle = new Circle(60);
        circle.setStrokeWidth(3.0);
        circle.setStroke(Color.BLACK);
        circle.setFill(Color.TRANSPARENT);
        return circle;
    }

    private Rectangle newPlaceholder(int col, int row) {
        Rectangle rectangle = new Rectangle(120, 120);
        rectangle.setFill(Color.TRANSPARENT);
        rectangle.setOnMouseClicked(event -> {
            Modals.showPopup("Clicked", "Clicked on column " + col + ", row " + row);
        });
        return rectangle;
    }

}

package nl.toefel.tictactoe.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import nl.toefel.tictactoe.client.controller.Controller;
import nl.toefel.tictactoe.client.controller.ErrorDialogDecorator;
import nl.toefel.tictactoe.client.controller.GrpcController;
import nl.toefel.tictactoe.client.state.ClientState;
import nl.toefel.tictactoe.client.view.ConnectComponent;
import nl.toefel.tictactoe.client.view.GamesTabComponent;
import nl.toefel.tictactoe.client.view.JoinGameComponent;
import nl.toefel.tictactoe.client.view.PlayerListComponent;

public class GameWindow extends Application {

    private ClientState state;
    private Controller controller;

    public GameWindow() {
        state = new ClientState();
        controller = new ErrorDialogDecorator(new GrpcController(state));
    }

    @Override
    public void start(Stage stage) {
        System.out.println(System.getProperty("java.version"));
        System.out.println(System.getProperty("javafx.version"));

        ConnectComponent connectComponent = new ConnectComponent(controller::connectToServer, state.getGrpcConnectionProperty());
        JoinGameComponent joinGameComponent = new JoinGameComponent(controller::joinGame, state.getGrpcConnectionProperty(), state.getMyselfProperty());
        PlayerListComponent playerListComponent = new PlayerListComponent(state.getPlayers(), controller::listPlayers, state.getMyselfProperty(), controller::startGameAgainstPlayer);
        GamesTabComponent gameTabs = new GamesTabComponent(state.getMyselfProperty(), state.getGameStates(), controller::makeBoardMove);

        HBox listAndGameLayout = new HBox(playerListComponent, gameTabs);
        HBox.setHgrow(playerListComponent, Priority.ALWAYS);
        HBox.setHgrow(gameTabs, Priority.ALWAYS);

        VBox mainLayout = new VBox(connectComponent, joinGameComponent, listAndGameLayout);
        VBox.setVgrow(listAndGameLayout, Priority.ALWAYS);

        Scene scene = new Scene(mainLayout, 1024, 800);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}
package nl.toefel.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import nl.toefel.client.controller.GrpcController;
import nl.toefel.client.state.ClientState;
import nl.toefel.client.view.ConnectComponent;
import nl.toefel.client.view.GamesTabComponent;
import nl.toefel.client.view.JoinGameComponent;
import nl.toefel.client.view.PlayerListComponent;

public class GameWindow extends Application {

    private ClientState state;
    private GrpcController controller;

    public GameWindow() {
        state = new ClientState();
        controller = new GrpcController(state);
    }

    @Override
    public void start(Stage stage) {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        System.out.println(javaVersion);
        System.out.println(javafxVersion);

        ConnectComponent connectComponent = new ConnectComponent(controller::connectToServer, state.getGrpcConnectionProperty());
        JoinGameComponent joinGameComponent = new JoinGameComponent(controller::createPlayer, state.getGrpcConnectionProperty(), state.getMyselfProperty());
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
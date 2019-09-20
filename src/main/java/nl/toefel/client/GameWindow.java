package nl.toefel.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import nl.toefel.client.controller.GrpcController;
import nl.toefel.client.state.ClientState;
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

        JoinGameComponent joinGameComponent = new JoinGameComponent(controller::joinServer);
        PlayerListComponent playerListComponent = new PlayerListComponent(state.getPlayers(), controller::listPlayers);

        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(joinGameComponent);
        mainLayout.setLeft(playerListComponent);

        Scene scene = new Scene(mainLayout, 1024, 800);
        stage.setScene(scene);
        stage.show();
    }

    public void load

    public static void main(String[] args) {
        launch();
    }

}
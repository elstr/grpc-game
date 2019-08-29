package nl.toefel.client;

import io.grpc.ManagedChannel;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import nl.toefel.client.controller.GrpcController;
import nl.toefel.client.state.UIGameState;
import nl.toefel.client.view.CreatePlayerComponent;
import nl.toefel.client.view.PlayerListComponent;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static nl.toefel.grpc.game.TicTacToeGrpc.TicTacToeFutureStub;

public class GameWindow extends Application {

    private Executor executor = Executors.newWorkStealingPool();
    private ManagedChannel channel;
    private TicTacToeFutureStub ticTacToeClient;
    private UIGameState state;
    private GrpcController controller;

    public GameWindow() {
        state = new UIGameState();
        controller = new GrpcController(state);
    }

    @Override
    public void init() throws Exception {
        controller.connect("localhost", 8080);
    }

    @Override
    public void start(Stage stage) {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        System.out.println(javaVersion);
        System.out.println(javafxVersion);

        CreatePlayerComponent createPlayerComponent = new CreatePlayerComponent(controller::createPlayer);
        PlayerListComponent playerListComponent = new PlayerListComponent(state.getPlayers(), controller::listPlayers);

        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(createPlayerComponent);
        mainLayout.setLeft(playerListComponent);

        Scene scene = new Scene(mainLayout, 1024, 800);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}
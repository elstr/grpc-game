package nl.toefel.application;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import nl.toefel.grpc.game.TicTacToeGrpc;
import nl.toefel.grpc.game.TicTacToeGrpc.*;
import nl.toefel.grpc.game.TicTacToeOuterClass;
import nl.toefel.grpc.game.TicTacToeOuterClass.*;

import java.util.stream.Collectors;

public class GameWindow extends Application {

    private ManagedChannel channel;
    TicTacToeBlockingStub ticTacToeClient;
    @Override
    public void init() throws Exception {
        channel = ManagedChannelBuilder
            .forAddress("localhost", 8080)
            .usePlaintext()
            .build();
        ticTacToeClient = TicTacToeGrpc.newBlockingStub(channel);
    }

    @Override
    public void start(Stage stage) {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        System.out.println(javaVersion);
        System.out.println(javafxVersion);
        Label players = new Label("No players fetched.");
        Button listPlayersButton = new Button("List Players");
        listPlayersButton.setOnAction(e -> {
            players.setText("Fetching ...");

            ListPlayersRequest listPlayersRequest = ListPlayersRequest.newBuilder().build();
            ListPlayersResponse listPlayersResponse = ticTacToeClient.listPlayers(listPlayersRequest);

            String playersJoined = listPlayersResponse.getPlayersList().stream()
                .map(Player::getName)
                .collect(Collectors.joining("\n"));

            players.setText("Players: \n" + playersJoined);
        });
        Scene scene = new Scene(new FlowPane(Orientation.VERTICAL, players, listPlayersButton), 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}
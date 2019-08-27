package nl.toefel.application;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import nl.toefel.grpc.game.TicTacToeOuterClass.ListPlayersRequest;
import nl.toefel.grpc.game.TicTacToeOuterClass.ListPlayersResponse;
import nl.toefel.grpc.game.TicTacToeOuterClass.Player;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static nl.toefel.grpc.game.TicTacToeGrpc.TicTacToeFutureStub;
import static nl.toefel.grpc.game.TicTacToeGrpc.newFutureStub;

public class GameWindow extends Application {

    private Executor executor = Executors.newWorkStealingPool();
    private ManagedChannel channel;
    private TicTacToeFutureStub ticTacToeClient;
    private Label playersLabel;

    @Override
    public void init() throws Exception {
        channel = ManagedChannelBuilder
            .forAddress("localhost", 8080)
            .usePlaintext()
            .build();
        ticTacToeClient = newFutureStub(channel);
    }

    @Override
    public void start(Stage stage) {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        System.out.println(javaVersion);
        System.out.println(javafxVersion);
        playersLabel = new Label("No players fetched.");
        Button listPlayersButton = new Button("List Players");
        listPlayersButton.setOnAction(this::listPlayers);
        Scene scene = new Scene(new FlowPane(Orientation.VERTICAL, playersLabel, listPlayersButton), 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    private void listPlayers(ActionEvent actionEvent) {
        playersLabel.setText("Fetching ...");
        ListPlayersRequest listPlayersRequest = ListPlayersRequest.newBuilder().build();
        ListenableFuture<ListPlayersResponse> listPlayersFuture = ticTacToeClient.listPlayers(listPlayersRequest);
        Futures.addCallback(listPlayersFuture, new FutureCallback<>() {
            @Override
            public void onSuccess(@NullableDecl ListPlayersResponse result) {
                String players = result.getPlayersList()
                    .stream()
                    .map(Player::getName)
                    .collect(Collectors.joining("\n"));

                Platform.runLater(() -> playersLabel.setText("Players: \n" + players));
            }

            @Override
            public void onFailure(Throwable t) {
                Platform.runLater(() -> playersLabel.setText("Error: " + t.getMessage()));

            }
        }, executor);
    }

    public static void main(String[] args) {
        launch();
    }

}
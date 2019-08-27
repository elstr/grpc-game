package nl.toefel.application;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.sun.javafx.collections.ObservableListWrapper;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import nl.toefel.application.components.CreatePlayerComponent;
import nl.toefel.application.components.PlayerListComponent;
import nl.toefel.grpc.game.TicTacToeOuterClass.ListPlayersRequest;
import nl.toefel.grpc.game.TicTacToeOuterClass.ListPlayersResponse;
import nl.toefel.grpc.game.TicTacToeOuterClass.Player;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static nl.toefel.grpc.game.TicTacToeGrpc.TicTacToeFutureStub;
import static nl.toefel.grpc.game.TicTacToeGrpc.newFutureStub;

public class GameWindow extends Application {

    private Executor executor = Executors.newWorkStealingPool();
    private ManagedChannel channel;
    private TicTacToeFutureStub ticTacToeClient;
    private ObservableList<Player> players = new ObservableListWrapper<>(new ArrayList<>());

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

        CreatePlayerComponent createPlayerComponent = new CreatePlayerComponent(this::createPlayer);
        PlayerListComponent playerListComponent = new PlayerListComponent(players, this::listPlayers);

        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(createPlayerComponent);
        mainLayout.setLeft(playerListComponent);

        Scene scene = new Scene(mainLayout, 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    private void createPlayer(String playerName) {
        System.out.println("Creating player " + playerName);
    }

    private void listPlayers() {
        ListPlayersRequest listPlayersRequest = ListPlayersRequest.newBuilder().build();
        ListenableFuture<ListPlayersResponse> listPlayersFuture = ticTacToeClient.listPlayers(listPlayersRequest);
        Futures.addCallback(listPlayersFuture, new FutureCallback<>() {
            @Override
            public void onSuccess(@NullableDecl ListPlayersResponse result) {
                Platform.runLater(() -> {
                    players.clear();
                    players.addAll(result.getPlayersList());
                });
            }

            @Override
            public void onFailure(Throwable t) {
                Platform.runLater(() -> {
                    players.clear();
                    Stage stage = new Stage();
                    stage.setScene(new Scene(new Label("Error: " + t.getMessage())));
                    stage.setTitle("My modal window");
                    stage.initModality(Modality.WINDOW_MODAL);
                    stage.show();
                });
            }
        }, executor);
    }

    public static void main(String[] args) {
        launch();
    }

}
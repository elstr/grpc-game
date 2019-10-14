package nl.toefel.tictactoe.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import nl.toefel.tictactoe.client.controller.DialogOnErrorClientControllerDecorator;
import nl.toefel.tictactoe.client.controller.TicTacToeClientController;
import nl.toefel.tictactoe.client.controller.GrpcTicTacToeClientController;
import nl.toefel.tictactoe.client.state.ClientState;
import nl.toefel.tictactoe.client.view.ConnectComponent;
import nl.toefel.tictactoe.client.view.GamesTabComponent;
import nl.toefel.tictactoe.client.view.JoinGameComponent;
import nl.toefel.tictactoe.client.view.PlayerListComponent;

public class GameWindow extends Application {

    private ClientState state;
    private TicTacToeClientController ticTacToeClient;

    public GameWindow() {
        state = new ClientState();
        ticTacToeClient = new DialogOnErrorClientControllerDecorator(new GrpcTicTacToeClientController(state));
    }

    @Override
    public void start(Stage stage) {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        System.out.println(javaVersion);
        System.out.println(javafxVersion);

        ConnectComponent connectComponent = new ConnectComponent(ticTacToeClient::connectToServer, state.getGrpcConnectionProperty());
        JoinGameComponent joinGameComponent = new JoinGameComponent(ticTacToeClient::createPlayer, state.getGrpcConnectionProperty(), state.getMyselfProperty());
        PlayerListComponent playerListComponent = new PlayerListComponent(state.getPlayers(), ticTacToeClient::listPlayers, state.getMyselfProperty(), ticTacToeClient::startGameAgainstPlayer);
        GamesTabComponent gameTabs = new GamesTabComponent(state.getMyselfProperty(), state.getGameStates(), ticTacToeClient::makeBoardMove);

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
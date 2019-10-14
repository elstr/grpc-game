package nl.toefel.tictactoe.client.controller;

import nl.toefel.grpc.game.TicTacToeOuterClass;

public interface TicTacToeClientController {
  void connectToServer(String host, String port);

  void createPlayer(String playerName);

  void initializeGameStream();

  void listPlayers();

  void startGameAgainstPlayer(TicTacToeOuterClass.Player opponent);

  void makeBoardMove(TicTacToeOuterClass.BoardMove move);
}

package nl.toefel.tictactoe.client.controller;

import nl.toefel.grpc.game.TicTacToeOuterClass;

public interface Controller {
  void connectToServer(String host, String port);

  void joinGame(String playerName);

  void initializeGameStream();

  void listPlayers();

  void startGameAgainstPlayer(TicTacToeOuterClass.Player opponent);

  void makeBoardMove(TicTacToeOuterClass.BoardMove move);
}

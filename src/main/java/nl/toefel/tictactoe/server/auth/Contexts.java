package nl.toefel.tictactoe.server.auth;

import io.grpc.Context;

public class Contexts {

  public static final Context.Key<String> PLAYER_ID = Context.key("playerid");

}

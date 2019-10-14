package nl.toefel.tictactoe.server.auth;

import io.grpc.Context;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;

/**
 * Reads the playerid metadata and sets it onto the context. Metadata is not available available in the call handlers in
 * the TicTacToeServerController, but Context is!
 */
public class PlayerIdInterceptor implements ServerInterceptor {
  public static final Metadata.Key<String> PLAYER_ID = Metadata.Key.of("playerid", Metadata.ASCII_STRING_MARSHALLER);

  @Override
  public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
    String playerId = headers.get(PLAYER_ID);

    if (call.getMethodDescriptor().getFullMethodName().contains("PlayGame") && playerId == null) {
      call.close(Status.UNAUTHENTICATED.withDescription("PlayGame requires that you provide a player id using the PlayerIdCallCredentials"), headers);
      return new ServerCall.Listener<>() {
      };
    }

    // Add the playerid from the headers/metadata to the context.
    // Headers/metadata are not available in the call, but Context can be retrieved!
    Context contextWithPlayerId = Context.current().withValue(Contexts.PLAYER_ID, playerId);
    return io.grpc.Contexts.interceptCall(contextWithPlayerId, call, headers, next);
  }
}

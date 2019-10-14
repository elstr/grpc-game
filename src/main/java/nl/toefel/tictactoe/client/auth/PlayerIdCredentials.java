package nl.toefel.tictactoe.client.auth;

import io.grpc.CallCredentials;
import io.grpc.Metadata;
import io.grpc.Status;

import java.util.concurrent.Executor;

/**
 * Sends the player name in the auth header as identification.
 */
public class PlayerIdCredentials extends CallCredentials {

  public static final Metadata.Key<String> PLAYER_ID = Metadata.Key.of("playerid", Metadata.ASCII_STRING_MARSHALLER);

  private String playerId;

  public PlayerIdCredentials(String playerId) {
    this.playerId = playerId;
  }

  @Override
  public void applyRequestMetadata(RequestInfo requestInfo, Executor appExecutor, MetadataApplier applier) {
    appExecutor.execute(() -> {
      try {
        Metadata headers = new Metadata();
        headers.put(PLAYER_ID, playerId);
        applier.apply(headers);
      } catch (Throwable e) {
        applier.fail(Status.UNAUTHENTICATED.withCause(e));
      }
    });
  }

  @Override
  public void thisUsesUnstableApi() {

  }
}

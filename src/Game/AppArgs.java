package Game;

public class ClientArgs {
    public final Mode mode;
    public final int port;
    public final String mapFile;
    public final String host; // null dla client

    public enum Mode { SERVER, CLIENT }

    public ClientArgs(Mode mode, int port, String mapFile, String host) {
        this.mode = mode;
        this.port = port;
        this.mapFile = mapFile;
        this.host = host;
    }
}

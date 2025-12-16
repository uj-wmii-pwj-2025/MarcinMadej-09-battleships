package Game;

public record AppArgs(Mode mode, int port, String mapFile, String host) {
    public enum Mode {SERVER, CLIENT}

}

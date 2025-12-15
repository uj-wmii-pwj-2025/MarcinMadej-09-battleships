package Game;

@FunctionalInterface
public interface Command {
    void exec(Game instance);
}

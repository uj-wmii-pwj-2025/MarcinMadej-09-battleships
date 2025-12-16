package Game;

@FunctionalInterface
public interface StartGameMode {
    void start(Game instance, AppArgs args);
}

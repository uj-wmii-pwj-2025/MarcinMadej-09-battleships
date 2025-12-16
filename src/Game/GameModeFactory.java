package Game;

public class GameModeFactory {

    public static StartGameMode create(AppArgs args) {
        return switch (args.mode()) {
            case SERVER -> serverCommand();
            case CLIENT -> clientCommand();
        };
    }

    private static StartGameMode clientCommand() {
        return (ctx, args) -> {
            System.out.println("CLIENT MODE");
            System.out.println("host = " + args.host());
            System.out.println("port = " + args.port());
            System.out.println("map = " + args.mapFile());
            ctx.startClient(args.host(), args.port(), args.mapFile());
        };
    }

    private static StartGameMode serverCommand() {
        return (ctx, args) -> {
            System.out.println("SERVER MODE");
            System.out.println("port = " + args.port());
            System.out.println("map = " + args.mapFile());
            ctx.startServer(args.port(), args.mapFile());
        };
    }

}

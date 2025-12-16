package Game;

public class ArgumentsParser {

    public static AppArgs parse(String[] args) {
        String mode = null;
        String map = null;
        String host = null;
        Integer port = null;

        for (int i = 0; i < args.length; i += 2) {
            switch (args[i]) {
                case "-mode":
                    mode = args[i + 1];
                    break;
                case "-port":
                    port = Integer.parseInt(args[i + 1]);
                    break;
                case "-map":
                    map = args[i + 1];
                    break;
                case "-host":
                    host = args[i + 1];
                    break;
                default:
                    throw new IllegalArgumentException("Unknown flag: " + args[i]);
            }
        }

        if (mode == null || port == null || map == null) {
            throw new IllegalArgumentException("Lack of arguments");
        }

        AppArgs.Mode parsedMode =
                mode.equals("server") ? AppArgs.Mode.SERVER :
                        mode.equals("client") ? AppArgs.Mode.CLIENT :
                                throwMode();

        if (parsedMode == AppArgs.Mode.CLIENT && host == null) {
            throw new IllegalArgumentException("Client needs flag -host");
        }

        if (parsedMode == AppArgs.Mode.SERVER && host != null) {
            throw new IllegalArgumentException("Use -host only for client mode");
        }

        return new AppArgs(parsedMode, port, map, host);
    }

    private static AppArgs.Mode throwMode() {
        throw new IllegalArgumentException("Unknown mode");
    }
}

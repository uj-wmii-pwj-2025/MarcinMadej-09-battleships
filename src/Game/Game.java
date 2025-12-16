package Game;

import Client.Client;
import Server.Server;

public class Game {
    public void main(String[] args) {
        TitlePrinter.printTitle();
        int version = Runtime.version().feature();
        if (version < 21) {
            System.out.println("Detected java version older than 21.\n App was not tested on it!\n");
        }

        AppArgs parsed = ArgumentsParser.parse(args);
        StartGameMode gameMode = GameModeFactory.create(parsed);
        gameMode.start(this, parsed);
    }

    public void startServer(int port, String mapFile) {
        Server server = new Server();
        server.serverMain(port, mapFile);
    }

    public void startClient(String host, int port, String mapFile) {
        Client client = new Client();
        client.clientMain(host, port, mapFile);
    }
}

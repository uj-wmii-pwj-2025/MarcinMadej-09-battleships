package Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import static ShipsGenerator.GenerateShips.generateToFile;

public class Server {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private static String MapFolder;
    private final String MapFileName = "hostMap.txt";
    private char[][] HostMap = new char[10][10];
    private final Scanner ReadConsole = new Scanner(System.in);

    public void serverMain(int port, String mapFile) {
        Server server = new Server();
        Server.MapFolder = mapFile;
        try {
            server.startServer(port);
            generateToFile(mapFile, MapFileName);
            server.runServerLogic(server);
        } finally {
            server.stopServer();
        }
    }

    public void runServerLogic(Server server){
        server.readMap();
        boolean isStarted = false;
        String command = null;
        int row = 0;
        int col = 0;
        String enemyResult = null;
        System.out.println("Waiting for client to start...");
        try {
            while(true){
                if(!isStarted){
                    while(true){
                        String received = in.readLine();
                        System.out.println("Client says: " + received);
                        String[] p = received.split(";", -1);
                        command = p[0].trim().toLowerCase();
                        col = p[1].trim().toLowerCase().charAt(0) - 97;
                        row = Integer.parseInt(p[1].trim().toLowerCase().substring(1)) - 1;
                        if(!command.equalsIgnoreCase("start")) {
                            out.println("Please, start the game first");
                            continue;
                        }
                        if (row < 0 || row > 9 || col < 0 || col > 9) {
                            out.println("Index out of bonds");
                            continue;
                        }
                        isStarted = true;
                        break;
                    }
                    String enemyShoot = shootLoader(col,row);
                    System.out.println("Enemy: " + enemyShoot + " " + col + " " + row);
                    enemyResult = enemyShoot;
                } else {
                    String recieved = in.readLine();
                    System.out.println("Client says: " + recieved);
                    String[] p = recieved.split(";", 2);
                    col = p[1].trim().toLowerCase().charAt(0) - 97;
                    row = Integer.parseInt(p[1].trim().toLowerCase().substring(1)) - 1;
                    if (row < 0 || row > 9 || col < 0 || col > 9) {
                        out.println("Index out of bonds");
                        continue;
                    }
                    String enemyShoot = shootLoader(col,row);
                    System.out.println("Enemy: " + enemyShoot + " " + col + " " + row);
                    enemyResult = enemyShoot;
                }
                System.out.println("Write coordinates:\n");
                String coordinates = ReadConsole.nextLine().trim().toUpperCase();
                out.println(enemyResult + ";" + coordinates);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public String shootLoader(int col, int row){
        char shootingPlace = HostMap[col][row];
        if(shootingPlace == '~' || shootingPlace == '.'){
            return "pudło";
        } else {
            return "trafiony";
        }
    }

    public void readMap(){
        Path mapPath = Paths.get(MapFolder + "\\" +  MapFileName);
        String mapString;
        try {
            System.out.println(mapPath.toString());
            mapString = Files.readString(mapPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int i = 0;
        int j = -1;
        for( char character : mapString.toCharArray() ){
            if(i == 10 && j == 10){
                break;
            }
            if(i == 0){
                ++j;
            }
            HostMap[i][j] = character;
            ++i;
            i = i % 10;
        }
    };

    public void startServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            clientSocket = serverSocket.accept();
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String greeting = in.readLine();
            if ("hello server".equals(greeting)) {
                out.println("hello client");
            } else {
                out.println("unrecognised greeting");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String sendMessage(String msg) {
        try {
            out.println(msg);
            return in.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void stopServer() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();
            if (serverSocket != null) serverSocket.close();
        } catch (IOException ignored) {}
    }
}

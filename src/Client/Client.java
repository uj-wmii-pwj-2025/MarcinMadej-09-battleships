package Client;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Scanner;

import static ShipsGenerator.GenerateShips.generateToFile;

public class Client {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private static String MapFolder;
    private final String MapFileName = "clientMap.txt";
    private char[][] ClientMap = new char[10][10];
    private final Scanner ReadConsole = new Scanner(System.in);
    private int shipsAlive = 1;

    public void clientMain(String host, int port, String mapFile){
        Client client = new Client();
        Client.MapFolder = mapFile;
        try {
            client.startClient(host, port);
            String response = client.sendMessage("hello server");
            System.out.println(response);
            System.out.println(MapFolder);
            generateToFile(mapFile, MapFileName);
            client.runClientLogic(client);
        } finally {
            stopClient();
        }
    }

    public void runClientLogic(Client client){
        client.readMap();
        String command = null;
        int row = 0;
        int col = 0;
        String enemyResult = null;
        try {
            while (true) {
                System.out.println("Write - start - to start the game");
                if(ReadConsole.nextLine().trim().equalsIgnoreCase("start")){
                    command = "start";
                    break;
                }
            }
            while (true) {
                if(command.equalsIgnoreCase("ostatni zatopiony")){
                    out.println(command + ";A1");
                    System.exit(0);
                }
                System.out.println("Write coordinates:\n");
                String coordinates = ReadConsole.nextLine().trim().toUpperCase();
                out.println(command + ";" + coordinates);
                while(true) {
                    String recieved = in.readLine();
                    if(recieved.equalsIgnoreCase("ostatni zatopiony")){
                        System.out.println("Wygrana");
                        System.exit(0);
                    }
                    System.out.println("Server says: " + recieved);
                    String[] p = recieved.split(";", 2);
                    col = p[1].trim().toLowerCase().charAt(0) - 97;
                    row = Integer.parseInt(p[1].trim().toLowerCase().substring(1)) - 1;
                    if (row < 0 || row > 9 || col < 0 || col > 9) {
                        out.println("Index out of bonds");
                        continue;
                    }
                    String enemyShoot = shootLoader(col,row);
                    System.out.println("Enemy: " + enemyShoot + " " + col + " " + row);;
                    command = enemyShoot;
                    break;
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String shootLoader(int col, int row){
        char shootingPlace = ClientMap[col][row];
        if(shootingPlace == '~' || shootingPlace == '.'){
            ClientMap[col][row] = '~';
            return "pudło";
        } else {
            ClientMap[col][row] = '@';
            if(isShipSunk(col, row)){
                --shipsAlive;
                if(shipsAlive == 0){
                    System.out.println("Przegrana");
                    return "ostatni zatopiony";
                }
                return "trafiony zatopiony";
            }
            return "trafiony";
        }
    }

    private boolean isShipSunk(int col, int row) {
        boolean[][] visited = new boolean[10][10];
        return checkVerticalSunk(col, row, visited);
    }

    private boolean checkVerticalSunk(int col, int row, boolean[][] visited) {
        if (col < 0 || col > 9 || row < 0 || row > 9) {
            return true;
        }
        char fieldOnCoords = ClientMap[col][row];

        if (visited[col][row] || fieldOnCoords == '~' || fieldOnCoords == '.') {
            return true;
        }

        visited[col][row] = true;

        if (fieldOnCoords == '#') {
            return false;
        }

        if (fieldOnCoords == '@') {
            if (!checkVerticalSunk(col, row - 1, visited)) return false;
            if (!checkVerticalSunk(col, row + 1, visited)) return false;
            if (!checkVerticalSunk(col + 1, row, visited)) return false;
            if (!checkVerticalSunk(col - 1, row, visited)) return false;
            return true;
        }

        return true;
    }

    public void readMap(){
        System.out.println(MapFolder);
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
            if(i == 9 && j == 9){
                break;
            }
            if(i == 0){
                ++j;
            }
            ClientMap[j][i] = character;
            ++i;
            i = i % 10;
        }
    };

    public void startClient(String host, int port){
        try {
            clientSocket = new Socket(host, port);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
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

    public void stopClient() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException ignored) {}
    }

}

package Client;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;

import static ShipsGenerator.GenerateShips.generateToFile;

public class Client {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private static String MapFolder;
    private final String MapFileName = "clientMap.txt";
    private char[][] ClientMap = new char[10][10];
    private char[][] EnemyMap = new char[10][10];
    private final Scanner ReadConsole = new Scanner(System.in);
    private int shipsAlive = 10;

    public void clientMain(String host, int port, String mapFile){
        Client client = new Client();
        Client.MapFolder = mapFile;
        try {
            client.startClient(host, port);
            generateToFile(mapFile, MapFileName);
            client.runClientLogic(client);
        } finally {
            client.stopClient();
        }
    }

    public void runClientLogic(Client client){
        client.readMap();
        for(char[] row : EnemyMap) {
            Arrays.fill(row, '.');
        }
        String command = null;
        int row = 0;
        int col = 0;
        int errorCount = 0;
        String p[];
        String myLastShot = "";
        String myLastMsg = "";
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
                    return;
                }
                System.out.println("Write coordinates:\n");
                String coordinates = ReadConsole.nextLine().trim().toUpperCase();
                myLastShot = coordinates.trim().toLowerCase();
                myLastMsg = command + ";" + coordinates;
                out.println(myLastMsg);
                errorCount = 0;
                while(true) {
                    String recieved = in.readLine();
                    System.out.println("Server says: " + recieved);
                    try {
                        p = recieved.split(";", 2);
                        if (p[0].trim().equalsIgnoreCase("ostatni zatopiony")) {
                            System.out.println("Wygrana");
                            return;
                        }
                        col = p[1].trim().toLowerCase().charAt(0) - 97;
                        row = Integer.parseInt(p[1].trim().toLowerCase().substring(1)) - 1;
                    } catch (Exception ignore) {
                        out.println(myLastMsg);
                        ++errorCount;
                        checkErrCount(errorCount);
                        continue;
                    }
                    if (row < 0 || row > 9 || col < 0 || col > 9) {
                        out.println(myLastMsg);
                        ++errorCount;
                        checkErrCount(errorCount);
                        continue;
                    }
                    int myLastShotCol = -1;
                    int myLastShotRow = -1;
                    try {
                        myLastShotCol = myLastShot.charAt(0) - 97;
                        myLastShotRow = Integer.parseInt(myLastShot.substring(1)) - 1;
                        if(!updateEnemyMap(myLastShotCol, myLastShotRow, p[0].trim().toLowerCase())){
                            out.println(myLastMsg);
                            ++errorCount;
                            checkErrCount(errorCount);
                            continue;
                        }
                    } catch (Exception ignore) {}
                    String enemyShoot = shootLoader(col,row);
                    printEnemyMap();
                    printClientMap();
                    System.out.println("Enemy shot: " + String.valueOf((char) (col + 97)).toUpperCase() + (row+1) + " Result: " + enemyShoot);
                    command = enemyShoot;
                    break;
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void printEnemyMap() {
        System.out.println("Enemy Map:");
        for (char[] chars : EnemyMap) {
            for (char aChar : chars) {
                System.out.print(aChar + " ");
            }
            System.out.println();
        }
    }

    public void printClientMap() {
        System.out.println("My Map:");
        for (char[] chars : ClientMap) {
            for (char aChar : chars) {
                System.out.print(aChar + " ");
            }
            System.out.println();
        }
    }

    public boolean updateEnemyMap(int col, int row, String result){
        if(result.equalsIgnoreCase("pudło")){
            EnemyMap[col][row] = '?';
            return true;
        } else if (result.equalsIgnoreCase("trafiony") || result.equalsIgnoreCase("trafiony zatopiony") || result.equalsIgnoreCase("ostatni zatopiony")) {
            EnemyMap[col][row] = '#';
            return true;
        } else if (col == -1 || row == -1) {
            return true;
        }
        return false;
    }

    public void checkErrCount(int errorCount){
        if(errorCount >= 3){
            System.out.println("Błąd komunikacji");
            this.stopClient();
        }
    }

    public String shootLoader(int col, int row){
        char shootingPlace = ClientMap[col][row];
        if(shootingPlace == '~' || shootingPlace == '.'){
            ClientMap[col][row] = '~';
            return "pudło";
        } else {
            if(shootingPlace == '@'){
                if(isShipSunk(col, row)){
                    return "trafiony zatopiony";
                }
                return "trafiony";
            }
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
            if(i == 10 && j == 9){
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

    public void stopClient() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException ignored) {}
    }

}

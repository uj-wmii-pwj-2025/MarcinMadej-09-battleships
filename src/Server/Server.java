package Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
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
    private char[][] EnemyMap = new char[10][10];
    private final Scanner ReadConsole = new Scanner(System.in);
    private int shipsAlive = 10;

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
        for(char[] row : EnemyMap) {
            Arrays.fill(row, '.');
        }

        boolean isStarted = false;
        String command = null;
        int row = 0;
        int col = 0;
        int errorCount = 0;
        String enemyResult = null;
        String p[];
        String myLastShot = "";
        String myLastMsg = "";
        System.out.println("Waiting for client to start...");
        try {
            while(true){
                if(!isStarted){
                    while(true){
                        String received = in.readLine();
                        System.out.println("Client says: " + received);
                        try {
                        p = received.split(";", -1);
                        command = p[0].trim().toLowerCase();
                        col = p[1].trim().toLowerCase().charAt(0) - 97;
                        row = Integer.parseInt(p[1].trim().toLowerCase().substring(1)) - 1;
                        } catch (Exception ignore) {
                            out.println(myLastMsg);
                            ++errorCount;
                            checkErrCount(errorCount);
                            continue;
                        }
                        if(!command.equalsIgnoreCase("start")) {
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
                        isStarted = true;
                        break;
                    }
                    String enemyShoot = shootLoader(col,row);
                    printEnemyMap();
                    printHostMap();
                    System.out.println("Enemy shot: " + String.valueOf((char) (col + 97)).toUpperCase() + (row+1) + " Result: " + enemyShoot);
                    enemyResult = enemyShoot;
                } else {
                    String recieved = in.readLine();
                    System.out.println("Client says: " + recieved);
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
                    try {
                        int myLastShotCol = myLastShot.charAt(0) - 97;
                        int myLastShotRow = Integer.parseInt(myLastShot.substring(1)) - 1;
                        if (!updateEnemyMap(myLastShotCol, myLastShotRow, p[0].trim().toLowerCase())) {
                            out.println(myLastMsg);
                            ++errorCount;
                            checkErrCount(errorCount);
                            continue;
                        }
                    } catch (Exception ignore) {}
                    String enemyShoot = shootLoader(col,row);
                    printEnemyMap();
                    printHostMap();
                    System.out.println("Enemy shot: " + String.valueOf((char) (col + 97)).toUpperCase() + (row+1) + " Result: " + enemyShoot);
                    enemyResult = enemyShoot;
                }
                if(Objects.equals(enemyResult, "ostatni zatopiony")){
                    out.println(enemyResult + ";A1");
                    return;
                }
                System.out.println("Write coordinates:\n");
                String coordinates = ReadConsole.nextLine().trim().toUpperCase();
                myLastShot = coordinates.toLowerCase().trim();
                myLastMsg = enemyResult + ";" + coordinates;
                out.println(myLastMsg);
                errorCount = 0;

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

    public void printHostMap() {
        System.out.println("My Map:");
        for (char[] chars : HostMap) {
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
        } else if (result.equalsIgnoreCase("start")) {
            return true;
        }
        return false;
    }

    public void checkErrCount(int errorCount){
        if(errorCount >= 3){
            System.out.println("Błąd komunikacji");
            this.stopServer();
        }
    }

    public String shootLoader(int col, int row){
        char shootingPlace = HostMap[col][row];
        if(shootingPlace == '~' || shootingPlace == '.'){
            HostMap[col][row] = '~';
            return "pudło";
        } else {
            if(shootingPlace == '@'){
                if(isShipSunk(col, row)){
                    return "trafiony zatopiony";
                }
                return "trafiony";
            }
            HostMap[col][row] = '@';
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
        char fieldOnCoords = HostMap[col][row];

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
            if(i == 10 && j == 10){
                break;
            }
            if(i == 0){
                ++j;
            }
            HostMap[j][i] = character;
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

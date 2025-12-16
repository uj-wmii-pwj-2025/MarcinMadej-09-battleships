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
        try {
            while (true) {
                System.out.println("Write your command:\n");
                String msg = ReadConsole.nextLine().trim();
                out.println(msg);
                System.out.println(in.readLine());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
            ClientMap[i][j] = character;
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

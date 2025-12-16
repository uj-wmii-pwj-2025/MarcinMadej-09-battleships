package ShipsGenerator;

import java.util.Arrays;
import java.util.Random;
import java.util.Vector;

public class BattleshipGeneratorImplementation implements BattleshipGenerator {
    private final int MAP_ROWS = 10;
    private final int MAP_COLS = 10;
    private final int[] SHIP_SIZES = {4,3,3,2,2,2,1,1,1,1};
    private final int MAX_TRIES_TO_ADD_NEW_SHIP = MAP_COLS * MAP_ROWS * 10;
    private Random random;
    private char[][] shipsMap = new char[MAP_ROWS][MAP_COLS];

    public BattleshipGeneratorImplementation(Random random) {
        this.random = random;
        for(char[] row : shipsMap) {
            Arrays.fill(row, '.');
        }
    }

    private boolean addShip(int size){
        char[][] backupMap = new char[MAP_ROWS][MAP_COLS];
        for (int i = 0; i < MAP_ROWS; i++) {
            backupMap[i] = shipsMap[i].clone();
        }

        Vector<Vector2D> currentShip = new Vector<>();
        Vector<Vector2D> validShipExtensions = new Vector<>();

        if(!findFirstPartForNewShip(currentShip)){
            throw new BattleshipException("Battleship Generator Error: There was no more free space to add even one ship");
        }
        --size;

        while (size > 0) {
            validShipExtensions.clear();
            findValidShipExtensions(validShipExtensions, currentShip);
            if(validShipExtensions.isEmpty()){
                shipsMap = backupMap;
                return false;
            }
            addShipPart(validShipExtensions, currentShip);
            --size;
        }

        return true;
    }

    private void addShipPart(Vector<Vector2D> validShipExtensions, Vector<Vector2D> currentShip){
        int theChosenOne = random.nextInt(validShipExtensions.size());
        shipsMap[validShipExtensions.elementAt(theChosenOne).getX()][validShipExtensions.elementAt(theChosenOne).getY()] = '#';
        currentShip.add(validShipExtensions.elementAt(theChosenOne));
    }

    private void findValidShipExtensions(Vector<Vector2D> validShipExtensions, Vector<Vector2D> currentShip){
        for (Vector2D part : currentShip) {
            int x = part.getX();
            int y = part.getY();
            int[][] directions = {{1,0},{-1,0},{0,1},{0,-1}};
            for (int[] d : directions) {
                int newX = x + d[0];
                int newY = y + d[1];
                if (isValidShipPartPlacement(newX, newY, currentShip)) {
                    validShipExtensions.add(new Vector2D(newX, newY));
                }
            }
        }

    }

    private boolean findFirstPartForNewShip(Vector<Vector2D> currentShip){
        Vector<Vector2D> possiblePoints = new Vector<>();
        for (int x = 0; x < MAP_ROWS; x++) {
            for (int y = 0; y < MAP_COLS; y++) {
                possiblePoints.add(new Vector2D(x, y));
            }
        }

        while(!possiblePoints.isEmpty()) {
            int randomIndex = random.nextInt(possiblePoints.size());
            if (isValidShipPartPlacement(possiblePoints.elementAt(randomIndex).getX(), possiblePoints.elementAt(randomIndex).getY(), currentShip)) {
                shipsMap[possiblePoints.elementAt(randomIndex).getX()][possiblePoints.elementAt(randomIndex).getY()] = '#';
                currentShip.add(possiblePoints.elementAt(randomIndex));
                return true;
            } else {
                possiblePoints.remove(randomIndex);
            }
        }
        return false;
    }


    private boolean isValidShipPartPlacement(int x, int y, Vector<Vector2D> currentShip){

        Vector2D currentPointBeingChecked = new Vector2D(x,y);
        if(currentShip.contains(currentPointBeingChecked)){
            return false;
        }
        if (x < 0 || x >= MAP_ROWS || y < 0 || y >= MAP_COLS) {
            return false;
        }
        if (shipsMap[x][y] != '.') {
            return false;
        }
        for (int rowBeingChecked = x - 1; rowBeingChecked <= x + 1; rowBeingChecked++) {
            for (int columnBeingChecked = y - 1; columnBeingChecked <= y + 1; columnBeingChecked++) {
                if (rowBeingChecked == x && columnBeingChecked == y) continue;
                if (rowBeingChecked < 0 || rowBeingChecked >= MAP_ROWS || columnBeingChecked < 0 || columnBeingChecked >= MAP_COLS) continue;
                currentPointBeingChecked = new Vector2D(rowBeingChecked,columnBeingChecked);
                if (shipsMap[rowBeingChecked][columnBeingChecked] != '.' && !currentShip.contains(currentPointBeingChecked)) {
                    return false;
                }
            }
        }
        return true;
    }



    @Override
    public String generateMap(){
        for(int size : SHIP_SIZES){
            boolean didShipGenerate = false;
            int triesToAddNewShip = this.MAX_TRIES_TO_ADD_NEW_SHIP;
            while(triesToAddNewShip > 0 && didShipGenerate == false){
                didShipGenerate = addShip(size);
                --triesToAddNewShip;
            }
            if(triesToAddNewShip == 0){
                throw new BattleshipException("Battleship Generator Error: Failed to add a new ship of size: " + size);
            }
        }
        StringBuilder mapString = new StringBuilder();

        for (char[] row : shipsMap) {
            for (char cell : row) {
                mapString.append(cell);
            }
        }

        return mapString.toString();
    }
}

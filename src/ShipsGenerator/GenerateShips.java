package ShipsGenerator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class GenerateShips {
    static BattleshipGenerator generator = BattleshipGenerator.defaultInstance();

    public static void generateToFile(String mapFile ,String filename){
        String map = generator.generateMap();
        File myMap = new File( mapFile , filename);
        try {
            Files.writeString(myMap.toPath(), map);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

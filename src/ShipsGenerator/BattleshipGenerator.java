package ShipsGenerator;

import java.util.Random;

public interface BattleshipGenerator {

    String generateMap();

    static BattleshipGenerator defaultInstance() {
        return new BattleshipGeneratorImplementation(new Random());
    }

}

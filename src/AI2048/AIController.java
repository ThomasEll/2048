package AI2048;

import java.util.Random;

/**
 * @author Tom Longdon
 */
public class AIController {

    public Game2048Model makeMove(Game2048Model myGame){

        Random r = new Random();

        int move =  r.nextInt(4);

        switch (move) {
            case 0:
                myGame.left();
                break;
            case 1:
                myGame.up();
                break;
            case 2:
                myGame.right();
                break;
            case 3:
                myGame.down();
                break;
        }

        return myGame;
    }
}

package AI2048;

import java.util.Random;

/**
 * @author Tom Longdon
 */
public class AIController {

    public Game2048Model makeMove(Game2048Model myGame){

        Game2048Model gameClone = new Game2048Model(myGame);
        Game2048Model bestGame = new Game2048Model(myGame);

        for (int i = 0; i < 100; i++){
            Random r = new Random();
            int move =  r.nextInt(4);

            switch (move) {
                case 0:
                    myGame.left();
                    if(myGame.getScore() >= bestGame.getScore()){
                        bestGame.copyGame(myGame);
                    }
                    break;
                case 1:
                    myGame.up();
                    if(myGame.getScore() >= bestGame.getScore()){
                        bestGame.copyGame(myGame);
                    }
                    break;
                case 2:
                    myGame.right();
                    if(myGame.getScore() >= bestGame.getScore()){
                        bestGame.copyGame(myGame);
                    }
                    break;
                case 3:
                    myGame.down();
                    if(myGame.getScore() >= bestGame.getScore()){
                        bestGame.copyGame(myGame);
                    }
                    break;
            }

            myGame.copyGame(gameClone);
        }





        return bestGame;
    }
}

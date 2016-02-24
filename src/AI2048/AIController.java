package AI2048;

import java.util.Random;

/**
 * @author Tom Longdon
 */
public class AIController {

    public Game2048Model makeMove(Game2048Model myGame){
        Game2048Model bestGame = new Game2048Model(myGame);

        Game2048Model gameLeft = new Game2048Model(myGame);
        Game2048Model gameRight = new Game2048Model(myGame);
        Game2048Model gameUp = new Game2048Model(myGame);
        Game2048Model gameDown = new Game2048Model(myGame);

        boolean movedLeft = gameLeft.left();
        boolean movedRight = gameRight.right();
        boolean movedUp = gameUp.up();
        boolean movedDown = gameDown.down();

        /*if(!moved)
            System.out.println("no move made");*/

        double bestEvaluation, leftEval, rightEval, upEval, downEval;
        int bestMove;

        if(movedLeft){
            leftEval = evaluate(gameLeft);
        } else {
            leftEval = -1.0;
        }

        if(movedRight){
            rightEval = evaluate(gameRight);
        } else {
            rightEval = -1.0;
        }

        if(movedUp){
            upEval = evaluate(gameUp);
        } else {
            upEval = -1.0;
        }

        if(movedDown){
            downEval = evaluate(gameDown);
        } else {
            downEval = -1.0;
        }


        bestEvaluation = leftEval;
        bestMove = 0;

        if(bestEvaluation < rightEval){
            bestEvaluation = rightEval;
            bestMove = 1;
        }

        if(bestEvaluation < upEval){
            bestEvaluation = upEval;
            bestMove = 2;
        }

        if(bestEvaluation < downEval){
            bestMove = 3;
        }

        switch (bestMove){
            case 0:
                bestGame.copyGame(gameLeft);
                break;
            case 1:
                bestGame.copyGame(gameRight);
                break;
            case 2:
                bestGame.copyGame(gameUp);
                break;
            case 3:
                bestGame.copyGame(gameDown);
                break;
        }

        return bestGame;
    }

    private double evaluate(Game2048Model game2048Model){
        Tile[] tiles =  game2048Model.getTiles();

        double count = 0.0;
        for (Tile tile : tiles) {
            if (tile.isEmpty()) {
                count++;
            }
        }
        double countEval = count / 14.0;

        double score = (double) game2048Model.getScore();


        double scoreEval;
        if (score != 0.0) {
            scoreEval = 1.0 - (1.0 / score);
        } else {
            scoreEval = 0.0;
        }

        return (4.0 * scoreEval) + countEval;
    }
}

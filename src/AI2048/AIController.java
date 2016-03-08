package AI2048;

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

        double monotonicityLeftRightScore = monotonicityLeftRight(tiles);
        double monotonicityUpDownScore = monotonicityUpDown(tiles);

        double mergeScore = merges(tiles);

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

        return (4.0 * scoreEval) + countEval + mergeScore;
    }

    private double monotonicityLeftRight(Tile[] tiles){
        double monotonicityLeft = 0.0;
        double monotonicityRight = 0.0;

        for(int y = 0; y < 4; y++){
            for(int x = 0; x < 4; x++){
                //Prevents array out of bounds errors
                if (x > 0) {
                    int previous = tiles[(x - 1) + (y * 4)].value;
                    int current = tiles[x + y * 4].value;
                    if(previous > current){
                        monotonicityLeft += previous - current;
                    } else {
                        monotonicityRight += current - previous;
                    }
                }
            }
        }
        return Math.max(monotonicityLeft, monotonicityRight);
    }

    private double monotonicityUpDown(Tile[] tiles){
        double monotonicityUp = 0.0;
        double monotonicityDown = 0.0;

        for(int x = 0; x < 4; x++){
            for(int y = 0; y < 4; y++){
                //Prevents array out of bounds errors
                if(y > 0){
                    int previous = tiles[(x + y * 4) - 4].value;
                    int current = tiles[(x + y * 4)].value;
                    if(previous > current){
                        monotonicityUp += previous - current;
                    } else {
                        monotonicityDown += current - previous;
                    }
                }
            }
        }

        return Math.max(monotonicityUp, monotonicityDown);
    }

    private double merges(Tile[] tiles){
        double mergeCount = 0.0;

        for(int i = 0; i < tiles.length; i++){
            int current = tiles[i].value;

            if(i-4 >= 0)
                if (current == tiles[i - 4].value)
                    mergeCount++;

            if(i-1 >= 0 && (i%4 != 0))
                if (current == tiles[i - 1].value)
                    mergeCount++;

            if(i+1 <= 15 && (i%4 != 3))
                if (current == tiles[i + 1].value)
                    mergeCount++;

            if(i+4 <= 15)
                if (current == tiles[i+4].value)
                    mergeCount++;
        }

        return mergeCount;
    }
}

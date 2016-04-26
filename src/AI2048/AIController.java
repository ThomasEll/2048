package AI2048;

import java.util.List;

/**
 * @author Tom Longdon
 */
public class AIController {

    /**
     * Makes all possible moves and evaluates which is the best
     * @param myGame The game board to make moves on
     * @return The game board with the best move made
     */
    public Game2048Model makeMove(Game2048Model myGame){
        Game2048Model bestGame = new Game2048Model(myGame);

        //Generate four copies of the current gameboard so that
        Game2048Model gameLeft = new Game2048Model(myGame);
        Game2048Model gameRight = new Game2048Model(myGame);
        Game2048Model gameUp = new Game2048Model(myGame);
        Game2048Model gameDown = new Game2048Model(myGame);

        boolean movedLeft = gameLeft.left();
        boolean movedRight = gameRight.right();
        boolean movedUp = gameUp.up();
        boolean movedDown = gameDown.down();

        double bestEvaluation, leftEval, rightEval, upEval, downEval;
        int bestMove;

        //If it was possible to move the game left then evaluate it
        //Otherwise set the evaluation to the minimum value for a double
        if(movedLeft){
            leftEval = evaluate(gameLeft);
        } else {
            leftEval = Double.NEGATIVE_INFINITY;
        }

        //If it was possible to move the game right then evaluate it
        //Otherwise set the evaluation to the minimum value for a double
        if(movedRight){
            rightEval = evaluate(gameRight);
        } else {
            rightEval = Double.NEGATIVE_INFINITY;
        }

        //If it was possible to move the game up then evaluate it
        //Otherwise set the evaluation to the minimum value for a double
        if(movedUp){
            upEval = evaluate(gameUp);
        } else {
            upEval = Double.NEGATIVE_INFINITY;
        }

        //If it was possible to move the game down then evaluate it
        //Otherwise set the evaluation to the minimum value for a double
        if(movedDown){
            downEval = evaluate(gameDown);
        } else {
            downEval = Double.NEGATIVE_INFINITY;
        }



        //Select the move that has the largest utility value
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

    /**
     * Evaluates the utility of a given board
     * @param game2048Model The board to evaluate
     * @return the utility for a given board
     */
    private double evaluate(Game2048Model game2048Model){
        Tile[] tiles =  game2048Model.getTiles();

        double monotonicityLeftRightScore = monotonicityLeftRight(tiles);
        double monotonicityUpDownScore = monotonicityUpDown(tiles);

        int mergeCount = getMergeCount(tiles);

        int emptyCount = getEmptyCount(tiles);

        int largestTile = largestTile(tiles);

        int score = game2048Model.getScore();

        int cornerScore = cornerScore(tiles);

        int adjacencyScore = adjacencyScore(tiles);

        return largestTile + score + cornerScore*3 + adjacencyScore*2 + emptyCount + mergeCount*2 - monotonicityLeftRightScore*2 - monotonicityUpDownScore*2;
    }

    /**
     * Counts the number of tiles that are on the board
     * @param tiles the array of tiles to check
     * @return the number of empty tiles
     */
    private int getEmptyCount(Tile[] tiles) {
        int count = 0;
        for (Tile tile : tiles) {
            if (tile.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Gets the monotonicity value of the rows on the board
     *
     * The aim of this function is to minimise the number of times that the game board changes value directions
     * for a given row: 2<4<8<16 is monotonic, 4<8>2<16 is non-monotonic
     *
     * @param tiles the array of tiles to check
     * @return the monotonicity score for the rows on the board
     */
    private double monotonicityLeftRight(Tile[] tiles){
        double monotonicityLeft = 0.0;
        double monotonicityRight = 0.0;

        for(int y = 0; y < 4; y++){
            for(int x = 0; x < 4; x++){
                //Prevents array out of bounds errors
                if (x > 0) {
                    double previous = (tiles[(x - 1) + (y * 4)].value != 0) ? Math.log(tiles[(x - 1) + (y * 4)].value) / Math.log(2) : 0;
                    double current = (tiles[x + y * 4].value != 0) ? Math.log(tiles[x + y * 4].value) / Math.log(2) : 0;
                    if(previous > current){
                        monotonicityLeft += previous - current;
                    } else {
                        monotonicityRight += current - previous;
                    }
                }
            }
        }
        return Math.min(monotonicityLeft, monotonicityRight);
    }

    /**
     * Gets the monotonicity value of the columns on the board
     *
     * The aim of this function is to minimise the number of times that the game board changes value directions
     * for a given column: 2<4<8<16 is monotonic, 4<8>2<16 is non-monotonic
     *
     * @param tiles the array of tiles to check
     * @return the monotonicity score for the columns on the board
     */
    private double monotonicityUpDown(Tile[] tiles){
        double monotonicityUp = 0.0;
        double monotonicityDown = 0.0;

        for(int x = 0; x < 4; x++){
            for(int y = 0; y < 4; y++){
                //Prevents array out of bounds errors
                if(y > 0){
                    //Gets the value
                    double previous = (tiles[(x + y * 4) - 4].value != 0) ? Math.log(tiles[(x + y * 4) - 4].value) / Math.log(2) : 0;
                    double current = (tiles[(x + y * 4)].value != 0) ? Math.log(tiles[(x + y * 4)].value) / Math.log(2) : 0;
                    if(previous > current){
                        monotonicityUp += previous - current;
                    } else {
                        monotonicityDown += current - previous;
                    }
                }
            }
        }

        return Math.min(monotonicityUp, monotonicityDown);
    }

    /**
     * Gets the number of merges that can be made on a given board
     * @param tiles the array of tiles to check
     * @return number of merges that can be made
     */
    private int getMergeCount(Tile[] tiles){
        int mergeCount = 0;

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

    /**
     * Finds the value of the largest tile currently on the board
     * @param tiles the array of tiles to check
     * @return the value of the largest tile
     */
    private int largestTile(Tile[] tiles){
        int largestTile = 0;

        for (Tile tile : tiles)
            if (tile.value > largestTile)
                largestTile = tile.value;

        return largestTile;
    }



    /**
     * Checks to see if the tile with the largest value is placed in a corner
     * @param tiles the array of tiles to check
     * @return returns the corner score for the board
     */
    private int cornerScore(Tile[] tiles){
        int largestTile = 0;

        //Board corners are held in the array positions 0, 3, 12, 15
        int topLeft = tiles[0].value;
        int topRight = tiles[3].value;
        int bottomLeft = tiles[12].value;
        int bottomRight = tiles[15].value;

        for (Tile tile : tiles)
            if (tile.value > largestTile)
                largestTile = tile.value;

        if(largestTile == topLeft
                || largestTile == topRight
                || largestTile == bottomLeft
                || largestTile == bottomRight){
            return largestTile;
        } else {
            return 0;
        }
    }

    /**
     * Check whether the second largest tile is adjacent to the largest tile
     * @param tiles the array of tiles to check
     * @return the adjacency score for the board
     */
    private int adjacencyScore(Tile[] tiles){
        //Values of the largest and second largest tiles
        int largestTile = 0;
        int secondLargest = 0;
        int thirdLargest = 0;

        //Positions of the largest and second largest tiles in the array
        int largestPosition = 0;
        int secondPosition = 0;
        int thirdPosition = 0;

        //Loops through all of the tiles and gets the positions and values of the largest and second largest tiles
        for (int i =0; i < tiles.length; i++) {
            if (tiles[i].value > largestTile) {
                thirdLargest = secondLargest;
                thirdPosition = secondPosition;

                secondLargest = largestTile;
                secondPosition = largestPosition;

                largestTile = tiles[i].value;
                largestPosition = i;
            } else if (tiles[i].value == largestTile) {
                //If multiple tiles exist with the same value, check whether one is in a corner.
                //If previous largest is already in a corner then prioritise that one.
                if((i == 0 || i == 3 || i == 12 || i == 15)
                        && (largestPosition != 0 || largestPosition != 3 || largestPosition != 12 || largestPosition !=15)){
                    thirdLargest = secondLargest;
                    thirdPosition = secondPosition;

                    secondLargest = largestTile;
                    secondPosition = largestPosition;

                    largestTile = tiles[i].value;
                    largestPosition = i;
                }
            }

        }

        if((secondPosition == (largestPosition-4) && thirdPosition == (secondPosition-4))
                || (secondPosition == (largestPosition-1) && thirdPosition == (secondPosition-1))
                || (secondPosition == (largestPosition +1) && thirdPosition == (secondPosition-1))
                || (secondPosition == (largestPosition + 4)) && thirdPosition == (secondPosition-1)){
            return secondLargest + thirdLargest;
        } else if (secondPosition == (largestPosition-4)
                || secondPosition == (largestPosition-1)
                || secondPosition == (largestPosition +1)
                || secondPosition == (largestPosition + 4)) {
            return secondLargest;
        } else {
            return 0;
        }
    }
}
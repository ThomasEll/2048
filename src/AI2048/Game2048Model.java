package AI2048;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Tom Longdon
 * @author Konstantin Bulenkov
 */
public class Game2048Model {

    private Tile[] tiles;
    boolean win = false;
    boolean lose = false;
    int score = 0;

    public Game2048Model(){
        resetGame();
    }

    public Game2048Model(Game2048Model game2048Model){
        tiles = new Tile[4 * 4];
        copyGame(game2048Model);
    }

    public void copyGame(Game2048Model game2048Model){
        this.score = game2048Model.getScore();
        this.win = game2048Model.getWin();
        this.lose = game2048Model.getLose();

        Tile[] t = game2048Model.getTiles();

        for(int i = 0; i < t.length; i++){
            this.tiles[i] = new Tile(t[i].value);
        }
    }

    /**
     * Resets the game, generating an array with a length of 16 to represent the 4x4 grid,
     * and randomly places two new tiles.
     */
    public void resetGame() {
        score = 0;
        win = false;
        lose = false;
        tiles = new Tile[4 * 4];
        for (int i = 0; i < tiles.length; i++) {
            tiles[i] = new Tile();
        }
        addTile();
        addTile();
    }

    /**
     * Generates a new tile in an empty space with a value of either 2 or 4. A Tile with a value of a 2 has a higher
     * probability of being generated
     */
    private void addTile() {
        List<Tile> list = availableSpace();
        if (!availableSpace().isEmpty()) {
            int index = (int) (Math.random() * list.size()) % list.size();
            Tile emptyTile = list.get(index);
            emptyTile.value = Math.random() < 0.9 ? 2 : 4;
        }
    }

    /**
     * Generates the list of spaces that currently do not have a tile in them
     *
     * @return  The list of empty tiles
     */
    private List<Tile> availableSpace() {
        final List<Tile> list = new ArrayList<Tile>(16);
        for (Tile t : tiles) {
            if (t.isEmpty()) {
                list.add(t);
            }
        }
        return list;
    }

    /**
     * Called when the Left key is pressed. Iterates through each line and and calls the moveLine and mergeLine
     * functions on them. Once the line has been changed, calls the setLine function to save the new line to the
     * game board
     */
    public void left() {
        boolean needAddTile = false;
        for (int i = 0; i < 4; i++) {
            Tile[] line = getLine(i);
            Tile[] merged = mergeLine(moveLine(line));
            setLine(i, merged);

            //If needAddTile is still set to false and the line has changed set needAddTile to true.
            if (!needAddTile && !compare(line, merged)) {
                needAddTile = true;
            }
        }

        //After all lines have been moved generate a new tile
        if (needAddTile) {
            addTile();
        }
    }

    /**
     * Called when the Right key is pressed. Calls the rotate function so that the tiles are rotated 180 degrees (This
     * would leave a tile in (0,0) at position (3,3). This allows to code for moving and merging lines to be reused.
     * Once those functions have been performed, the tiles are rotated a further 180 degrees to return them to their
     * correct position.
     */
    public void right() {
        tiles = rotate(180);
        left();
        tiles = rotate(180);
    }

    /**
     * Called when the Up key is pressed. Calls the rotate function so that the tiles are rotated 270 degrees (This
     * would leave a tile in (0,0) at position (3,0). This allows to code for moving and merging lines to be reused.
     * Once those functions have been performed, the tiles are rotated a further 90 degrees to return them to their
     * correct position.
     */
    public void up() {
        tiles = rotate(270);
        left();
        tiles = rotate(90);
    }

    /**
     * Called when the Right key is pressed. Calls the rotate function so that the tiles are rotated 90 degrees (This
     * would leave a tile in (0,0) at position (0,3). This allows to code for moving and merging lines to be reused.
     * Once those functions have been performed, the tiles are rotated a further 270 degrees to return them to their
     * correct position.
     */
    public void down() {
        tiles = rotate(90);
        left();
        tiles = rotate(270);
    }

    /**
     * Checks if the game board is full
     *
     * @return Boolean value, true if availableSpace's size is 0
     */
    private boolean isFull() {
        return availableSpace().size() == 0;
    }

    /**
     * Checks if a move can be made on the current game board
     *
     * @return Boolean value, false if no more moves can be made
     */
    boolean canMove() {
        //If the game board isn't full then more moves can be made
        if (!isFull()) {
            return true;
        }
        //Iterates through each tile and checks if any adjacent tile has the same value.
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                Tile t = tileAt(x, y);
                if ((x < 3 && t.value == tileAt(x + 1, y).value)
                        || ((y < 3) && t.value == tileAt(x, y + 1).value)) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Checks whether a line has had any values change
     *
     * @param line1 State of the line before the move was made
     * @param line2 State of the line after the move was made
     * @return Boolean value, false if a change has occurred
     */
    private boolean compare(Tile[] line1, Tile[] line2) {
        if (line1 == line2) {
            return true;
        } else if (line1.length != line2.length) {
            return false;
        }

        for (int i = 0; i < line1.length; i++) {
            if (line1[i].value != line2[i].value) {
                return false;
            }
        }
        return true;
    }


    /**
     * "Rotates" the tiles on the game board by a given number of degrees. This is done by shifting values in the
     * tiles array to another position
     *
     * @param angle Number of degrees to "rotate" tiles by
     * @return      Shifted tiles positions
     */
    private Tile[] rotate(int angle) {
        Tile[] newTiles = new Tile[4 * 4];
        int offsetX = 3, offsetY = 3;
        if (angle == 90) {
            offsetY = 0;
        } else if (angle == 270) {
            offsetX = 0;
        }

        double rad = Math.toRadians(angle);
        int cos = (int) Math.cos(rad);
        int sin = (int) Math.sin(rad);
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                int newX = (x * cos) - (y * sin) + offsetX;
                int newY = (x * sin) + (y * cos) + offsetY;
                newTiles[(newX) + (newY) * 4] = tileAt(x, y);
            }
        }
        return newTiles;
    }

    /**
     * Moves values in a section of the tiles array to the first empty position. For example, [4,0,2,0] would become
     * [4,2,0,0]
     * @param oldLine   Current state of line
     * @return          Updated state of line as array
     */
    private Tile[] moveLine(Tile[] oldLine) {
        LinkedList<Tile> l = new LinkedList<Tile>();
        for (int i = 0; i < 4; i++) {
            if (!oldLine[i].isEmpty())
                l.addLast(oldLine[i]);
        }
        if (l.size() == 0) {
            return oldLine;
        } else {
            Tile[] newLine = new Tile[4];
            ensureSize(l, 4);
            for (int i = 0; i < 4; i++) {
                newLine[i] = l.removeFirst();
            }
            return newLine;
        }
    }

    /**
     * Merges adjacent tiles in a line that have the same value, and then moves merged tiles into the first available
     * space. This is done from the left, so if more than tile has the same value only one merge will happen.
     * For example [2,2,2,0] will produce [4,2,0,0]
     * @param oldLine   The line of tiles to be merged
     * @return          The merged line
     */
    private Tile[] mergeLine(Tile[] oldLine) {
        LinkedList<Tile> list = new LinkedList<Tile>();
        for (int i = 0; i < 4 && !oldLine[i].isEmpty(); i++) {
            int num = oldLine[i].value;
            if (i < 3 && oldLine[i].value == oldLine[i + 1].value) {
                num *= 2;
                score += num;
                int ourTarget = 2048;
                if (num == ourTarget) {
                    win = true;
                }
                i++;
            }
            list.add(new Tile(num));
        }
        if (list.size() == 0) {
            return oldLine;
        } else {
            ensureSize(list, 4);
            return list.toArray(new Tile[4]);
        }
    }

    /**
     * Ensures that each line is the correct size once tiles have been merged. If a line is not the correct size then
     * new (empty) tiles are added.
     *
     * @param l The line to be checked
     * @param s The size that the line should be
     */
    private static void ensureSize(java.util.List<Tile> l, int s) {
        while (l.size() != s) {
            l.add(new Tile());
        }
    }

    /**
     *Gets the tiles contained in a particular line
     *
     * @param index Corresponds with which line is to be accessed
     * @return      Array of values in that line
     */
    private Tile[] getLine(int index) {
        Tile[] result = new Tile[4];
        for (int i = 0; i < 4; i++) {

            result[i] = tileAt(i, index);
        }
        return result;
    }

    /**
     * Saves updated line values to tiles
     *
     * @param index Offset to select line
     * @param re    Temporary updated line to be saved
     */
    private void setLine(int index, Tile[] re) {
        System.arraycopy(re, 0, tiles, index * 4, 4);
    }

    /**
     * Gets the tile at a particular position in a particular line. Because all of the tiles are stored in a single
     * array, x acts as a the position within a line, whilst y acts as the offset value to get that line within the
     * array. For example (0,3) would represent the value at tiles[12]
     *
     * @param x Position within line
     * @param y Line number (0 top, 3 bottom)
     * @return  The value of the tile
     */
    private Tile tileAt(int x, int y) {
        return tiles[x + y * 4];
    }

    public boolean getWin(){
        return win;
    }

    public boolean getLose(){
        return !win && !canMove();
    }

    public Tile getTile(int x, int y){
        return tiles[x + y * 4];
    }

    public int getScore(){
        return score;
    }

    public Tile[] getTiles(){
        return tiles;
    }
}

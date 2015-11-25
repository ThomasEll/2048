/*
 * Copyright 1998-2014 Konstantin Bulenkov http://bulenkov.com/about
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bulenkov.game2048;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Konstantin Bulenkov
 * @author Tom Longdon
 */
public class Game2048 extends JPanel {
    private static final Color BG_COLOR = new Color(0xbbada0);
    private static final String FONT_NAME = "Arial";
    private static final int TILE_SIZE = 64;
    private static final int TILES_MARGIN = 16;

    private Tile[] myTiles;
    boolean myWin = false;
    boolean myLose = false;
    int myScore = 0;

    public Game2048() {
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                //Resets game when escape is pressed
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    resetGame();
                }

                //If no more moves can be made set lose condition to true
                if (!canMove()) {
                    myLose = true;
                }

                //Switch statement for directional keys
                if (!myWin && !myLose) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_LEFT:
                            left();
                            break;
                        case KeyEvent.VK_RIGHT:
                            right();
                            break;
                        case KeyEvent.VK_DOWN:
                            down();
                            break;
                        case KeyEvent.VK_UP:
                            up();
                            break;
                    }
                }

                //If win condition has not been met and no more moves can be made set lose condition to true
                if (!myWin && !canMove()) {
                    myLose = true;
                }

                repaint();
            }
        });
        resetGame();
    }

    /**
     * Resets the game, generating an array with a length of 16 to represent the 4x4 grid,
     * and randomly places two new tiles.
     */
    public void resetGame() {
        myScore = 0;
        myWin = false;
        myLose = false;
        myTiles = new Tile[4 * 4];
        for (int i = 0; i < myTiles.length; i++) {
            myTiles[i] = new Tile();
        }
        addTile();
        addTile();
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
        myTiles = rotate(180);
        left();
        myTiles = rotate(180);
    }

    /**
     * Called when the Up key is pressed. Calls the rotate function so that the tiles are rotated 270 degrees (This
     * would leave a tile in (0,0) at position (3,0). This allows to code for moving and merging lines to be reused.
     * Once those functions have been performed, the tiles are rotated a further 90 degrees to return them to their
     * correct position.
     */
    public void up() {
        myTiles = rotate(270);
        left();
        myTiles = rotate(90);
    }

    /**
     * Called when the Right key is pressed. Calls the rotate function so that the tiles are rotated 90 degrees (This
     * would leave a tile in (0,0) at position (0,3). This allows to code for moving and merging lines to be reused.
     * Once those functions have been performed, the tiles are rotated a further 270 degrees to return them to their
     * correct position.
     */
    public void down() {
        myTiles = rotate(90);
        left();
        myTiles = rotate(270);
    }

    /**
     * Gets the tile at a particular position in a particular line. Because all of the tiles are stored in a single
     * array, x acts as a the position within a line, whilst y acts as the offset value to get that line within the
     * array. For example (0,3) would represent the value at myTiles[12]
     *
     * @param x Position within line
     * @param y Line number (0 top, 3 bottom)
     * @return  The value of the tile
     */
    private Tile tileAt(int x, int y) {
        return myTiles[x + y * 4];
    }

    /**
     * Generates a new tile in an empty space with a value of either 2 or 4. A Tile with a value of a 2 has a higher
     * probability of being generated
     */
    private void addTile() {
        List<Tile> list = availableSpace();
        if (!availableSpace().isEmpty()) {
            int index = (int) (Math.random() * list.size()) % list.size();
            Tile emptyTime = list.get(index);
            emptyTime.value = Math.random() < 0.9 ? 2 : 4;
        }
    }

    /**
     * Generates the list of spaces that currently do not have a tile in them
     *
     * @return  The list of empty tiles
     */
    private List<Tile> availableSpace() {
        final List<Tile> list = new ArrayList<Tile>(16);
        for (Tile t : myTiles) {
            if (t.isEmpty()) {
                list.add(t);
            }
        }
        return list;
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
                myScore += num;
                int ourTarget = 2048;
                if (num == ourTarget) {
                    myWin = true;
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
     * Saves updated line values to myTiles
     *
     * @param index Offset to select line
     * @param re    Temporary updated line to be saved
     */
    private void setLine(int index, Tile[] re) {
        System.arraycopy(re, 0, myTiles, index * 4, 4);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(BG_COLOR);
        g.fillRect(0, 0, this.getSize().width, this.getSize().height);
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                drawTile(g, myTiles[x + y * 4], x, y);
            }
        }
    }

    private void drawTile(Graphics g2, Tile tile, int x, int y) {
        Graphics2D g = ((Graphics2D) g2);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        int value = tile.value;
        int xOffset = offsetColors(x);
        int yOffset = offsetColors(y);
        g.setColor(tile.getBackground());
        g.fillRoundRect(xOffset, yOffset, TILE_SIZE, TILE_SIZE, 14, 14);
        g.setColor(tile.getForeground());
        final int size = value < 100 ? 36 : value < 1000 ? 32 : 24;
        final Font font = new Font(FONT_NAME, Font.BOLD, size);
        g.setFont(font);

        String s = String.valueOf(value);
        final FontMetrics fm = getFontMetrics(font);

        final int w = fm.stringWidth(s);
        final int h = -(int) fm.getLineMetrics(s, g).getBaselineOffsets()[2];

        if (value != 0)
            g.drawString(s, xOffset + (TILE_SIZE - w) / 2, yOffset + TILE_SIZE - (TILE_SIZE - h) / 2 - 2);

        if (myWin || myLose) {
            g.setColor(new Color(255, 255, 255, 30));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(new Color(78, 139, 202));
            g.setFont(new Font(FONT_NAME, Font.BOLD, 48));
            if (myWin) {
                g.drawString("You won!", 68, 150);
            }
            if (myLose) {
                g.drawString("Game over!", 50, 130);
                g.drawString("You lose!", 64, 200);
            }
            if (myWin || myLose) {
                g.setFont(new Font(FONT_NAME, Font.PLAIN, 16));
                g.setColor(new Color(128, 128, 128, 128));
                g.drawString("Press ESC to play again", 80, getHeight() - 40);
            }
        }
        g.setFont(new Font(FONT_NAME, Font.PLAIN, 18));
        g.drawString("Score: " + myScore, 200, 365);

    }

    private static int offsetColors(int arg) {
        return arg * (TILES_MARGIN + TILE_SIZE) + TILES_MARGIN;
    }

    static class Tile {
        int value;

        public Tile() {
            this(0);
        }

        public Tile(int num) {
            value = num;
        }

        public boolean isEmpty() {
            return value == 0;
        }

        public Color getForeground() {
            return value < 16 ? new Color(0x776e65) :  new Color(0xf9f6f2);
        }

        public Color getBackground() {
            switch (value) {
                case 2:    return new Color(0xeee4da);
                case 4:    return new Color(0xede0c8);
                case 8:    return new Color(0xf2b179);
                case 16:   return new Color(0xf59563);
                case 32:   return new Color(0xf67c5f);
                case 64:   return new Color(0xf65e3b);
                case 128:  return new Color(0xedcf72);
                case 256:  return new Color(0xedcc61);
                case 512:  return new Color(0xedc850);
                case 1024: return new Color(0xedc53f);
                case 2048: return new Color(0xedc22e);
            }
            return new Color(0xcdc1b4);
        }
    }

    public static void main(String[] args) {
        JFrame game = new JFrame();
        game.setTitle("2048 Game");
        game.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        game.setSize(340, 400);
        game.setResizable(false);

        game.add(new Game2048());

        game.setLocationRelativeTo(null);
        game.setVisible(true);
    }
}

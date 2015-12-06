package AI2048;

import java.util.Random;

/**
 * @author Tom Longdon
 */
public class AIController {

    public int makeMove(){

        Random r = new Random();

        return r.nextInt(4);
    }
}

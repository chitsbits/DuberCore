
/**
 * [Terrain.java]
 * Abstract terrain class
 * @author Sunny Jiao
 * @version 1.0
 */

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.physics.box2d.Body;

public abstract class Terrain {

    float worldX;
    float worldY;
    Body body1;
    Body body2;
    Sprite sprite;
    int marchingSquaresCase;
    int numEdges;

    /**
     * Initializes tilecase and coordinates
     * @param tileCase marching squares case
     * @param x worldX coordinates
     * @param y worldY coordinates
     */
    public Terrain(int tileCase, float x, float y){
        this.marchingSquaresCase = tileCase;
        this.worldX = x;
        this.worldY = y;

        if(marchingSquaresCase == 5 || marchingSquaresCase == 10){
            numEdges = 2;
        }
        else if (marchingSquaresCase > 0 && marchingSquaresCase < 15){
            numEdges = 1;
        }
        else {
            numEdges = 0;
        }
    }
}


/**
 * [TileMap.java]
 * Class representing the procedurally generated game map
 * @author Sunny Jiao
 * @version 1.0
 */

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

public class TileMap {
    
    // Each tile on the map is 0.5 meters
    // Dimensions of the map in tiles. The corner map
    // will be +1 larger than the map dimensions.
    public static final int MAP_COLS = 480;
    public static final int MAP_ROWS = 270;

    private float increment = 0.07f;
    private float zIncrement = 0.2f;

    private World world; // Reference to Box2D world

    private int[][] cornerArr;
    private Terrain[][] terrainArr;

    /**
     * Create and generate the tilemap
     * @param world Box2D world
     */
    public TileMap(World world){
        this.world = world;
        generateNewMap(world);
    }

    /**
     * Generates a new tilemap
     * @param world Box2D world
     */
    public void generateNewMap(World world) {

        OpenSimplexNoise worldGenNoise = new OpenSimplexNoise((long)(Math.random() * Long.MAX_VALUE));

        terrainArr = new Terrain[MAP_COLS][MAP_ROWS];
        cornerArr = new int[MAP_COLS+1][MAP_ROWS+1];

        // Generate tilemap from noise
        float xoffset = 0f;
        for(int i = 0; i < MAP_COLS+1; i++){
            xoffset += increment;
            float yoffset = 0f;
            for(int j = 0; j < MAP_ROWS+1; j++){
                float value = (float) (worldGenNoise.eval(xoffset, yoffset)); // eval returns a double from -1 to 1
                cornerArr[i][j] = (int)(Math.ceil(value)); // 0 or 1
                yoffset += increment;
            }
        }

        // World edge
        Vector2 a = new Vector2(0, DuberCore.WORLD_HEIGHT);
        Vector2 b = new Vector2(DuberCore.WORLD_WIDTH, DuberCore.WORLD_HEIGHT);
        Vector2 c = new Vector2(DuberCore.WORLD_WIDTH, 0);
        Vector2 d = new Vector2(0, 0);

        makeEdgeShape(a, b);
        makeEdgeShape(b, c);
        makeEdgeShape(c, d);
        makeEdgeShape(d, a);

        // Initialize map
        for (int i = 0; i < TileMap.MAP_COLS; i++) {
            for (int j = 0; j < TileMap.MAP_ROWS; j++) {
                
                // Marching square edges
                a = new Vector2((float) (i + 0.5)/2, (float) (j)/2);
                b = new Vector2((float) (i + 1)/2, (float) (j + 0.5)/2);
                c = new Vector2((float) (i + 0.5)/2, (float) (j + 1)/2);
                d = new Vector2((float) (i)/2, (float) (j + 0.5)/2);

                // Which contour - determined by the 4 corners of the tile
                int tileCase = getTileMarchCase(cornerArr[i][j], cornerArr[i+1][j], cornerArr[i+1][j+1], cornerArr[i][j+1]);

                switch (tileCase) {
                case 0:
                    terrainArr[i][j] = new Air(tileCase, i/2f, j/2f);
                    break;
                case 1:
                    terrainArr[i][j] = new Stone(tileCase, i/2f, j/2f, makeEdgeShape(a, d));
                    break;
                case 2:
                    terrainArr[i][j] = new Stone(tileCase, i/2f, j/2f, makeEdgeShape(a, b));
                    break;
                case 3:
                    terrainArr[i][j] = new Stone(tileCase, i/2f, j/2f, makeEdgeShape(d, b));
                    break;
                case 4:
                    terrainArr[i][j] = new Stone(tileCase, i/2f, j/2f, makeEdgeShape(c, b));
                    break;
                case 5:
                    terrainArr[i][j] = new DoubleStone(tileCase, i/2f, j/2f, makeEdgeShape(d, c), makeEdgeShape(b, a));
                    break;
                case 6:
                    terrainArr[i][j] = new Stone(tileCase, i/2f, j/2f, makeEdgeShape(a, c));
                    break;
                case 7:
                    terrainArr[i][j] = new Stone(tileCase, i/2f, j/2f, makeEdgeShape(c, d));
                    break;
                case 8:
                    terrainArr[i][j] = new Stone(tileCase, i/2f, j/2f, makeEdgeShape(c, d));
                    break;
                case 9:
                    terrainArr[i][j] = new Stone(tileCase, i/2f, j/2f, makeEdgeShape(a, c));
                    break;
                case 10:
                    terrainArr[i][j] = new DoubleStone(tileCase, i/2f, j/2f, makeEdgeShape(a, d), makeEdgeShape(b, c));
                    break;
                case 11:
                    terrainArr[i][j] = new Stone(tileCase, i/2f, j/2f, makeEdgeShape(c, b));
                    break;
                case 12:
                    terrainArr[i][j] = new Stone(tileCase, i/2f, j/2f, makeEdgeShape(d, b));
                    break;
                case 13:
                    terrainArr[i][j] = new Stone(tileCase, i/2f, j/2f, makeEdgeShape(a, b));
                    break;
                case 14:
                    terrainArr[i][j] = new Stone(tileCase, i/2f, j/2f, makeEdgeShape(d, a));
                    break;
                case 15:
                    terrainArr[i][j] = new Stone(tileCase, i/2f, j/2f);
                }

            }
        }

        // Generate Gold
        OpenSimplexNoise GoldGenNoise = new OpenSimplexNoise((long)(Math.random() * Long.MAX_VALUE));
        
        // Generate tilemap from noise
        xoffset = 0f;
        for(int i = 0; i < MAP_COLS; i++){
            xoffset += increment;
            float yoffset = 0f;
            for(int j = 0; j < MAP_ROWS; j++){
                yoffset += increment;
                float zoffset = 0f;
                
                /* 
                 * Have Z increment to generate ores multiple times.
                 * This allows for many pockets of small veins, rather than
                 * few pockets of large veins.
                 */
                for(int k = 0; k < 40; k++){
                    zoffset += zIncrement;

                    float value = (float) GoldGenNoise.eval(xoffset, yoffset, zoffset); // eval returns a double from -1 to 1

                    Terrain currentTile = terrainArr[i][j];

                    // Make sure ore doesn't generate in air or as a double body case
                    if(currentTile instanceof Stone && currentTile.marchingSquaresCase != 5 && currentTile.marchingSquaresCase != 10){

                        // Embedded ore (not exposed to air)
                        if(currentTile.marchingSquaresCase == 15){
                            // Generate gold
                            if(value < -0.75f){
                                terrainArr[i][j] = new Gold(currentTile.marchingSquaresCase, currentTile.worldX, currentTile.worldY);
                            }
                            // Generate health crystal
                            else if (value > 0.85f){
                                terrainArr[i][j] = new HealthCrystal(currentTile.marchingSquaresCase, currentTile.worldX, currentTile.worldY);
                            }
                        }
                        // Surface ore (exposed to air)
                        else{
                            // Generate gold
                            if(value < -0.8f){
                                terrainArr[i][j] = new Gold(currentTile.marchingSquaresCase, currentTile.worldX, currentTile.worldY, currentTile.body1);
                            }
                            // Generate health crystal
                            else if (value > 0.9f){
                                terrainArr[i][j] = new HealthCrystal(currentTile.marchingSquaresCase, currentTile.worldX, currentTile.worldY, currentTile.body1);
                            }
                        }

                    }
                }
            }
        }
    }

    /**
     * Gives the marching squares tile case
     * @param bottomLeft Bottom left corner value
     * @param bottomRight Bottom right corner value
     * @param topRight Top right corner value
     * @param topLeft Top left corner value
     * @return Integer case from 0 - 15
     */
    private int getTileMarchCase(int bottomLeft, int bottomRight, int topRight, int topLeft) {
        return topLeft * 8 + topRight * 4 + bottomRight * 2 + bottomLeft;
    }

    /**
     * Creates an edgebody
     * @param v1 Point 1
     * @param v2 Point 2
     * @return The created body
     */
    private Body makeEdgeShape(Vector2 v1, Vector2 v2) {

        EdgeShape edgeShape = new EdgeShape();
        edgeShape.set(v1, v2);
  
        BodyDef edgeDef = new BodyDef();
        edgeDef.type = BodyType.StaticBody;
  
        Body edgeBody = world.createBody(edgeDef);
  
        FixtureDef edgeFixtureDef = new FixtureDef();
        edgeFixtureDef.shape = edgeShape;
        edgeFixtureDef.filter.categoryBits = DuberCore.TERRAIN;
        edgeFixtureDef.filter.maskBits = DuberCore.PLAYER | DuberCore.ENEMY | DuberCore.PROJECTILE |
                                        DuberCore.GRAPPLE | DuberCore.SENSOR;
        edgeFixtureDef.friction = 0.5f;
        Fixture edgeFixture = edgeBody.createFixture(edgeFixtureDef);
        edgeFixture.setUserData("edgeFixture");
  
        edgeShape.dispose();
        return edgeBody;
     }

    /**
     * Breaks a tile and rerenders the surrounding tiles
     * @param tileMapBreakPoint Vector point with tilemap coords
     */
    public void clearTile(Vector2 tileMapBreakPoint, DuberCore game){
        int x = (int)(tileMapBreakPoint.x);
        int y = (int)(tileMapBreakPoint.y);

        if (terrainArr[x][y] instanceof Gold){
            game.score += 5;
        }
        else if (terrainArr[x][y] instanceof HealthCrystal){
            game.score += 3;
            game.player.heal(3);
        }

        // Set tile's corner points to 0
        if(x >= 0 && x+1 <= MAP_COLS && y >= 0 && y+1 <= MAP_ROWS){
            cornerArr[x][y] = 0;
            cornerArr[x+1][y] = 0;
            cornerArr[x][y+1] = 0;
            cornerArr[x+1][y+1] = 0;
        }

        // Recalculate cases for encompassing tiles
        for(int i = -1; i <= 1; i++){
            for(int j = -1; j <= 1; j++){

                int currentX = x+i;
                int currentY = y+j;

                if(currentX >= 0 && currentX < MAP_COLS && currentY >= 0 && currentY < MAP_ROWS){
                    
                    Terrain currentTile = terrainArr[currentX][currentY];

                    if(currentTile.numEdges == 1) {
                        world.destroyBody(currentTile.body1);
                    } else if(currentTile.numEdges == 2){
                        world.destroyBody(currentTile.body1);
                        world.destroyBody(currentTile.body2);
                    }

                    // Marching square edges
                    Vector2 a = new Vector2((float) (currentX + 0.5)/2, (float) (currentY)/2);
                    Vector2 b = new Vector2((float) (currentX + 1)/2, (float) (currentY + 0.5)/2);
                    Vector2 c = new Vector2((float) (currentX + 0.5)/2, (float) (currentY + 1)/2);
                    Vector2 d = new Vector2((float) (currentX)/2, (float) (currentY + 0.5)/2);

                    // Which contour - determined by the 4 corners of the tile
                    int tileCase = getTileMarchCase(cornerArr[currentX][currentY],
                                                    cornerArr[currentX+1][currentY],
                                                    cornerArr[currentX+1][currentY+1],
                                                    cornerArr[currentX][currentY+1]);

                    // Replace the tile
                    if(terrainArr[currentX][currentY] instanceof Stone){

                        switch (tileCase) {
                            case 0:
                                terrainArr[currentX][currentY] = new Air(tileCase, currentX/2f, currentY/2f);
                                break;
                            case 1:
                                terrainArr[currentX][currentY] = new Stone(tileCase, currentX/2f, currentY/2f, makeEdgeShape(a, d));
                                break;
                            case 2:
                                terrainArr[currentX][currentY] = new Stone(tileCase, currentX/2f, currentY/2f, makeEdgeShape(a, b));
                                break;
                            case 3:
                                terrainArr[currentX][currentY] = new Stone(tileCase, currentX/2f, currentY/2f, makeEdgeShape(d, b));
                                break;
                            case 4:
                                terrainArr[currentX][currentY] = new Stone(tileCase, currentX/2f, currentY/2f, makeEdgeShape(c, b));
                                break;
                            case 5:
                                terrainArr[currentX][currentY] = new DoubleStone(tileCase, currentX/2f, currentY/2f, makeEdgeShape(d, c), makeEdgeShape(b, a));
                                break;
                            case 6:
                                terrainArr[currentX][currentY] = new Stone(tileCase, currentX/2f, currentY/2f, makeEdgeShape(a, c));
                                break;
                            case 7:
                                terrainArr[currentX][currentY] = new Stone(tileCase, currentX/2f, currentY/2f, makeEdgeShape(c, d));
                                break;
                            case 8:
                                terrainArr[currentX][currentY] = new Stone(tileCase, currentX/2f, currentY/2f, makeEdgeShape(c, d));
                                break;
                            case 9:
                                terrainArr[currentX][currentY] = new Stone(tileCase, currentX/2f, currentY/2f, makeEdgeShape(a, c));
                                break;
                            case 10:
                                terrainArr[currentX][currentY] = new DoubleStone(tileCase, currentX/2f, currentY/2f, makeEdgeShape(a, d), makeEdgeShape(b, c));
                                break;
                            case 11:
                                terrainArr[currentX][currentY] = new Stone(tileCase, currentX/2f, currentY/2f, makeEdgeShape(c, b));
                                break;
                            case 12:
                                terrainArr[currentX][currentY] = new Stone(tileCase, currentX/2f, currentY/2f, makeEdgeShape(d, b));
                                break;
                            case 13:
                                terrainArr[currentX][currentY] = new Stone(tileCase, currentX/2f, currentY/2f, makeEdgeShape(a, b));
                                break;
                            case 14:
                                terrainArr[currentX][currentY] = new Stone(tileCase, currentX/2f, currentY/2f, makeEdgeShape(d, a));
                                break;
                            case 15:
                                terrainArr[currentX][currentY] = new Stone(tileCase, currentX/2f, currentY/2f);
                        }
                    }
                    else if (terrainArr[currentX][currentY] instanceof Gold){
                        switch (tileCase) {
                            case 0:
                                terrainArr[currentX][currentY] = new Air(tileCase, currentX/2f, currentY/2f);
                                break;
                            case 1:
                                terrainArr[currentX][currentY] = new Gold(tileCase, currentX/2f, currentY/2f, makeEdgeShape(a, d));
                                break;
                            case 2:
                                terrainArr[currentX][currentY] = new Gold(tileCase, currentX/2f, currentY/2f, makeEdgeShape(a, b));
                                break;
                            case 3:
                                terrainArr[currentX][currentY] = new Gold(tileCase, currentX/2f, currentY/2f, makeEdgeShape(d, b));
                                break;
                            case 4:
                                terrainArr[currentX][currentY] = new Gold(tileCase, currentX/2f, currentY/2f, makeEdgeShape(c, b));
                                break;
                            case 5:
                                terrainArr[currentX][currentY] = new DoubleStone(tileCase, currentX/2f, currentY/2f, makeEdgeShape(d, c), makeEdgeShape(b, a));
                                break;
                            case 6:
                                terrainArr[currentX][currentY] = new Gold(tileCase, currentX/2f, currentY/2f, makeEdgeShape(a, c));
                                break;
                            case 7:
                                terrainArr[currentX][currentY] = new Gold(tileCase, currentX/2f, currentY/2f, makeEdgeShape(c, d));
                                break;
                            case 8:
                                terrainArr[currentX][currentY] = new Gold(tileCase, currentX/2f, currentY/2f, makeEdgeShape(c, d));
                                break;
                            case 9:
                                terrainArr[currentX][currentY] = new Gold(tileCase, currentX/2f, currentY/2f, makeEdgeShape(a, c));
                                break;
                            case 10:
                                terrainArr[currentX][currentY] = new DoubleStone(tileCase, currentX/2f, currentY/2f, makeEdgeShape(a, d), makeEdgeShape(b, c));
                                break;
                            case 11:
                                terrainArr[currentX][currentY] = new Gold(tileCase, currentX/2f, currentY/2f, makeEdgeShape(c, b));
                                break;
                            case 12:
                                terrainArr[currentX][currentY] = new Gold(tileCase, currentX/2f, currentY/2f, makeEdgeShape(d, b));
                                break;
                            case 13:
                                terrainArr[currentX][currentY] = new Gold(tileCase, currentX/2f, currentY/2f, makeEdgeShape(a, b));
                                break;
                            case 14:
                                terrainArr[currentX][currentY] = new Gold(tileCase, currentX/2f, currentY/2f, makeEdgeShape(d, a));
                                break;
                            case 15:
                                terrainArr[currentX][currentY] = new Gold(tileCase, currentX/2f, currentY/2f);
                        }
                    }
                    else if (terrainArr[currentX][currentY] instanceof HealthCrystal){
                        switch (tileCase) {
                            case 0:
                                terrainArr[currentX][currentY] = new Air(tileCase, currentX/2f, currentY/2f);
                                break;
                            case 1:
                                terrainArr[currentX][currentY] = new HealthCrystal(tileCase, currentX/2f, currentY/2f, makeEdgeShape(a, d));
                                break;
                            case 2:
                                terrainArr[currentX][currentY] = new HealthCrystal(tileCase, currentX/2f, currentY/2f, makeEdgeShape(a, b));
                                break;
                            case 3:
                                terrainArr[currentX][currentY] = new HealthCrystal(tileCase, currentX/2f, currentY/2f, makeEdgeShape(d, b));
                                break;
                            case 4:
                                terrainArr[currentX][currentY] = new HealthCrystal(tileCase, currentX/2f, currentY/2f, makeEdgeShape(c, b));
                                break;
                            case 5:
                                terrainArr[currentX][currentY] = new DoubleStone(tileCase, currentX/2f, currentY/2f, makeEdgeShape(d, c), makeEdgeShape(b, a));
                                break;
                            case 6:
                                terrainArr[currentX][currentY] = new HealthCrystal(tileCase, currentX/2f, currentY/2f, makeEdgeShape(a, c));
                                break;
                            case 7:
                                terrainArr[currentX][currentY] = new HealthCrystal(tileCase, currentX/2f, currentY/2f, makeEdgeShape(c, d));
                                break;
                            case 8:
                                terrainArr[currentX][currentY] = new HealthCrystal(tileCase, currentX/2f, currentY/2f, makeEdgeShape(c, d));
                                break;
                            case 9:
                                terrainArr[currentX][currentY] = new HealthCrystal(tileCase, currentX/2f, currentY/2f, makeEdgeShape(a, c));
                                break;
                            case 10:
                                terrainArr[currentX][currentY] = new DoubleStone(tileCase, currentX/2f, currentY/2f, makeEdgeShape(a, d), makeEdgeShape(b, c));
                                break;
                            case 11:
                                terrainArr[currentX][currentY] = new HealthCrystal(tileCase, currentX/2f, currentY/2f, makeEdgeShape(c, b));
                                break;
                            case 12:
                                terrainArr[currentX][currentY] = new HealthCrystal(tileCase, currentX/2f, currentY/2f, makeEdgeShape(d, b));
                                break;
                            case 13:
                                terrainArr[currentX][currentY] = new HealthCrystal(tileCase, currentX/2f, currentY/2f, makeEdgeShape(a, b));
                                break;
                            case 14:
                                terrainArr[currentX][currentY] = new HealthCrystal(tileCase, currentX/2f, currentY/2f, makeEdgeShape(d, a));
                                break;
                            case 15:
                                terrainArr[currentX][currentY] = new HealthCrystal(tileCase, currentX/2f, currentY/2f);
                        }
                    }
                }
            }
        }
    }

    /**
     * Get the terrain 2D array
     * @return 2D terrain array
     */
    public Terrain[][] getTerrainArr(){
        return terrainArr;
    }
}

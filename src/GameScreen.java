import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameScreen extends ScreenAdapter implements InputProcessor {

    // Camera dimensions in metres. TODO: scale with monitor
    public static final float CAMERA_WIDTH = 32f;
    public static final float CAMERA_HEIGHT = 18f;

    DuberCore dubercore; // Local instance of the game
    OrthographicCamera camera;
    Player player;

    Box2DDebugRenderer debugRenderer;
    //Vector2 tempMouseVector = new Vector2(0, 0);

    boolean useDebugCamera = false;

    BitmapFont font;
    SpriteBatch worldBatch;
    SpriteBatch hudBatch;

    public static Texture[] stoneTextures;
    public static Texture textureAir;
    public static TextureAtlas textureAtlas;

    int screenX;
    int screenY;
    float clock;

    public GameScreen(DuberCore dubercore){
        this.dubercore = dubercore;
    }

    @Override
    public void show() {
        
        textureAtlas = new TextureAtlas("assets\\sprites.txt");
        dubercore.initialize();
        font = new BitmapFont();

        player = dubercore.player;

        worldBatch = new SpriteBatch();
        hudBatch = new SpriteBatch();

        // create the camera and the SpriteBatch
        camera = new OrthographicCamera();
        // viewport = new FitViewport(800, 480, camera);

        if(useDebugCamera)
        camera.setToOrtho(false, DuberCore.WORLD_WIDTH, DuberCore.WORLD_HEIGHT);
        else
        camera.setToOrtho(false, CAMERA_WIDTH, CAMERA_HEIGHT);
        

        debugRenderer = new Box2DDebugRenderer();
        // sr = new ShapeRenderer();
        Gdx.input.setInputProcessor(this);

        System.out.println(Gdx.graphics.getWidth() + " " + Gdx.graphics.getHeight());

        BodyDef tempEnemyBodyDef = player.bodyDef;
        tempEnemyBodyDef.position.set(player.getPos().x + 3, player.getPos().y + 3);
    }

    @Override
    public void render(float delta) {
        // clear the screen with a dark blue color. The
        // arguments to glClearColor are the red, green
        // blue and alpha component in the range [0,1]
        // of the color to be used to clear the screen.
        Gdx.gl.glClearColor(0.57f, 0.77f, 0.85f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Player input
        // apply left impulse, but only if max velocity is not reached yet
        if (Gdx.input.isKeyPressed(Keys.A) && player.getVel().x > -Player.MAX_VELOCITY && player.canMove) {			
            player.moveLeft();
        }

        // apply right impulse, but only if max velocity is not reached yet
        if (Gdx.input.isKeyPressed(Keys.D) && player.getVel().x < Player.MAX_VELOCITY && player.canMove) {
            player.moveRight();
        }

        // apply right impulse, but only if max velocity is not reached yet
        if (Gdx.input.isKeyJustPressed(Keys.W) && player.collidingCount > 0) {
            player.jump();
        }

        //System.out.println("x: " + player.getPos().x + " y: " + player.getPos().y);
        //System.out.println(Gdx.graphics.getFramesPerSecond());

        // Step physics world
        dubercore.doPhysicsStep(delta);


        // Focus camera on player
        if(!useDebugCamera)
        camera.position.set(player.getPos().x, player.getPos().y, 0);

        // tell the camera to update its matrices.
        camera.update();

        worldBatch.begin();
        worldBatch.setProjectionMatrix(camera.combined);

        // Draw map sprites
        Terrain[][] terrainArr = dubercore.tileMap.terrainArr;

        // Set bounds of the map to render
        int iStart = (int)(Math.max(0f, (camera.position.x - camera.viewportWidth / 2f) * 2));
        int jStart = (int)(Math.max(0f, (camera.position.y - camera.viewportHeight / 2f) * 2));

        int iEnd = (int)(Math.min(TileMap.MAP_COLS, (camera.position.x + camera.viewportWidth / 2f) * 2 + 1));
        int jEnd = (int)(Math.min(TileMap.MAP_ROWS-1, (camera.position.y + camera.viewportHeight / 2f) * 2) + 1);

        for(int i = iStart; i < iEnd; i++){
            for(int j = jStart; j < jEnd; j++){
                Terrain tile = terrainArr[i][j];
                Sprite sprite = tile.sprite;
                sprite.setBounds(tile.worldX, tile.worldY, 0.5f, 0.5f);
                sprite.draw(worldBatch);
            }
        }

        // Draw entities
        for(Entity ent : dubercore.entityList){
            Sprite sprite = ent.sprite;
            sprite.setPosition(ent.body.getPosition().x - sprite.getWidth() / 2, ent.body.getPosition().y - sprite.getHeight() / 2);
            sprite.draw(worldBatch);
        }
        worldBatch.end();

        // Draw hud
        hudBatch.begin();
        font.draw(hudBatch, "Score: " + Integer.toString(dubercore.score), 20, 20);
        hudBatch.end();

        // Render Box2D world
        debugRenderer.render(dubercore.world, camera.combined);
        // Render test mouse line
        // sr.setProjectionMatrix(camera.combined);
        // sr.begin(ShapeType.Line);
        // sr.line(player.getPos(), tempMouseVector);
        // sr.end();

        //System.out.println(enemy.heuristic(enemy.body.getPosition(), player.getPos()));
        for (int e = 0; e < dubercore.entityList.size(); e++){
            if (dubercore.entityList.get(e) instanceof Enemy){
                Enemy enemy = ((Enemy) dubercore.entityList.get(e));

                EnemyAiRayCastCallback callback = new EnemyAiRayCastCallback();
                dubercore.world.rayCast(callback, enemy.body.getPosition(), player.getPos());

                if (enemy.enemyState.equals("wander")){

                    if (enemy.heuristic(enemy.body.getPosition(), player.getPos()) < 15 && callback.los) {
                        enemy.enemyState = "pursuit";
                        enemy.body.setLinearVelocity(0,0);
                    }
                    
                    enemy.move();
                }

                else if (enemy.enemyState.equals("pursuit")) {

                    if (enemy.heuristic(enemy.body.getPosition(), player.getPos()) > 15 && callback.los) {
                        enemy.enemyState = "wander";
                        enemy.body.setLinearVelocity(0,0);
                    }


                    else if (callback.fixtureType != null && callback.fixtureType.equals("player")) {
                        enemy.body.setLinearVelocity(0,0);
                        enemy.pursuit(player.getPos());

                    }
                    
                }
            }
        }

        //periodic spawning of enemies
        clock += Gdx.graphics.getDeltaTime();
        
        if (clock > (int)(Math.random() * ((10 - 5)+1)) + 5) {
            //System.out.println("spawned");
            dubercore.spawnEnemy();
            clock = 0;

        }

        /* sr.begin(ShapeType.Filled);
        for(int i = 0; i < TileMap.MAP_COLS+1; i++){
            for(int j = 0; j < TileMap.MAP_ROWS+1; j++){
                if(dubercore.tileMap.cornerArr[i][j] == 1){
                    sr.setColor(Color.RED);
                } else{
                    sr.setColor(Color.BLACK);
                }
                sr.rect(i / 2f - 0.05f, j / 2f - 0.05f, 0.1f, 0.1f);
            }
        }
        sr.end(); */
        
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void dispose(){
        dubercore.world.dispose();
        worldBatch.dispose();
        hudBatch.dispose();
        textureAtlas.dispose();
        debugRenderer.dispose();
        // sr.dispose();
    }
    
    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.G && player.checkCooldown(player.lastGrenadeUse, Grenade.COOLDOWN)){
            Vector3 mousePos = camera.unproject(new Vector3(screenX, screenY, 0));

            player.throwGrenade(dubercore, mousePos);
            player.lastGrenadeUse = System.currentTimeMillis();

            return true;
        }
        
        else if (keycode == Input.Keys.NUM_1){
            player.activeItem = 1;
            System.out.println("gun go pew");
            if(player.isGrappling){
                player.retractGrapple();
                dubercore.bodyDeletionList.add(player.grapple.body);
            }
            return true;
        }
        else if (keycode == Input.Keys.NUM_2){
            player.activeItem = 2;
            System.out.println("grapple go hook");
            return true;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // Pickaxe
        if (button == Input.Buttons.RIGHT) {
            Vector3 mouseWorldPos = camera.unproject(new Vector3(screenX, screenY, 0));  // Maps the mouse from camera pos to world pos
            Vector2 pickaxeDirection = new Vector2(mouseWorldPos.x - player.getPos().x, mouseWorldPos.y - player.getPos().y).clamp(2, 2);
            Vector2 breakPoint = new Vector2(player.getPos().x + pickaxeDirection.x, player.getPos().y + pickaxeDirection.y);

            PickaxeRayCastCallback callback = new PickaxeRayCastCallback();
            dubercore.world.rayCast(callback, player.getPos(), breakPoint);
            if (callback.collisionPoint != null) {
                dubercore.destroyTerrain(callback.collisionPoint);
                // tempMouseVector = callback.collisionPoint;
            }
            return true;
        }
        //firing weapon/grapple hook
        else if(button == Input.Buttons.LEFT){

            if (player.activeItem == 1 && player.checkCooldown(player.lastWeaponFire, player.getWeapon().fireRate)){
                Vector3 mousePos = camera.unproject(new Vector3(screenX, screenY, 0));
                player.getWeapon().fire(dubercore, mousePos);
                player.lastWeaponFire = System.currentTimeMillis();
                return true;
            }

            else if (player.activeItem == 2 && player.checkCooldown(player.lastGrappleUse, GrapplingHook.COOLDOWN)){
                Vector3 mousePos = camera.unproject(new Vector3(screenX, screenY, 0));  // Maps the mouse from camera pos to world pos
                //System.out.println("shot grapple");
                player.shootGrapple(dubercore.world, mousePos);
                player.lastGrappleUse = System.currentTimeMillis();
                return true; 
            }
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if(button == Input.Buttons.LEFT){
            if (player.activeItem == 2 && player.isGrappling){
                //System.out.println("released grapple");
                player.retractGrapple();
                dubercore.bodyDeletionList.add(player.grapple.body);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {

        this.screenX = screenX;
        this.screenY = screenY;

        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {

        if (amountY == 1 || amountY == -1){
            player.activeItem += amountY;
            if (player.activeItem > 2) {
                player.activeItem = 1;
            }
            else if (player.activeItem < 1) {
                player.activeItem = 2;
            }
            else if (player.activeItem == 1){
                System.out.println("gun go pew");
                if (player.isGrappling){
                    player.retractGrapple();
                    dubercore.bodyDeletionList.add(player.grapple.body);
                }
            }
            else {
                System.out.println("grapple go hook");
            }
            return true;
        }

        return false;
    }

    /**
     * Get the top 10 scores from the online database
     * @return String representation of the leaderboard
     */
    public String getLeaderBoard() {
        Socket sock = new Socket();
        ObjectInputStream inputStream = null;
        ObjectOutputStream outputStream = null;
        try {
            sock.connect(new InetSocketAddress("127.0.0.1", 5000), 5000);
        }
        catch (IOException e) {
            System.out.println("Error connecting to server.");
            // Close socket
            try{
                sock.close();
                System.out.println("Socket sucessfully closed.");
            }
            catch (IOException e1){
                System.out.println("Error closing socket");
                e1.printStackTrace();               
            }
        }

        // Open streams
        try {
            inputStream = new ObjectInputStream(sock.getInputStream());
            outputStream = new ObjectOutputStream(sock.getOutputStream());
        }
        catch (IOException e) {
            System.out.println("Error opening streams");
            e.printStackTrace();
        }
        
        GetLeaderboard request = new GetLeaderboard();
        // Write object
        try {
            outputStream.writeObject(request);
            outputStream.flush();
        }
        catch (IOException e) {
            System.out.println("Error writing object.");
        }

        // Get response
        try {
            Object packet = inputStream.readObject();
            if (packet instanceof String){
                System.out.println("Get packet recieved");
                return (String) packet;
            }
        }
        catch (ClassNotFoundException | IOException e) {
            System.out.println("Erroring reading packet.");
        }
        return null;
    }

    /**
     * Connects to the online database and adds the score to the leaderboard
     */
    public void writeToLeaderboard(){
        Socket sock = new Socket();
        ObjectOutputStream outputStream = null;
        try {
            sock.connect(new InetSocketAddress("127.0.0.1", 5000), 5000);
        }
        catch (IOException e) {
            System.out.println("Error connecting to server.");
            // Close socket
            try{
                sock.close();
                System.out.println("Socket sucessfully closed.");
            }
            catch (IOException e1){
                System.out.println("Error closing socket");             
            }
        }

        // Open stream
        try {
            outputStream = new ObjectOutputStream(sock.getOutputStream());
        }
        catch (IOException e) {
            System.out.println("Error opening streams");
        }

        WriteLeaderboard packet = new WriteLeaderboard();
        packet.name = "test";
        packet.score = 69;

        // Write object
        try {
            outputStream.writeObject(packet);
            outputStream.flush();
        }
        catch (IOException e) {
            System.out.println("Error writing object.");
        }

        // Close socket
        try {
            sock.close();
        }
        catch (IOException e) {
            System.out.println("Error closing socket.");
        }
        System.out.println("Stats written");
    }
}
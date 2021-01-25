import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;

public class GameScreen extends ScreenAdapter implements InputProcessor {

    // Camera dimensions in metres.
    public static final float CAMERA_WIDTH = 32f;
    public static final float CAMERA_HEIGHT = 18f;

    private DuberCore dubercore;
    private OrthographicCamera camera;
    private Player player;

    private Box2DDebugRenderer debugRenderer;
    private ShapeRenderer shapeRenderer;

    private BitmapFont font;
    private SpriteBatch worldBatch;
    private SpriteBatch hudBatch;

    public static TextureAtlas textureAtlas;

    private int screenX;
    private int screenY;
    private float spawnClock;

    // Hud elements
    Sprite grenadeSprite;
    Sprite grappleSprite;
    Sprite pistolSprite;

    public GameScreen(DuberCore dubercore){
        this.dubercore = dubercore;
    }

    @Override
    public void show() {
        
        textureAtlas = new TextureAtlas("assets\\sprites.txt");
        dubercore.initialize();
        font = new BitmapFont();
        font.getData().setScale(1.5f);
        
        player = dubercore.player;

        worldBatch = new SpriteBatch();
        hudBatch = new SpriteBatch();

        // create the camera and the SpriteBatch
        camera = new OrthographicCamera();

        camera.setToOrtho(false, CAMERA_WIDTH, CAMERA_HEIGHT);

        debugRenderer = new Box2DDebugRenderer();
        shapeRenderer = new ShapeRenderer();
        Gdx.input.setInputProcessor(this);

        System.out.println(Gdx.graphics.getWidth() + " " + Gdx.graphics.getHeight());

        grenadeSprite = textureAtlas.createSprite("grenade");
        grenadeSprite.setSize(36f, 40);
        grenadeSprite.setPosition(1200, 30);

        grappleSprite = textureAtlas.createSprite("grappleicon");
        grappleSprite.setSize(40, 50);
        grappleSprite.setPosition(1100, 30);

        pistolSprite = textureAtlas.createSprite("pistol");
        pistolSprite.setSize(60, 40);
        pistolSprite.setPosition(1000, 30);
    }

    @Override
    public void render(float delta) {

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
        if (Gdx.input.isKeyJustPressed(Keys.W) && player.collidingCount > 0 && !player.isGrappling) {
            player.jump();
        }

        // Step physics world
        dubercore.doPhysicsStep(delta);

        // Focus camera on player
        camera.position.set(player.getPos().x, player.getPos().y, 0);
        camera.update();

        // Update cooldown timers
        if (!player.grenadeReady){
            if (DuberCore.checkCooldown(player.lastGrenadeUse, Grenade.COOLDOWN)){
                player.grenadeReady = true;
            }
        }

        if (!player.grappleReady){
            if (DuberCore.checkCooldown(player.lastGrappleUse, GrapplingHook.COOLDOWN)){
                player.grappleReady = true;
            }
        }

        if (!player.weaponReady){
            if (DuberCore.checkCooldown(player.lastWeaponFire, player.getWeapon().fireRate)){
                player.weaponReady = true;
            }
        }

        if (!player.mineReady){
            if (DuberCore.checkCooldown(player.lastTerrainMined, Player.MINING_SPEED)){
                player.mineReady = true;
            }
        }

        // ~~~~ DRAWING ~~~~~
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
            // Slight offset for player sprites
            if (ent instanceof Player){
                sprite.setPosition(ent.body.getPosition().x - sprite.getWidth() / 2 + 0.14f,
                                    ent.body.getPosition().y - sprite.getHeight() / 2);
            } else {
                sprite.setPosition(ent.body.getPosition().x - sprite.getWidth() / 2,
                                    ent.body.getPosition().y - sprite.getHeight() / 2);
            }
            sprite.draw(worldBatch);
            
        }
        worldBatch.end();

        // World UI elements
        Gdx.gl.glLineWidth(1);
        shapeRenderer.setAutoShapeType(true);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin();
        for(Entity ent : dubercore.entityList) {
            if(ent instanceof Enemy){
                Enemy enemy = (Enemy) ent;
                Vector2 enemyPos = enemy.getPos();
                shapeRenderer.set(ShapeType.Line);
                shapeRenderer.setColor(Color.WHITE);
                shapeRenderer.rect(enemyPos.x - 0.4f, enemyPos.y + 0.7f, 0.8f, 0.1f);
                shapeRenderer.set(ShapeType.Filled);
                shapeRenderer.setColor(Color.RED);
                float hpWidth = (enemy.getHp() / Enemy.MAX_HP) * 0.7f;
                shapeRenderer.rect(enemyPos.x - 0.35f, enemyPos.y + 0.73f, hpWidth, 0.05f);
            }
            else if (ent instanceof GrapplingHook){
                shapeRenderer.set(ShapeType.Line);
                shapeRenderer.setColor(Color.GRAY);
                shapeRenderer.line(player.getPos(), ent.getPos());
                Gdx.gl.glLineWidth(4);
            }
        }
        shapeRenderer.end();

        // Draw hud
        hudBatch.begin();
        font.draw(hudBatch, dubercore.playerName, 50, 50);
        font.draw(hudBatch, "Score: " + Integer.toString(dubercore.score), 150, 50);
        font.draw(hudBatch, "Health: " + Integer.toString((int)dubercore.player.hp), 300, 50);

        if (player.grenadeReady){
            grenadeSprite.setColor(Color.WHITE);
        } else {
            grenadeSprite.setColor(Color.RED);
        }
        grenadeSprite.draw(hudBatch);

        if (player.grappleReady){
            grappleSprite.setColor(Color.WHITE);
        } else {
            grappleSprite.setColor(Color.RED);
        }
        grappleSprite.draw(hudBatch);

        if (player.weaponReady){
            pistolSprite.setColor(Color.WHITE);
        } else {
            pistolSprite.setColor(Color.RED);
        }
        pistolSprite.draw(hudBatch);
        hudBatch.end();

        // Render Box2D world
        if (dubercore.getDebugMode()){
            debugRenderer.render(dubercore.world, camera.combined);
        }

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
        spawnClock += Gdx.graphics.getDeltaTime();
        
        if (spawnClock > (int)(Math.random()*(10 - 5)) + 5) {
            //System.out.println("spawned");
            dubercore.spawnEnemy();
            spawnClock = 0;
        }
        if (player.mineReady && player.isMining){
            Vector3 mouseWorldPos = camera.unproject(new Vector3(this.screenX, this.screenY, 0));  // Maps the mouse from camera pos to world pos
            Vector2 pickaxeDirection = new Vector2(mouseWorldPos.x - player.getPos().x, mouseWorldPos.y - player.getPos().y).clamp(2, 2);
            Vector2 breakPoint = new Vector2(player.getPos().x + pickaxeDirection.x, player.getPos().y + pickaxeDirection.y);

            PickaxeRayCastCallback callback = new PickaxeRayCastCallback();
            dubercore.world.rayCast(callback, player.getPos(), breakPoint);
            if (callback.collisionPoint != null) {
                dubercore.destroyTerrain(callback.collisionPoint);
                player.mineReady = false;
            }
            player.lastTerrainMined = System.currentTimeMillis();

        }
        // death
        if (player.hp <= 0){
            dubercore.changeScreen(DuberCore.GAME_OVER);
        }
    }

    @Override
    public void dispose(){
        dubercore.world.dispose();
        worldBatch.dispose();
        hudBatch.dispose();
        textureAtlas.dispose();
        debugRenderer.dispose();
    }
    
    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.G && player.grenadeReady){
            Vector3 mousePos = camera.unproject(new Vector3(screenX, screenY, 0));
            player.throwGrenade(dubercore, mousePos);
            player.grenadeReady = false;
            player.lastGrenadeUse = System.currentTimeMillis();

            return true;
        }
        else if (keycode == Input.Keys.NUM_1){
            player.activeItem = 1;
            System.out.println("gun go pew");
            if(player.isGrappling){
                player.retractGrapple();
                dubercore.entityDeletionQueue.add(player.grapple);
            }
            return true;
        }
        else if (keycode == Input.Keys.NUM_2){
            player.activeItem = 2;
            System.out.println("grapple go hook");
            return true;
        }
        else if (keycode == Input.Keys.ESCAPE){
            dubercore.changeScreen(DuberCore.GAME_OVER);
        }
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // Pickaxe
        if (button == Input.Buttons.RIGHT) {
            player.isMining = true;
            return true;
        }
        // Firing weapon/grapple hook
        else if(button == Input.Buttons.LEFT){

            if (player.activeItem == 1 && player.weaponReady){
                Vector3 mousePos = camera.unproject(new Vector3(screenX, screenY, 0));
                player.getWeapon().fire(dubercore, mousePos, player.getPos());
                player.weaponReady = false;
                player.lastWeaponFire = System.currentTimeMillis();
                return true;
            }

            else if (player.activeItem == 2 && player.grappleReady){
                Vector3 mousePos = camera.unproject(new Vector3(screenX, screenY, 0));  // Maps the mouse from camera pos to world pos
                player.shootGrapple(dubercore.world, mousePos);
                dubercore.entityList.add(player.grapple);
                return true; 
            }
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {

        if (button == Input.Buttons.RIGHT) {
            player.isMining = false;
            return true;
        }

        if(button == Input.Buttons.LEFT){
            if (player.activeItem == 2 && player.isGrappling){
                //System.out.println("released grapple");
                player.retractGrapple();
                player.lastGrappleUse = System.currentTimeMillis();
                dubercore.entityDeletionQueue.add(player.grapple);
                return true;
            }
        }
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
                    dubercore.entityDeletionQueue.add(player.grapple);
                }
            }
            else {
                System.out.println("grapple go hook");
            }
            return true;
        }

        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {

        this.screenX = screenX;
        this.screenY = screenY;
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {

        this.screenX = screenX;
        this.screenY = screenY;

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
}

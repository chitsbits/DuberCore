import com.badlogic.gdx.physics.box2d.Body;

public class Gold extends Terrain {

    public Gold(int tileCase, float x, float y){
        super(tileCase, x,y);
        //this.sprite = new Sprite(GameClient.stoneTextures[tileCase-1]);
        this.sprite = GameClient.textureAtlas.createSprite("gold" + (tileCase));
    }

    public Gold(int tileCase, float x, float y, Body body1){
        super(tileCase, x,y);
        this.body1 = body1;
        //this.sprite = new Sprite(GameClient.stoneTextures[tileCase-1]);
        this.sprite = GameClient.textureAtlas.createSprite("gold" + (tileCase));
    }
    
}

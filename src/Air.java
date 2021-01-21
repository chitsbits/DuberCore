import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;

public class Air extends Terrain {

    public Air(int tileCase, float x, float y){
        super(tileCase, x,y);
        this.sprite = GameClient.textureAtlas.createSprite("air");
    }
    
}

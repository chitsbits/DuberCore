import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

public class GrapplingHook extends Entity {
    
    Player player;  // Reference to host player

    public static final long COOLDOWN = 5000;

    public GrapplingHook(World world, Player player){
        this.player = player;

        bodyDef = new BodyDef();
        bodyDef.type = BodyType.DynamicBody;
        bodyDef.position.set(player.getPos());

        body = world.createBody(bodyDef);
        body.setGravityScale(0);
        
        entityShape = new CircleShape();
        ((CircleShape) entityShape).setRadius(0.2f);
        
        FixtureDef grappleFixtureDef = new FixtureDef();
        grappleFixtureDef.shape = entityShape;
        grappleFixtureDef.filter.categoryBits = DuberCore.GRAPPLE;
        grappleFixtureDef.filter.maskBits = DuberCore.TERRAIN;
        grappleFixtureDef.friction = 1f;

        Fixture grappleFixture = body.createFixture(grappleFixtureDef);
        grappleFixture.setUserData(this);
        entityShape.dispose();
    }
}

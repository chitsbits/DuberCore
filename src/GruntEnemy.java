import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class GruntEnemy extends Enemy {

    GruntEnemy(World world, BodyDef bodyDef) {
        super(2f, 5f);
        this.width = 0.4f;
        this.height =  0.7f;

        this.bodyDef = bodyDef;
        bodyDef.type = BodyType.DynamicBody;
        //bodyDef.position.set(10, 24);
        
        body = world.createBody(bodyDef);

        entityShape = new PolygonShape();
        ((PolygonShape) entityShape).setAsBox(width, height);
        
        body = world.createBody(bodyDef);

        FixtureDef bodyFixtureDef = new FixtureDef();
        bodyFixtureDef.shape = entityShape;
        bodyFixtureDef.filter.categoryBits = Game.ENEMY;
        bodyFixtureDef.filter.maskBits = Game.TERRAIN | Game.PROJECTILE;
        bodyFixtureDef.friction = 1.0f;

        Fixture bodyFixture = body.createFixture(bodyFixtureDef);
        bodyFixture.setUserData(this);
        body.setFixedRotation(true);
        


        entityShape.dispose();
    }

    @Override
    public void pathfind() {
        // TODO Auto-generated method stub

    }
    
}

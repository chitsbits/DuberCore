import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class Pistol extends Weapon {

    Pistol() {
        weaponType = "Pistol";
        damage = 4f;
        fireRate = 400;
        isFiring = false;
        magazineSize = 8;

        sprite = GameScreen.textureAtlas.createSprite("pistol");
        sprite.setSize(60, 40);
        sprite.setPosition(800, 30);
    }

    @Override
    public void fire(DuberCore game, Vector3 mousePos, Vector2 playerPos) {
        bulletDirection = new Vector2();
        bulletDirection.x = mousePos.x - playerPos.x;
        bulletDirection.y = mousePos.y - playerPos.y;
        bulletDirection.clamp(40f, 40f);
        Bullet bullet = new Bullet(game.world, damage, playerPos);
        game.entityList.add(bullet);
        bullet.body.setLinearVelocity(bulletDirection);
        magazineSize -= 1;
        if (magazineSize <= 0){
            fireRate = 2000;
        }
    }

    @Override
    public void reload() {
        magazineSize = 8;
        fireRate = 400;
    }

}

package juego.elementos;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.Viewport;

public class CartaRenderer {

    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch batch;
    private final Viewport viewport;
    private final Color backgroundColor = new Color(0.95f, 0.95f, 0.95f, 1);

    public CartaRenderer(SpriteBatch batch, ShapeRenderer shapeRenderer, Viewport viewport) {
        this.batch = batch;
        this.shapeRenderer = shapeRenderer;
        this.viewport = viewport;
    }

    public void render(Carta carta, float x, float y, float width, float height) {

        carta.updateLimites(x, y, width, height);
        Rectangle limites = carta.getLimites();

        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(backgroundColor);

        shapeRenderer.rect(limites.x, limites.y, limites.width, limites.height);

        shapeRenderer.end();
        batch.begin();
        carta.draw(batch, x, y, width, height);
        batch.end();
    }
}
package juego.elementos;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.Viewport;
import juego.pantallas.Partida;

/**
 * Versión dummy del botón de truco para el servidor.
 * El servidor no necesita renderizar botones, solo manejar la lógica.
 */
public class BotonTruco {

    public BotonTruco(float x, float y, float ancho, float alto,
                      BitmapFont font, Viewport viewport, Partida partida) {
        // Constructor vacío - el servidor no usa interfaz gráfica de juego
    }

    public void update(float delta) {
        // No hace nada - el servidor no actualiza botones visuales
    }

    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        // No hace nada - el servidor no renderiza botones
    }

    public boolean detectarClick() {
        // Siempre retorna false - el servidor no detecta clicks
        return false;
    }
}
package juego.pantallas;
import juego.elementos.Hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.Viewport;
import juego.personajes.Jugador;

/**
 * Pantalla de fin de partida que muestra el resultado final
 * y permite volver al menú
 */
public class PantallaFinal {

    private BitmapFont font;
    private Viewport viewport;
    private Hud hud;

    private float worldWidth;
    private float worldHeight;

    // Control de tiempo
    private float tiempoTranscurrido = 0f;
    private final float TIEMPO_ANTES_PERMITIR_SALIDA = 5.0f;
    private final float TIEMPO_AUTO_RETORNO = 10.0f;

    // Jugadores
    private Jugador ganador;
    private Jugador jugador1;
    private Jugador jugador2;

    // Colores
    private Color colorVictoria = new Color(0.2f, 0.9f, 0.2f, 1f);
    private Color colorDerrota = new Color(0.9f, 0.2f, 0.2f, 1f);
    private Color colorFondoOverlay = new Color(0, 0, 0, 0.8f);

    // Estado
    private boolean activa = false;

    /**
     * Constructor
     */
    public PantallaFinal(BitmapFont font, Viewport viewport, Hud hud,
                         float worldWidth, float worldHeight) {
        this.font = font;
        this.viewport = viewport;
        this.hud = hud;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }

    /**
     * Activa la pantalla final con los jugadores
     */
    public void activar(Jugador ganador, Jugador jugador1, Jugador jugador2) {
        this.ganador = ganador;
        this.jugador1 = jugador1;
        this.jugador2 = jugador2;
        this.activa = true;
        this.tiempoTranscurrido = 0f;
    }

    /**
     * Desactiva la pantalla final
     */
    public void desactivar() {
        this.activa = false;
        this.tiempoTranscurrido = 0f;
    }

    /**
     * Actualiza la lógica de la pantalla
     * @return true si se debe volver al menú
     */
    public boolean update(float delta) {
        if (!activa) {
            return false;
        }

        tiempoTranscurrido += delta;

        // Auto-retorno después del tiempo límite extendido
        if (tiempoTranscurrido >= TIEMPO_ANTES_PERMITIR_SALIDA + TIEMPO_AUTO_RETORNO) {
            return true;
        }

        // Permitir salida manual después del tiempo mínimo
        if (tiempoTranscurrido >= TIEMPO_ANTES_PERMITIR_SALIDA) {
            if (Gdx.input.isTouched() || Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Renderiza la pantalla final
     */
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        if (!activa) {
            return;
        }

        // 1. Dibujar overlay oscuro
        renderOverlay(shapeRenderer);

        // 2. Dibujar mensaje principal
        renderMensajePrincipal(batch);

        // 3. Dibujar puntuación
        renderPuntuacion(batch);

        // 4. Dibujar mensaje de instrucciones
        renderInstrucciones(batch);
    }

    /**
     * Dibuja el overlay oscuro de fondo
     */
    private void renderOverlay(ShapeRenderer shapeRenderer) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(colorFondoOverlay);
        shapeRenderer.rect(0, 0, worldWidth, worldHeight);
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    /**
     * Dibuja el mensaje principal (VICTORIA/DERROTA)
     */
    private void renderMensajePrincipal(SpriteBatch batch) {
        boolean ganoJugador1 = (ganador == jugador1);
        String mensaje = ganoJugador1 ? "¡VICTORIA!" : "DERROTA";
        Color colorMensaje = ganoJugador1 ? colorVictoria : colorDerrota;

        hud.dibujarMensajeCentral(batch, mensaje, colorMensaje);
    }

    /**
     * Dibuja la puntuación final
     */
    private void renderPuntuacion(SpriteBatch batch) {
        batch.begin();

        font.setColor(Color.WHITE);
        font.getData().setScale(1.5f);

        String puntuacion = jugador1.getNombre() + ": " + jugador1.getPuntos() +
                " - " + jugador2.getNombre() + ": " + jugador2.getPuntos();

        GlyphLayout layout = new GlyphLayout(font, puntuacion);

        float x = (worldWidth - layout.width) / 2f;
        float y = worldHeight / 2f - 50f;

        font.draw(batch, puntuacion, x, y);

        batch.end();
    }

    /**
     * Dibuja las instrucciones de salida
     */
    private void renderInstrucciones(SpriteBatch batch) {
        batch.begin();

        font.getData().setScale(1.0f);
        String mensaje;

        if (tiempoTranscurrido < TIEMPO_ANTES_PERMITIR_SALIDA) {
            // Cuenta regresiva
            int segundosRestantes = (int)(TIEMPO_ANTES_PERMITIR_SALIDA - tiempoTranscurrido) + 1;
            mensaje = "Volviendo al menú en " + segundosRestantes + "...";
        } else {
            // Permitir salida
            mensaje = "Presiona cualquier tecla para continuar";
        }

        GlyphLayout layout = new GlyphLayout(font, mensaje);
        float x = (worldWidth - layout.width) / 2f;
        float y = 50f;

        font.draw(batch, mensaje, x, y);

        batch.end();
    }

    /**
     * Verifica si la pantalla está activa
     */
    public boolean isActiva() {
        return activa;
    }

    /**
     * Obtiene el tiempo transcurrido
     */
    public float getTiempoTranscurrido() {
        return tiempoTranscurrido;
    }

    // Setters para personalización

    public void setColorVictoria(Color color) {
        this.colorVictoria = color;
    }

    public void setColorDerrota(Color color) {
        this.colorDerrota = color;
    }

    public void setColorFondoOverlay(Color color) {
        this.colorFondoOverlay = color;
    }
}
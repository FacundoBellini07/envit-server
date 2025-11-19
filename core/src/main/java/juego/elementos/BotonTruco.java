package juego.elementos;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import juego.pantallas.Partida;

/**
 * Botón visual para cantar Truco en el juego
 * ✅ ACTUALIZADO: Solo disponible para quien tira primero en cada mano
 */
public class BotonTruco {

    private Rectangle btnRect;
    private boolean hovered = false;
    private float animacionPulso = 0f;

    private BitmapFont font;
    private Viewport viewport;
    private Partida partida;

    // Colores
    private Color colorDeshabilitado = new Color(0.3f, 0.3f, 0.3f, 0.5f);
    private Color colorNormal = new Color(0.9f, 0.1f, 0.1f, 0.9f);
    private Color colorHover = new Color(1.0f, 0.2f, 0.2f, 1f);
    private Color colorTexto = Color.WHITE;
    private Color colorTextoDeshabilitado = new Color(0.5f, 0.5f, 0.5f, 1f);
    private Color colorIndicador = Color.YELLOW;

    // Dimensiones
    private float btnAncho;
    private float btnAlto;

    public BotonTruco(float x, float y, float ancho, float alto,
                      BitmapFont font, Viewport viewport, Partida partida) {
        this.btnAncho = ancho;
        this.btnAlto = alto;
        this.font = font;
        this.viewport = viewport;
        this.partida = partida;

        this.btnRect = new Rectangle(x, y, ancho, alto);
        this.animacionPulso = 0f;
    }

    public void update(float delta) {
        // Actualizar animación del pulso
        animacionPulso += delta * 3f;
        if (animacionPulso > Math.PI * 2) {
            animacionPulso = 0f;
        }

        // Detectar hover
        actualizarHover();
    }

    /**
     * Dibuja el botón
     */
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        // ✅ NUEVO: El botón solo está disponible si:
        // 1. No se usó el truco aún
        // 2. Es el primer turno de la mano
        // 3. Es el turno del jugador 1
        // 4. El jugador 1 es quien empieza esta mano
        boolean trucoDisponible = !partida.isTrucoUsado()
                && partida.esPrimerTurnoEnMano()
                && partida.esTurnoJugador()
                && partida.jugador1EmpiezaEstaMano();

        // Calcular color y escala según estado
        Color colorBtn;
        float escala = 1.0f;

        if (!trucoDisponible) {
            colorBtn = colorDeshabilitado;
        } else if (hovered) {
            float pulso = (float)Math.sin(animacionPulso) * 0.1f + 0.9f;
            colorBtn = new Color(
                    colorHover.r * pulso,
                    colorHover.g,
                    colorHover.b,
                    colorHover.a
            );
            escala = 1.1f;
        } else {
            colorBtn = colorNormal;
        }

        float offsetX = (btnRect.width * escala - btnRect.width) / 2f;
        float offsetY = (btnRect.height * escala - btnRect.height) / 2f;

        // Dibujar fondo del botón
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(colorBtn);
        shapeRenderer.rect(
                btnRect.x - offsetX,
                btnRect.y - offsetY,
                btnRect.width * escala,
                btnRect.height * escala
        );
        shapeRenderer.end();

        // Borde del botón
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(
                btnRect.x - offsetX,
                btnRect.y - offsetY,
                btnRect.width * escala,
                btnRect.height * escala
        );
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Dibujar texto
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        font.setColor(trucoDisponible ? colorTexto : colorTextoDeshabilitado);
        font.getData().setScale(1.8f);

        String textoTruco = "TRUCO";
        GlyphLayout layout = new GlyphLayout(font, textoTruco);

        float textX = btnRect.x + (btnRect.width - layout.width) / 2f;
        float textY = btnRect.y + (btnRect.height + layout.height) / 2f;

        font.draw(batch, textoTruco, textX, textY);

        // Si está activo en esta mano, mostrar indicador
        if (partida.isTrucoActivoEnManoActual()) {
            font.getData().setScale(0.8f);
            font.setColor(colorIndicador);
            String textoActivo = "x2";
            layout = new GlyphLayout(font, textoActivo);
            float x2X = btnRect.x + (btnRect.width - layout.width) / 2f;
            float x2Y = btnRect.y - 5f;
            font.draw(batch, textoActivo, x2X, x2Y);
        }

        batch.end();
    }

    private void actualizarHover() {
        // ✅ ACTUALIZADO: Solo hacer hover si el botón está disponible
        boolean trucoDisponible = !partida.isTrucoUsado()
                && partida.esPrimerTurnoEnMano()
                && partida.esTurnoJugador()
                && partida.jugador1EmpiezaEstaMano();

        Vector2 mouse = viewport.unproject(
                new Vector2(Gdx.input.getX(), Gdx.input.getY())
        );

        hovered = btnRect.contains(mouse.x, mouse.y) && trucoDisponible;
    }

    public boolean detectarClick() {
        if (!Gdx.input.justTouched()) {
            return false;
        }

        Vector2 touch = viewport.unproject(
                new Vector2(Gdx.input.getX(), Gdx.input.getY())
        );

        if (btnRect.contains(touch.x, touch.y)) {
            return intentarCantarTruco();
        }

        return false;
    }

    private boolean intentarCantarTruco() {
        boolean exito = partida.cantarTruco(Partida.TipoJugador.JUGADOR_1);

        if (exito) {
            System.out.println("¡TRUCO cantado por el jugador!");
        }

        return exito;
    }
}
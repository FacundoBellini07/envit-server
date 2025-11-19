package juego.elementos;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;

public class Animacion {

    private final float WORLD_WIDTH;
    private final float WORLD_HEIGHT;
    private final float CARTA_ANCHO;
    private final float CARTA_ALTO;
    private final Texture dorsoCartaSprite;
    private final ManoManager manoManager;

    private boolean animacionRepartoActiva = false;
    private float tiempoReparto = 0;
    private boolean cartasSalieron = false;
    private boolean cartasEntraron = false;

    private final float DURACION_MOVIMIENTO_CARTA = 0.7f;
    private final float TIEMPO_ENTRE_CARTAS = 0.2f;
    private final int NUM_CARTAS_SALIDA = 6;
    private final float DURACION_SALIDA = NUM_CARTAS_SALIDA * TIEMPO_ENTRE_CARTAS + DURACION_MOVIMIENTO_CARTA;
    private final float DURACION_ENTRADA = 0.5f;

    private final float mazoX;
    private final float mazoY;
    private final float MARGEN_HORIZONTAL;


    public Animacion(float worldWidth, float worldHeight, float cartaAncho, float cartaAlto,
                     Texture dorsoSprite, ManoManager manager) {

        this.WORLD_WIDTH = worldWidth;
        this.WORLD_HEIGHT = worldHeight;
        this.CARTA_ANCHO = cartaAncho;
        this.CARTA_ALTO = cartaAlto;
        this.dorsoCartaSprite = dorsoSprite;
        this.manoManager = manager;

        this.MARGEN_HORIZONTAL = WORLD_WIDTH * 0.05f;
        this.mazoX = WORLD_WIDTH - CARTA_ANCHO - MARGEN_HORIZONTAL;
        this.mazoY = (WORLD_HEIGHT - CARTA_ALTO) / 2f;
    }

    public void iniciarAnimacionReparto() {
        this.animacionRepartoActiva = true;
        this.tiempoReparto = 0;
        this.cartasSalieron = false;
        this.cartasEntraron = false;

        if (manoManager != null) {
            manoManager.setPosicionInicialY(-CARTA_ALTO);
        }
    }

    public void update(float delta) {
        if (animacionRepartoActiva) {
            updateReparto(delta);
        }
    }

    public void render(SpriteBatch batch) {
        if (animacionRepartoActiva) {
            renderReparto(batch);
        }
    }

    public boolean isRepartoTerminado() {
        return cartasSalieron && cartasEntraron;
    }

    private void updateReparto(float delta) {
        this.tiempoReparto += delta;

        if (!cartasSalieron) {
            if (tiempoReparto >= DURACION_SALIDA) {
                cartasSalieron = true;
                tiempoReparto = 0;
            }
        }

        else if (cartasSalieron && !cartasEntraron) {

            if (tiempoReparto < DURACION_ENTRADA) {

                float progreso = tiempoReparto / DURACION_ENTRADA;
                float yInicial = -CARTA_ALTO;
                float yFinal = WORLD_HEIGHT * 0.05f;
                float yActual = yInicial + ((yFinal - yInicial) * progreso);

                if (manoManager != null) {
                    manoManager.setPosicionInicialY(yActual);
                }

            } else {
                cartasEntraron = true;
                animacionRepartoActiva = false;
                if (manoManager != null) {
                    manoManager.setPosicionInicialY(WORLD_HEIGHT * 0.05f);
                }
            }
        }
    }

    private void renderReparto(SpriteBatch batch) {
        if (!cartasSalieron) {

            float distanciaRecorrido = WORLD_WIDTH * 0.7f;
            float yFixed = mazoY;

            for (int i = 0; i < NUM_CARTAS_SALIDA; i++) {

                float tiempoInicioCarta = i * TIEMPO_ENTRE_CARTAS;
                float tiempoMovimiento = tiempoReparto - tiempoInicioCarta;

                if (tiempoMovimiento < 0) {
                    continue;
                }

                float progreso = tiempoMovimiento / DURACION_MOVIMIENTO_CARTA;

                if (progreso > 1.0f) {
                    progreso = 1.0f;
                }

                float xInicial = mazoX;
                float xActual = xInicial + (distanciaRecorrido * progreso);

                float xDraw = xActual + (i * CARTA_ANCHO * 0.05f);

                batch.draw(dorsoCartaSprite,
                        xDraw,
                        yFixed,
                        CARTA_ANCHO,
                        CARTA_ALTO);
            }
        }
    }
}
package juego.elementos;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import juego.personajes.Jugador;


public class ManoRivalRenderer {

    private final Jugador jugadorRival;
    private final CartaRenderer cartaRenderer;
    private final Texture dorsoTexture;
    private final ZonaJuego zonaJuego;

    private final float WORLD_WIDTH;
    private final float WORLD_HEIGHT;
    private final float CARTA_ANCHO;
    private final float CARTA_ALTO;
    private final float ESPACIADO;

    // Configuración
    private boolean mostrarCartasBocaAbajo = true; // Para debug, ponerlo en false

    public ManoRivalRenderer(Jugador rival, CartaRenderer renderer, Texture dorso,
                             ZonaJuego zona, float worldWidth, float worldHeight,
                             float cartaAncho, float cartaAlto) {
        this.jugadorRival = rival;
        this.cartaRenderer = renderer;
        this.dorsoTexture = dorso;
        this.zonaJuego = zona;
        this.WORLD_WIDTH = worldWidth;
        this.WORLD_HEIGHT = worldHeight;
        this.CARTA_ANCHO = cartaAncho;
        this.CARTA_ALTO = cartaAlto;
        this.ESPACIADO = CARTA_ANCHO * 0.2f;
    }

    /**
     * Posiciona las cartas del rival en la parte superior
     */
    public void inicializarPosiciones() {
        Carta[] mano = jugadorRival.getMano();
        int numCartas = mano.length;

        float anchoTotalMano = (numCartas * CARTA_ANCHO) + ((numCartas - 1) * ESPACIADO);
        float startX = (WORLD_WIDTH - anchoTotalMano) / 2f;

        float yRival = WORLD_HEIGHT - (CARTA_ALTO* 0.5f);

        for (int i = 0; i < numCartas; i++) {
            Carta carta = mano[i];
            if (carta == null) continue;

            float currentX = startX + (i * CARTA_ANCHO) + (i * ESPACIADO);
            carta.updateLimites(currentX, yRival, CARTA_ANCHO, CARTA_ALTO);
        }
    }

    /**
     * Dibuja las cartas del rival
     */
    public void render(SpriteBatch batch) {
        Carta[] mano = jugadorRival.getMano();

        for (Carta carta : mano) {
            if (carta == null) continue;
            if (zonaJuego.contieneCartaJugada(carta)) {
                continue;
            }

            Rectangle limites = carta.getLimites();

            if (mostrarCartasBocaAbajo) {
                // Dibujar dorso
                batch.draw(dorsoTexture,
                        limites.x, limites.y,
                        limites.width, limites.height);
            } else {
                // Dibujar carta real (para debug)
                cartaRenderer.render(carta,
                        limites.x, limites.y,
                        limites.width, limites.height);
            }
        }
    }

    public void setMostrarBocaAbajo(boolean bocaAbajo) {
        this.mostrarCartasBocaAbajo = bocaAbajo;
    }

    public boolean isMostrarBocaAbajo() {
        return mostrarCartasBocaAbajo;
    }
}
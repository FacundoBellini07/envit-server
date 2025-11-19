package juego.elementos;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.math.Rectangle;

public class CartaInput implements InputProcessor {

    private final Carta cartaParaMover;
    private final Viewport viewport;
    private final float cartaAncho;
    private final float cartaAlto;

    private ZonaJuego zonaJuego;

    private float posicionOriginalX;
    private float posicionOriginalY;

    private boolean isDragging = false;
    private final Vector3 touchPoint = new Vector3();

    private boolean cartaJugada = false;

    private float dragOffsetX;
    private float dragOffsetY;

    private boolean habilitado = true;

    public CartaInput(Carta carta, Viewport viewport, float ancho, float alto) {
        this.cartaParaMover = carta;
        this.viewport = viewport;
        this.cartaAncho = ancho;
        this.cartaAlto = alto;
    }

    public void setZonaJuego(ZonaJuego zona) {
        this.zonaJuego = zona;
    }

    public void setHabilitado(boolean habilitado) {
        this.habilitado = habilitado;

        // Si se deshabilita mientras se está arrastrando, cancelar el drag
        if (!habilitado && isDragging) {
            isDragging = false;
            cartaParaMover.updateLimites(
                    posicionOriginalX,
                    posicionOriginalY,
                    cartaAncho,
                    cartaAlto
            );
        }
    }



    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {

        if (!habilitado || cartaJugada) {
            return false;
        }

        touchPoint.set(screenX, screenY, 0);
        viewport.unproject(touchPoint);

        Rectangle limitesCarta = cartaParaMover.getLimites();

        if (limitesCarta.contains(touchPoint.x, touchPoint.y)) {
            isDragging = true;

            posicionOriginalX = limitesCarta.x;
            posicionOriginalY = limitesCarta.y;

            dragOffsetX = touchPoint.x - limitesCarta.x;
            dragOffsetY = touchPoint.y - limitesCarta.y;

            return true;
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (!habilitado) {
            return false;
        }

        if (isDragging) {
            touchPoint.set(screenX, screenY, 0);
            viewport.unproject(touchPoint);

            float newX = touchPoint.x - dragOffsetX;
            float newY = touchPoint.y - dragOffsetY;

            cartaParaMover.updateLimites(newX, newY, cartaAncho, cartaAlto);

            return true;
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (!habilitado) {
            return false;
        }

        if (isDragging) {
            isDragging = false;

            // Verificar si la carta fue soltada en la zona de juego
            if (zonaJuego != null && zonaJuego.contieneCarta(cartaParaMover)) {
                // La carta está en la zona válida
                zonaJuego.agregarCarta(cartaParaMover);
                cartaJugada = true;

                System.out.println("Carta jugada: " + cartaParaMover.getNombre());
            } else {
                // La carta NO está en la zona → volver a posición original
                cartaParaMover.updateLimites(
                        posicionOriginalX,
                        posicionOriginalY,
                        cartaAncho,
                        cartaAlto
                );

                System.out.println("Carta devuelta a la mano");
            }

            return true;
        }
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        if (isDragging) {
            isDragging = false;

            cartaParaMover.updateLimites(
                    posicionOriginalX,
                    posicionOriginalY,
                    cartaAncho,
                    cartaAlto
            );

            return true;
        }
        return false;
    }

    public boolean isCartaJugada() {
        return cartaJugada;
    }

    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }
    @Override public boolean keyUp(int keycode) { return false; }
    @Override public boolean keyDown(int keycode) { return false; }
    @Override public boolean keyTyped(char character) { return false; }
}
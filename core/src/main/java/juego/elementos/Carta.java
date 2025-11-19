package juego.elementos;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class Carta {

    private static final Texture barajaTexture = new Texture(Gdx.files.internal("sprites/baraja2.png"));
    private int valor;
    private Palo palo;
    private int jerarquia;
    private TextureRegion region;
    private Rectangle limites;

    public Carta(int valor, Palo palo) {
        this.valor = valor;
        this.palo = palo;
        this.asignarPeso(valor, palo);  // ✅ Esto ahora SÍ asigna

        int cartaW = barajaTexture.getWidth() / 10;
        int cartaH = barajaTexture.getHeight() / 4;
        int colIndex;
        if (valor == 1) {
            colIndex = 9;
        } else if (valor >= 2 && valor <= 7) {
            colIndex = valor - 2;
        } else if (valor >= 10 && valor <= 12) {
            colIndex = valor - 4;
        } else {
            colIndex = 0;
        }

        int rowIndex;
        if (palo == Palo.COPAS) {
            rowIndex = 0;
        } else if (palo == Palo.BASTO) {
            rowIndex = 1;
        } else if (palo == Palo.ORO) {
            rowIndex = 2;
        } else if (palo == Palo.ESPADAS) {
            rowIndex = 3;
        } else {
            rowIndex = 0;
        }

        int startX = colIndex * cartaW;
        int startY = rowIndex * cartaH;

        this.region = new TextureRegion(
                barajaTexture,
                startX,
                startY,
                cartaW,
                cartaH
        );

        this.limites = new Rectangle(0, 0, cartaW, cartaH);
    }


    public void draw(SpriteBatch batch, float x, float y, float width, float height) {
        batch.draw(this.region, x, y, width, height);
    }

    private void asignarPeso(int valor, Palo palo) {
        // 1. ANCHOS Y SIETES ESPECIALES
        if (valor == 1 && palo == Palo.ESPADAS) {
            this.jerarquia = 1;
            return;
        }
        if (valor == 1 && palo == Palo.BASTO) {
            this.jerarquia = 2;
            return;
        }
        if (valor == 7 && palo == Palo.ESPADAS) {
            this.jerarquia = 3;
            return;
        }
        if (valor == 7 && palo == Palo.ORO) {
            this.jerarquia = 4;
            return;
        }

        // 2. TRES Y DOS
        if (valor == 3) {
            this.jerarquia = 5;
            return;
        }
        if (valor == 2) {
            this.jerarquia = 6;
            return;
        }

        // 3. ANCHOS COMUNES
        if (valor == 1) {
            this.jerarquia = 7;
            return;
        }

        // 4. CARTAS NEGRAS
        if (valor == 12) {
            this.jerarquia = 8;
            return;
        }
        if (valor == 11) {
            this.jerarquia = 9;
            return;
        }
        if (valor == 10) {
            this.jerarquia = 10;
            return;
        }

        // 5. SIETES COMUNES
        if (valor == 7) {
            this.jerarquia = 11;
            return;
        }

        // 6. CARTAS MEDIAS
        if (valor == 6) {
            this.jerarquia = 12;
            return;
        }
        if (valor == 5) {
            this.jerarquia = 13;
            return;
        }
        if (valor == 4) {
            this.jerarquia = 14;
            return;
        }

        // Fallback
        this.jerarquia = 15;
    }

    // ✅ NUEVO: equals y hashCode para comparar cartas correctamente
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Carta carta = (Carta) obj;
        return valor == carta.valor && palo == carta.palo;
    }

    @Override
    public int hashCode() {
        int result = valor;
        result = 31 * result + (palo != null ? palo.hashCode() : 0);
        return result;
    }

    public void updateLimites(float x, float y, float width, float height) {
        this.limites.set(x, y, width, height);
    }

    public int getJerarquia() {
        return jerarquia;
    }

    public String getNombre() {
        return valor + " de " + palo;
    }

    public Rectangle getLimites() {
        return limites;
    }

    public int getValor() {
        return valor;
    }

    public Palo getPalo() {
        return palo;
    }
}
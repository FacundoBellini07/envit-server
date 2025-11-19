package juego.personajes;

import juego.elementos.Carta;

public class Jugador {
    private String nombre;
    private Carta mano[] = new Carta[3];
    private int cantCartas = 0;

    // ✅ NUEVO: Puntos de la ronda actual (antes de sumarlos al total)


    public Jugador() {
        this.nombre = "Jugador";
    }

    public Jugador(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Carta[] getMano() {
        return mano;
    }


    public void agregarCarta(Carta c){
        if (cantCartas < mano.length) {
            this.mano[cantCartas] = c;
            cantCartas++;
        }
    }

    public void limpiarMazo(){
        // Limpiar referencias
        for (int i = 0; i < mano.length; i++) {
            mano[i] = null;
        }
        cantCartas = 0;
    }

}
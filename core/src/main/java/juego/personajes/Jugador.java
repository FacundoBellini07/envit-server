package juego.personajes;

import juego.elementos.Carta;

public class Jugador {
    private String nombre;
    private int puntos;
    private Carta mano[] = new Carta[3];
    private int cantCartas = 0;



    public Jugador(String nombre) {
        this.nombre = nombre;
        this.puntos = 0;
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

    public int getPuntos() {
        return puntos;
    }

    // ✅ NUEVO: Sumar puntos
    public void sumarPuntos(int cantidad) {
        this.puntos += cantidad;
    }


    public void agregarCarta(Carta c){
        if (cantCartas < mano.length) {
            this.mano[cantCartas] = c;
            cantCartas++;
        }
    }

    public void limpiarMazo(){

        for (int i = 0; i < mano.length; i++) {
            mano[i] = null;
        }
        cantCartas = 0;
    }


    // ✅ NUEVO: Verificar si tiene una carta específica

}
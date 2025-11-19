package juego.utilidades;

import java.util.Random;

public final class Aleatorio {

    private Aleatorio() {}

    private static Random r = new Random();

    public static int generarEntero(final int MIN, final int MAX){
        return r.nextInt(MAX - MIN + 1 ) + MIN;
    }

    public static int generarEntero(final int NRO){
        return r.nextInt(NRO);
    }

}

package juego.utilidades;

public abstract class Global {
    public static boolean empieza = false;
    public static int manoActual = 0;
    public static int puntosJ1 = 0;
    public static int puntosJ2 = 0;
    public static int turno;
    public static EstadoTurno estadoTurno;

    public enum EstadoTurno {
        ESPERANDO_JUGADOR_1,
        ESPERANDO_JUGADOR_2,
        FINALIZANDO_MANO,
        PARTIDA_TERMINADA
    }
}

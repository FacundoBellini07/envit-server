package juego.elementos;

public enum EstadoTruco {
    SIN_TRUCO(1),
    TRUCO_CANTADO(2),
    RETRUCO_CANTADO(3),
    VALE_CUATRO_CANTADO(4);

    private final int puntos;

    EstadoTruco(int puntos) {
        this.puntos = puntos;
    }

    public int getPuntos() {
        return puntos;
    }

    public EstadoTruco siguiente() {
        switch (this) {
            case SIN_TRUCO:
                return TRUCO_CANTADO;
            case TRUCO_CANTADO:
                return RETRUCO_CANTADO;
            case RETRUCO_CANTADO:
                return VALE_CUATRO_CANTADO;
            default:
                return this;
        }
    }

    public boolean puedeSubir() {
        return this != VALE_CUATRO_CANTADO;
    }
}
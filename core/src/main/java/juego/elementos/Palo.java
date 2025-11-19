package juego.elementos;

public enum Palo {
    BASTO("Basto"),
    ORO("Oro"),
    ESPADAS("Espadas"),
    COPAS("Copas");

    private String nombre;

    Palo(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
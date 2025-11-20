package juego.pantallas;

import juego.elementos.*;
import juego.personajes.Jugador;
import juego.personajes.RivalBot;
import juego.personajes.TipoJugador;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Partida {

    private ArrayList<Carta> mazoRevuelto = new ArrayList<>();
    private int indiceMazo = 0;

    private EstadoTurno estadoActual;

    private final int PUNTOS_PARA_GANAR = 15;
    private Jugador ganador = null;

    private ZonaJuego zonaJugador1;
    private ZonaJuego zonaJugador2;

    private RivalBot rivalBot;
    private Jugador jugador1;
    private Jugador jugador2;

    private int cartasJugador1Antes = 0;
    private int cartasJugador2Antes = 0;

    private int manoActual = 0;
    private final int MAX_MANOS = 3;

    private TipoJugador jugadorMano;
    private Random random = new Random();

    private boolean trucoUsado = false;
    private int manoTrucoUsada = -1;
    private TipoJugador jugadorQueCanto = null;

    public Partida() {
        this.estadoActual = EstadoTurno.ESPERANDO_JUGADOR_1;
        Mazo mazoOriginal = new Mazo();
        for (int i = 0; i < mazoOriginal.getCantCartas(); i++) {
            mazoRevuelto.add(mazoOriginal.getCarta(i));
        }
        Collections.shuffle(mazoRevuelto);
    }

    public void inicializar(ZonaJuego zonaJug1, ZonaJuego zonaJug2, RivalBot bot,
                            Jugador jug1, Jugador jug2, int manoActual) {
        this.zonaJugador1 = zonaJug1;
        this.zonaJugador2 = zonaJug2;
        this.rivalBot = bot;
        this.jugador1 = jug1;
        this.jugador2 = jug2;

        this.jugadorMano = random.nextBoolean() ? TipoJugador.JUGADOR_1 : TipoJugador.JUGADOR_2;

        this.estadoActual = (jugadorMano == TipoJugador.JUGADOR_1)
                ? EstadoTurno.ESPERANDO_JUGADOR_1
                : EstadoTurno.ESPERANDO_JUGADOR_2;

        System.out.println("[SERVIDOR] INICIO DE PARTIDA - Empieza: " +
                (jugadorMano == TipoJugador.JUGADOR_1 ? jug1.getNombre() : jug2.getNombre()));

        this.cartasJugador1Antes = 0;
        this.cartasJugador2Antes = 0;
        this.manoActual = manoActual;

        resetearTruco();
    }

    private void resetearTruco() {
        this.trucoUsado = false;
        this.manoTrucoUsada = -1;
        this.jugadorQueCanto = null;
    }

    public void repartirCartas(Jugador jugador1, Jugador jugador2) {
        if (indiceMazo + 6 > mazoRevuelto.size()) {
            indiceMazo = 0;
            Collections.shuffle(mazoRevuelto);
        }

        jugador1.limpiarMazo();
        jugador2.limpiarMazo();

        for (int i = 0; i < 3; i++) {
            jugador1.agregarCarta(mazoRevuelto.get(indiceMazo++));
            jugador2.agregarCarta(mazoRevuelto.get(indiceMazo++));
        }

        System.out.println("[SERVIDOR] Cartas repartidas a ambos jugadores");
    }

    public Carta[] getCartasJugador1() {
        return jugador1 != null ? jugador1.getMano() : null;
    }

    public Carta[] getCartasJugador2() {
        return jugador2 != null ? jugador2.getMano() : null;
    }

    public boolean rondaCompletada() {
        return manoActual >= MAX_MANOS;
    }
    public void repartirNuevasCartas() {
        repartirCartas(jugador1, jugador2);

        // Resetear contadores de mano
        manoActual = 0;
        cartasJugador1Antes = 0;
        cartasJugador2Antes = 0;

        // Limpiar zonas de juego
        if (zonaJugador1 != null) zonaJugador1.limpiar();
        if (zonaJugador2 != null) zonaJugador2.limpiar();

        // Resetear truco
        resetearTruco();

        // Alternar quien es mano
        jugadorMano = (jugadorMano == TipoJugador.JUGADOR_1)
                ? TipoJugador.JUGADOR_2
                : TipoJugador.JUGADOR_1;

        // Actualizar estado según quien empieza
        estadoActual = (jugadorMano == TipoJugador.JUGADOR_1)
                ? EstadoTurno.ESPERANDO_JUGADOR_1
                : EstadoTurno.ESPERANDO_JUGADOR_2;

        System.out.println("[SERVIDOR] Nueva ronda iniciada. Empieza: " + jugadorMano);
    }

    public void jugarCarta(TipoJugador jugadorQueJugo, Carta carta) {
        if (ganador != null) return;

        if (jugadorQueJugo == TipoJugador.JUGADOR_1) {
            zonaJugador1.agregarCarta(carta);
            cartasJugador1Antes++;
        } else {
            zonaJugador2.agregarCarta(carta);
            cartasJugador2Antes++;
        }

        if (cartasJugador1Antes == cartasJugador2Antes) {
            System.out.println("[SERVIDOR] Fin de la mano " + (manoActual + 1) + ". Evaluando...");

            evaluarRonda();

            manoActual++;

            if (jugador1.getPuntos() >= PUNTOS_PARA_GANAR ||
                    jugador2.getPuntos() >= PUNTOS_PARA_GANAR) {

                estadoActual = EstadoTurno.PARTIDA_TERMINADA;

                if (jugador1.getPuntos() > jugador2.getPuntos()) {
                    ganador = jugador1;
                } else if (jugador2.getPuntos() > jugador1.getPuntos()) {
                    ganador = jugador2;
                }

                System.out.println("[SERVIDOR] Partida terminada. Ganador: " +
                        (ganador != null ? ganador.getNombre() : "Empate"));

            } else {
                estadoActual = (jugadorMano == TipoJugador.JUGADOR_1)
                        ? EstadoTurno.ESPERANDO_JUGADOR_1
                        : EstadoTurno.ESPERANDO_JUGADOR_2;

                System.out.println("[SERVIDOR] Siguiente turno. Estado: " + estadoActual);
            }
        } else {
            estadoActual = (jugadorQueJugo == TipoJugador.JUGADOR_1)
                    ? EstadoTurno.ESPERANDO_JUGADOR_2
                    : EstadoTurno.ESPERANDO_JUGADOR_1;

            System.out.println("[SERVIDOR] Carta jugada. Turno de: " + estadoActual);
        }
    }

    private void evaluarRonda() {
        System.out.println("\n[SERVIDOR] === EVALUANDO MANO " + (manoActual + 1) + " ===");

        ArrayList<Carta> cartasJug1 = zonaJugador1.getCartasJugadas();
        ArrayList<Carta> cartasJug2 = zonaJugador2.getCartasJugadas();

        int i = manoActual;

        if (i >= cartasJug1.size() || i >= cartasJug2.size()) {
            System.out.println("[SERVIDOR] Error: No hay cartas suficientes para evaluar mano " + i);
            return;
        }

        Carta cartaJug1 = cartasJug1.get(i);
        Carta cartaJug2 = cartasJug2.get(i);

        int puntosEnJuego = 1;

        if (trucoUsado && manoTrucoUsada == i) {
            puntosEnJuego = 2;
            System.out.println("[SERVIDOR] ¡TRUCO! Esta mano vale " + puntosEnJuego + " puntos");
        }

        if (cartaJug1.getJerarquia() < cartaJug2.getJerarquia()) {
            jugador1.sumarPuntos(puntosEnJuego);
            System.out.println("[SERVIDOR] Mano " + (i+1) + ": GANÓ " +
                    jugador1.getNombre() + " (+" + puntosEnJuego + " puntos)");
        } else if (cartaJug1.getJerarquia() > cartaJug2.getJerarquia()) {
            jugador2.sumarPuntos(puntosEnJuego);
            System.out.println("[SERVIDOR] Mano " + (i+1) + ": GANÓ " +
                    jugador2.getNombre() + " (+" + puntosEnJuego + " puntos)");
        } else {
            System.out.println("[SERVIDOR] Mano " + (i+1) + ": EMPATE (parda)");
            // Aquí podrías implementar la lógica de quién gana en parda si lo necesitas
        }

        System.out.println("[SERVIDOR] Resultado Parcial: " + jugador1.getNombre() + " " +
                jugador1.getPuntos() + " - " +
                jugador2.getPuntos() + " " + jugador2.getNombre());
    }

    public boolean cantarTruco(TipoJugador jugador) {
        if (trucoUsado) {
            System.out.println("[SERVIDOR] Truco ya fue usado");
            return false;
        }

        if (!esPrimerTurnoEnMano()) {
            System.out.println("[SERVIDOR] No es el primer turno");
            return false;
        }

        if (jugador != jugadorMano) {
            System.out.println("[SERVIDOR] Solo el jugador Mano puede cantar truco");
            return false;
        }

        trucoUsado = true;
        manoTrucoUsada = manoActual;
        jugadorQueCanto = jugador;

        String nombreJugador = (jugador == TipoJugador.JUGADOR_1)
                ? jugador1.getNombre()
                : jugador2.getNombre();

        System.out.println("[SERVIDOR] ✅ " + nombreJugador +
                " CANTÓ TRUCO válido en mano " + (manoActual + 1));

        return true;
    }
    public void resetearTotal() {
        System.out.println("[SERVIDOR] Realizando reseteo total de la partida...");

        // 1. Reiniciar variables de flujo
        this.manoActual = 0;
        this.ganador = null;
        this.cartasJugador1Antes = 0;
        this.cartasJugador2Antes = 0;

        // 2. Reiniciar el mazo (Barajar de nuevo)
        this.indiceMazo = 0;
        Collections.shuffle(mazoRevuelto);

        // 3. Resetear Truco
        resetearTruco();

        // 4. Limpiar Jugadores (Puntos y Manos)
        if (jugador1 != null) {
            jugador1.setPuntos(0); // Asegúrate de tener este setter en Jugador
            jugador1.limpiarMazo();
        }
        if (jugador2 != null) {
            jugador2.setPuntos(0);
            jugador2.limpiarMazo();
        }

        // 5. Limpiar Mesa (Zonas de juego)
        if (zonaJugador1 != null) zonaJugador1.limpiar();
        if (zonaJugador2 != null) zonaJugador2.limpiar();

        // 6. Reiniciar quién es mano aleatoriamente para la nueva partida
        this.jugadorMano = random.nextBoolean() ? TipoJugador.JUGADOR_1 : TipoJugador.JUGADOR_2;

        // 7. Establecer estado inicial
        this.estadoActual = (jugadorMano == TipoJugador.JUGADOR_1)
                ? EstadoTurno.ESPERANDO_JUGADOR_1
                : EstadoTurno.ESPERANDO_JUGADOR_2;

        System.out.println("[SERVIDOR] Partida reiniciada completamente. Puntos a 0. Esperando jugadores.");
    }

    public boolean esPrimerTurnoEnMano() {
        return cartasJugador1Antes == cartasJugador2Antes;
    }

    public boolean esTurnoJugador2() {
        return estadoActual == EstadoTurno.ESPERANDO_JUGADOR_2;
    }

    public boolean isTrucoUsado() {
        return trucoUsado;
    }

    public boolean isTrucoActivoEnManoActual() {
        return trucoUsado && manoTrucoUsada == manoActual;
    }

    public int getManoActual() {
        return manoActual;
    }

    public int getPuntosJ1() {
        return jugador1.getPuntos();
    }

    public int getPuntosJ2() {
        return jugador2.getPuntos();
    }

    public EstadoTurno getEstadoActual() {
        return estadoActual;
    }

    public TipoJugador getJugadorMano() {
        return jugadorMano;
    }

    public Jugador getGanador() {
        return ganador;
    }

    public int getManoTrucoUsada() {
        return manoTrucoUsada;
    }

    public Jugador getJugador1() {
        return jugador1;
    }
}
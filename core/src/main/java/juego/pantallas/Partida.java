package juego.pantallas;

import juego.elementos.*;
import juego.personajes.Jugador;
import juego.personajes.TipoJugador;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Partida {

    private ArrayList<Carta> mazoCompeto = new ArrayList<>();
    private ArrayList<Carta> mazoRevuelto = new ArrayList<>();
    private int indiceMazo = 0;

    private EstadoTurno estadoActual;
    private boolean trucoPendiente = false;
    private final int PUNTOS_PARA_GANAR = 15;
    private Jugador ganador = null;

    private ZonaJuego zonaJugador1;
    private ZonaJuego zonaJugador2;

    private Jugador jugador1;
    private Jugador jugador2;

    private int cartasJugador1Antes = 0;
    private int cartasJugador2Antes = 0;

    private int manoActual = 0;
    private final int MAX_MANOS = 3;

    private TipoJugador jugadorMano;
    private Random random = new Random();

    private EstadoTruco estadoTruco = EstadoTruco.SIN_TRUCO;
    private int manoTrucoUsada = -1;
    private TipoJugador ultimoQueCanto = null;

    public Partida() {
        this.estadoActual = EstadoTurno.ESPERANDO_JUGADOR_1;

        Mazo mazoOriginal = new Mazo();
        for (int i = 0; i < mazoOriginal.getCantCartas(); i++) {
            mazoCompeto.add(mazoOriginal.getCarta(i));
        }

        reinicializarMazo();
    }

    private void reinicializarMazo() {
        mazoRevuelto.clear();
        mazoRevuelto.addAll(mazoCompeto);
        Collections.shuffle(mazoRevuelto);
        indiceMazo = 0;
        System.out.println("[SERVIDOR] Mazo barajado y listo para repartir");
    }

    public void inicializar(ZonaJuego zonaJug1, ZonaJuego zonaJug2,
                            Jugador jug1, Jugador jug2, int manoActual) {
        this.zonaJugador1 = zonaJug1;
        this.zonaJugador2 = zonaJug2;
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
        this.estadoTruco = EstadoTruco.SIN_TRUCO;
        this.manoTrucoUsada = -1;
        this.ultimoQueCanto = null;
        this.trucoPendiente = false; // ✅ IMPORTANTE
    }

    public void repartirCartas(Jugador jugador1, Jugador jugador2) {
        if (indiceMazo + 6 > mazoRevuelto.size()) {
            reinicializarMazo();
        }

        jugador1.limpiarMazo();
        jugador2.limpiarMazo();

        for (int i = 0; i < 3; i++) {
            jugador1.agregarCarta(mazoRevuelto.get(indiceMazo++));
            jugador2.agregarCarta(mazoRevuelto.get(indiceMazo++));
        }

        System.out.println("[SERVIDOR] Cartas repartidas a ambos jugadores. Índice mazo: " + indiceMazo + "/" + mazoRevuelto.size());
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
        manoActual = 0;
        cartasJugador1Antes = 0;
        cartasJugador2Antes = 0;

        if (zonaJugador1 != null) zonaJugador1.limpiar();
        if (zonaJugador2 != null) zonaJugador2.limpiar();

        resetearTruco();

        jugadorMano = (jugadorMano == TipoJugador.JUGADOR_1)
                ? TipoJugador.JUGADOR_2
                : TipoJugador.JUGADOR_1;

        estadoActual = (jugadorMano == TipoJugador.JUGADOR_1)
                ? EstadoTurno.ESPERANDO_JUGADOR_1
                : EstadoTurno.ESPERANDO_JUGADOR_2;

        System.out.println("[SERVIDOR] Nueva ronda iniciada. Empieza: " + jugadorMano);
    }

    public void jugarCarta(TipoJugador jugadorQueJugo, Carta carta) {
        // ✅ BLOQUEAR SOLO SI HAY TRUCO PENDIENTE
        if (ganador != null || trucoPendiente) {
            System.out.println("[SERVIDOR] Carta bloqueada por trucoPendiente=" + trucoPendiente);
            return;
        }

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

        if (cartasJug1.size() <= manoActual || cartasJug2.size() <= manoActual) {
            System.out.println("[SERVIDOR] Error: No hay cartas suficientes para evaluar mano " + manoActual);
            return;
        }

        Carta cartaJug1 = cartasJug1.get(manoActual);
        Carta cartaJug2 = cartasJug2.get(manoActual);

        int resultadoMano = 0;

        if (cartaJug1.getJerarquia() < cartaJug2.getJerarquia()) {
            resultadoMano = 1;
            System.out.println("[SERVIDOR] Mano ganada por JUGADOR 1");
        } else if (cartaJug1.getJerarquia() > cartaJug2.getJerarquia()) {
            resultadoMano = 2;
            System.out.println("[SERVIDOR] Mano ganada por JUGADOR 2");
        } else {
            resultadoMano = 3;
            System.out.println("[SERVIDOR] Mano EMPATADA (Parda)");
        }

        int puntosEnJuego = 1;

        if (manoActual == 0) {
            puntosEnJuego = estadoTruco.getPuntos();
            System.out.println("[SERVIDOR] Puntos de la Mano 1/3 afectados por Truco: " + puntosEnJuego);
        } else {
            System.out.println("[SERVIDOR] Puntos de la Mano " + (manoActual + 1) + "/3: 1 (Valor Base)");
        }

        if (resultadoMano == 1) {
            jugador1.sumarPuntos(puntosEnJuego);
        } else if (resultadoMano == 2) {
            jugador2.sumarPuntos(puntosEnJuego);
        }
    }

    // ✅ CORRECCIÓN CRÍTICA DEL MÉTODO cantarTruco()
    public boolean cantarTruco(TipoJugador jugador) {
        System.out.println("[SERVIDOR] cantarTruco() llamado por: " + jugador);
        System.out.println("[SERVIDOR] Estado actual: estadoTruco=" + estadoTruco +
                ", manoActual=" + manoActual +
                ", trucoPendiente=" + trucoPendiente +
                ", jugadorMano=" + jugadorMano +
                ", ultimoQueCanto=" + ultimoQueCanto);

        // 1. Validar si se puede subir más
        if (!estadoTruco.puedeSubir()) {
            System.out.println("[SERVIDOR] ❌ No se puede subir más (ya estamos en Vale 4)");
            return false;
        }

        // 2. Solo en la primera mano
        if (manoActual != 0) {
            System.out.println("[SERVIDOR] ❌ Intento de truco fuera de mano 0");
            return false;
        }

        // 3. Lógica dividida: INICIAR vs RESPONDER
        if (estadoTruco == EstadoTruco.SIN_TRUCO) {
            // --- INICIAR TRUCO ---
            if (jugador != jugadorMano) {
                System.out.println("[SERVIDOR] ❌ Solo el jugador mano puede iniciar truco");
                return false;
            }
            if (!esPrimerTurnoEnMano()) {
                System.out.println("[SERVIDOR] ❌ Ya se jugaron cartas, no se puede iniciar truco");
                return false;
            }
        } else {
            // --- RESPONDER/SUBIR TRUCO ---
            if (ultimoQueCanto == jugador) {
                System.out.println("[SERVIDOR] ❌ No puedes responder tu propio canto");
                return false;
            }
        }

        // 4. Actualizar estado de Truco
        EstadoTruco estadoAnterior = estadoTruco;
        estadoTruco = estadoTruco.siguiente();
        manoTrucoUsada = 0;
        ultimoQueCanto = jugador;

        // ✅ CRÍTICO: MARCAR COMO PENDIENTE
        trucoPendiente = true;

        String nombreJugador = (jugador == TipoJugador.JUGADOR_1)
                ? jugador1.getNombre()
                : jugador2.getNombre();

        System.out.println("[SERVIDOR] ✅ " + nombreJugador +
                " CANTÓ " + estadoTruco + ". Esperando respuesta del rival...");
        System.out.println("[SERVIDOR] trucoPendiente=true, estadoActual=" + estadoActual + " (NO CAMBIA)");

        return true;
    }

    // ✅ MÉTODO MEJORADO
    public void aceptarTruco() {
        System.out.println("[SERVIDOR] Truco aceptado. Desbloqueando juego...");
        this.trucoPendiente = false; // ✅ Desbloquea
        // El estado del juego ya está correcto, solo desbloqueamos
    }

    // ✅ MÉTODO MEJORADO
    public void subirTruco(EstadoTruco nuevoEstado, TipoJugador quienCanto) {
        System.out.println("[SERVIDOR] Truco subido a: " + nuevoEstado + " por " + quienCanto);

        // ✅ ACTUALIZAR ESTADO PERO MANTENER BLOQUEADO
        this.estadoTruco = nuevoEstado;
        this.ultimoQueCanto = quienCanto;
        this.manoTrucoUsada = 0;

        // ✅ MANTENER trucoPendiente=true porque ahora el OTRO jugador debe responder
        // Se desbloqueará cuando ese jugador responda

        System.out.println("[SERVIDOR] Esperando respuesta al " + nuevoEstado);
    }

    public void resetearTotal() {
        System.out.println("[SERVIDOR] Realizando reseteo total de la partida...");

        this.manoActual = 0;
        this.ganador = null;
        this.cartasJugador1Antes = 0;
        this.cartasJugador2Antes = 0;

        reinicializarMazo();
        resetearTruco();

        if (jugador1 != null) {
            jugador1.setPuntos(0);
            jugador1.limpiarMazo();
        }
        if (jugador2 != null) {
            jugador2.setPuntos(0);
            jugador2.limpiarMazo();
        }

        if (zonaJugador1 != null) zonaJugador1.limpiar();
        if (zonaJugador2 != null) zonaJugador2.limpiar();

        this.jugadorMano = random.nextBoolean() ? TipoJugador.JUGADOR_1 : TipoJugador.JUGADOR_2;

        this.estadoActual = (jugadorMano == TipoJugador.JUGADOR_1)
                ? EstadoTurno.ESPERANDO_JUGADOR_1
                : EstadoTurno.ESPERANDO_JUGADOR_2;

        System.out.println("[SERVIDOR] Partida reiniciada completamente. Puntos a 0. Esperando jugadores.");
    }

    public boolean esPrimerTurnoEnMano() {
        return cartasJugador1Antes == 0 && cartasJugador2Antes == 0;
    }

    // Getters
    public int getManoActual() { return manoActual; }
    public int getPuntosJ1() { return jugador1.getPuntos(); }
    public int getPuntosJ2() { return jugador2.getPuntos(); }
    public EstadoTurno getEstadoActual() { return estadoActual; }
    public TipoJugador getJugadorMano() { return jugadorMano; }
    public Jugador getGanador() { return ganador; }
    public EstadoTruco getEstadoTruco() { return estadoTruco; }
    public int getManoTrucoUsada() { return manoTrucoUsada; }
    public TipoJugador getUltimoQueCanto() { return ultimoQueCanto; }
    public Jugador getJugador1() { return jugador1; }
    public boolean isTrucoPendiente() { return trucoPendiente; }
    public void setTrucoPendiente(boolean pendiente) { this.trucoPendiente = pendiente; }
}
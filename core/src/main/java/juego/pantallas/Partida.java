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

    private float delayFinalizacion = 0;
    private boolean esperandoFinalizacion = false;

    private int manoActual = 0;
    private final int MAX_MANOS = 3;

    private TipoJugador jugadorMano;  // Quién empieza la ronda
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

        // Determinar quién empieza de forma aleatoria
        this.jugadorMano = random.nextBoolean() ? TipoJugador.JUGADOR_1 : TipoJugador.JUGADOR_2;

        this.estadoActual = (jugadorMano == TipoJugador.JUGADOR_1)
                ? EstadoTurno.ESPERANDO_JUGADOR_1
                : EstadoTurno.ESPERANDO_JUGADOR_2;

        System.out.println("INICIO DE PARTIDA - Empieza: " +
                (jugadorMano == TipoJugador.JUGADOR_1 ? jug1.getNombre() : jug2.getNombre()));

        this.cartasJugador1Antes = 0;
        this.cartasJugador2Antes = 0;
        this.manoActual = manoActual;

        this.trucoUsado = false;
        this.manoTrucoUsada = -1;
        this.jugadorQueCanto = null;
    }

    public void repartirCartas(Jugador jugador1, Jugador jugador2) {
        if (indiceMazo + 6 > mazoRevuelto.size()) {
            indiceMazo = 0;
            Collections.shuffle(mazoRevuelto);
        }

        for (int i = 0; i < 3; i++) {
            jugador1.agregarCarta(mazoRevuelto.get(indiceMazo++));
            jugador2.agregarCarta(mazoRevuelto.get(indiceMazo++));
        }
    }

    public void update(float delta) {
        if (zonaJugador1 == null || zonaJugador2 == null) {
            return;
        }

        if (rivalBot != null) {
            rivalBot.update(delta);
        }

        if (esperandoFinalizacion) {
            delayFinalizacion += delta;
            if (delayFinalizacion >= 1.5f) {
                estadoActual = EstadoTurno.FINALIZANDO_MANO;
                esperandoFinalizacion = false;
                delayFinalizacion = 0;
            } else {
                return;
            }
        }

        switch (estadoActual) {
            case ESPERANDO_JUGADOR_1:
                int cartasJug1Actual = zonaJugador1.getCantidadCartas();

                if (cartasJug1Actual > cartasJugador1Antes) {
                    System.out.println(jugador1.getNombre() + " tiró una carta. Turno de " + jugador2.getNombre());
                    cartasJugador1Antes = cartasJug1Actual;

                    if (cartasJugador1Antes == cartasJugador2Antes) {
                        manoActual++;
                        System.out.println("=== Completada mano " + manoActual + " de " + MAX_MANOS + " ===");

                        if (manoActual >= MAX_MANOS) {
                            esperandoFinalizacion = true;
                            delayFinalizacion = 0;
                        } else {
                            // Siguiente mano - quien es "mano" sigue empezando
                            estadoActual = (jugadorMano == TipoJugador.JUGADOR_1)
                                    ? EstadoTurno.ESPERANDO_JUGADOR_1
                                    : EstadoTurno.ESPERANDO_JUGADOR_2;

                            if (estadoActual == EstadoTurno.ESPERANDO_JUGADOR_2 && rivalBot != null) {
                                rivalBot.activarTurno();
                            }
                        }
                    } else {
                        estadoActual = EstadoTurno.ESPERANDO_JUGADOR_2;
                        if (rivalBot != null) {
                            rivalBot.activarTurno();
                        }
                    }
                }
                break;

            case ESPERANDO_JUGADOR_2:
                boolean turnoJugador2Completo = false;

                if (rivalBot != null) {
                    turnoJugador2Completo = !rivalBot.isEsperandoTurno();
                } else {
                    turnoJugador2Completo = zonaJugador2.getCantidadCartas() > cartasJugador2Antes;
                }

                if (turnoJugador2Completo) {
                    int cartasJug2Actual = zonaJugador2.getCantidadCartas();

                    if (cartasJug2Actual > cartasJugador2Antes) {
                        System.out.println(jugador2.getNombre() + " tiró una carta. Turno de " + jugador1.getNombre());
                        cartasJugador2Antes = cartasJug2Actual;

                        if (cartasJugador1Antes == cartasJugador2Antes) {
                            manoActual++;
                            System.out.println("=== Completada mano " + manoActual + " de " + MAX_MANOS + " ===");

                            if (manoActual >= MAX_MANOS) {
                                esperandoFinalizacion = true;
                                delayFinalizacion = 0;
                            } else {
                                // Siguiente mano - quien es "mano" sigue empezando
                                estadoActual = (jugadorMano == TipoJugador.JUGADOR_1)
                                        ? EstadoTurno.ESPERANDO_JUGADOR_1
                                        : EstadoTurno.ESPERANDO_JUGADOR_2;

                                if (estadoActual == EstadoTurno.ESPERANDO_JUGADOR_2 && rivalBot != null) {
                                    rivalBot.activarTurno();
                                }
                            }
                        } else {
                            estadoActual = EstadoTurno.ESPERANDO_JUGADOR_1;
                        }
                    }
                }
                break;

            case FINALIZANDO_MANO:
                evaluarRonda();
                break;

            case PARTIDA_TERMINADA:
                break;
        }
    }

    private void evaluarRonda() {
        System.out.println("\n=== EVALUANDO RONDA ===");
        System.out.println("Cartas " + jugador1.getNombre() + " en zona: " + zonaJugador1.getCantidadCartas());
        System.out.println("Cartas " + jugador2.getNombre() + " en zona: " + zonaJugador2.getCantidadCartas());

        ArrayList<Carta> cartasJug1 = zonaJugador1.getCartasJugadas();
        ArrayList<Carta> cartasJug2 = zonaJugador2.getCartasJugadas();

        System.out.println("\nCartas de " + jugador1.getNombre() + ":");
        for (Carta c : cartasJug1) {
            System.out.println("  - " + c.getNombre() + " (Jerarquía: " + c.getJerarquia() + ")");
        }

        System.out.println("\nCartas de " + jugador2.getNombre() + ":");
        for (Carta c : cartasJug2) {
            System.out.println("  - " + c.getNombre() + " (Jerarquía: " + c.getJerarquia() + ")");
        }

        // Evaluar cada mano
        for (int i = 0; i < Math.min(cartasJug1.size(), cartasJug2.size()); i++) {
            Carta cartaJug1 = cartasJug1.get(i);
            Carta cartaJug2 = cartasJug2.get(i);

            int puntosEnJuego = 1;
            if (trucoUsado && manoTrucoUsada == i) {
                puntosEnJuego = 2;
                System.out.println("¡TRUCO! Esta mano vale " + puntosEnJuego + " puntos");
            }

            // Jerarquía menor = carta más fuerte
            if (cartaJug1.getJerarquia() < cartaJug2.getJerarquia()) {
                jugador1.sumarPuntos(puntosEnJuego);
                System.out.println("Mano " + (i+1) + ": GANÓ " + jugador1.getNombre() +
                        " (+" + puntosEnJuego + " puntos)");
            } else if (cartaJug1.getJerarquia() > cartaJug2.getJerarquia()) {
                jugador2.sumarPuntos(puntosEnJuego);
                System.out.println("Mano " + (i+1) + ": GANÓ " + jugador2.getNombre() +
                        " (+" + puntosEnJuego + " puntos)");
            } else {
                System.out.println("Mano " + (i+1) + ": EMPATE (parda)");
            }
        }

        System.out.println("\nResultado: " + jugador1.getNombre() + " " +
                jugador1.getPuntos() + " - " +
                jugador2.getPuntos() + " " + jugador2.getNombre());

        if (jugador1.getPuntos() >= PUNTOS_PARA_GANAR) {
            ganador = jugador1;
            estadoActual = EstadoTurno.PARTIDA_TERMINADA;
            System.out.println("\n🏆 ¡GANADOR: " + jugador1.getNombre() + "!");
        } else if (jugador2.getPuntos() >= PUNTOS_PARA_GANAR) {
            ganador = jugador2;
            estadoActual = EstadoTurno.PARTIDA_TERMINADA;
            System.out.println("\n🏆 ¡GANADOR: " + jugador2.getNombre() + "!");
        }
    }

    public boolean esTurnoJugador1() {
        return estadoActual == EstadoTurno.ESPERANDO_JUGADOR_1;
    }

    public boolean esTurnoJugador() {
        return esTurnoJugador1();
    }

    public boolean esTurnoJugador2() {
        return estadoActual == EstadoTurno.ESPERANDO_JUGADOR_2;
    }

    public boolean rondaTerminada() {
        return estadoActual == EstadoTurno.FINALIZANDO_MANO && ganador == null;
    }

    public boolean partidaTerminada() {
        return estadoActual == EstadoTurno.PARTIDA_TERMINADA;
    }

    public Jugador getGanador() {
        return ganador;
    }

    public void nuevaRonda() {
        // Alternar quién es mano
        jugadorMano = (jugadorMano == TipoJugador.JUGADOR_1)
                ? TipoJugador.JUGADOR_2
                : TipoJugador.JUGADOR_1;

        estadoActual = (jugadorMano == TipoJugador.JUGADOR_1)
                ? EstadoTurno.ESPERANDO_JUGADOR_1
                : EstadoTurno.ESPERANDO_JUGADOR_2;

        System.out.println("NUEVA RONDA - Empieza: " +
                (jugadorMano == TipoJugador.JUGADOR_1 ? jugador1.getNombre() : jugador2.getNombre()));

        cartasJugador1Antes = 0;
        cartasJugador2Antes = 0;
        manoActual = 0;

        if (zonaJugador1 != null) zonaJugador1.limpiar();
        if (zonaJugador2 != null) zonaJugador2.limpiar();

        trucoUsado = false;
        manoTrucoUsada = -1;
        jugadorQueCanto = null;

        // Si empieza el jugador 2 y es un bot, activarlo
        if (jugadorMano == TipoJugador.JUGADOR_2 && rivalBot != null) {
            rivalBot.activarTurno();
        }
    }

    public int getManoActual() {
        return manoActual;
    }

    // ✅ NUEVO: Verificar si es el turno del primero que tira en esta mano
    public boolean esPrimerTurnoEnMano() {
        // Es primer turno si nadie tiró aún en esta mano
        return cartasJugador1Antes == cartasJugador2Antes;
    }

    // ✅ NUEVO: Verificar si el jugador 1 es quien empieza (es mano)
    public boolean jugador1EmpiezaEstaMano() {
        return jugadorMano == TipoJugador.JUGADOR_1;
    }

    public boolean cantarTruco(TipoJugador jugador) {
        // ✅ NUEVO: Solo puede cantar truco quien tira primero
        if (!esPrimerTurnoEnMano()) {
            System.out.println("Solo puede cantar truco quien tira primero en la mano");
            return false;
        }


        if (jugador == TipoJugador.JUGADOR_1 && jugadorMano != TipoJugador.JUGADOR_1) {
            System.out.println("No es tu turno para cantar truco");
            return false;
        }

        if (trucoUsado) {
            System.out.println("El truco ya fue cantado en esta ronda");
            return false;
        }

        trucoUsado = true;
        manoTrucoUsada = manoActual;
        jugadorQueCanto = jugador;

        String nombreJugador = (jugador == TipoJugador.JUGADOR_1)
                ? jugador1.getNombre()
                : jugador2.getNombre();

        System.out.println("¡" + nombreJugador + " CANTÓ TRUCO! La mano " +
                (manoActual + 1) + " vale 2 puntos");

        return true;
    }

    public boolean isTrucoUsado() {
        return trucoUsado;
    }

    public boolean isTrucoActivoEnManoActual() {
        return trucoUsado && manoTrucoUsada == manoActual;
    }
    public void procesarJugadaServidor(TipoJugador jugadorQueJugo, int valor, Palo palo) {
        Carta carta = new Carta(valor, palo);
        if (jugadorQueJugo == TipoJugador.JUGADOR_1) {
            zonaJugador1.agregarCarta(carta);
            cartasJugador1Antes = zonaJugador1.getCantidadCartas();
        } else {
            zonaJugador2.agregarCarta(carta);
            cartasJugador2Antes = zonaJugador2.getCantidadCartas();
        }

        // 3. Verificar lógica de turno (copiada y adaptada de tu update)
        if (cartasJugador1Antes == cartasJugador2Antes) {
            // Ambos jugaron, fin de la mano actual
            manoActual++;

            evaluarRonda();

            if (manoActual >= MAX_MANOS || ganador != null) {
                estadoActual = EstadoTurno.PARTIDA_TERMINADA; // O finalizando
            } else {
                // Siguiente mano
                estadoActual = (jugadorMano == TipoJugador.JUGADOR_1)
                        ? EstadoTurno.ESPERANDO_JUGADOR_1
                        : EstadoTurno.ESPERANDO_JUGADOR_2;
            }
        } else {
            // Falta que juegue el otro
            estadoActual = (jugadorQueJugo == TipoJugador.JUGADOR_1)
                    ? EstadoTurno.ESPERANDO_JUGADOR_2
                    : EstadoTurno.ESPERANDO_JUGADOR_1;
        }
    }

    // Necesitas getters para que el servidor pueda leer el estado y enviarlo
    public int getPuntosJ1() { return jugador1.getPuntos(); }
    public int getPuntosJ2() { return jugador2.getPuntos(); }

    public EstadoTurno getEstadoActual() {
        return estadoActual;
    }
    public TipoJugador getJugadorMano() {
        return jugadorMano;
    }
    public void jugarCarta(TipoJugador jugadorQueJugo, Carta carta) {
        if (ganador != null) return;

        // 1. Agregar carta a la zona de juego y actualizar contadores
        if (jugadorQueJugo == TipoJugador.JUGADOR_1) {
            zonaJugador1.agregarCarta(carta);
            cartasJugador1Antes++; // 1 carta jugada por J1
        } else {
            zonaJugador2.agregarCarta(carta);
            cartasJugador2Antes++; // 1 carta jugada por J2
        }

        // 2. Lógica de Cambio de Turno / Fin de Mano
        if (cartasJugador1Antes == cartasJugador2Antes) {
            // **********************************************
            // AMBOS JUGARON UNA CARTA: FIN DE MANO
            // **********************************************

            System.out.println("[PARTIDA] Fin de la mano " + (manoActual + 1) + ". Evaluando...");

            // Llamar a la lógica de puntuación y ganadores de mano
            // (Debes asegurar que 'evaluarRonda()' existe y funciona)
            evaluarRonda();

            // Resetear contadores y limpiar mesa visualmente
            cartasJugador1Antes = 0;
            cartasJugador2Antes = 0;
            zonaJugador1.limpiar();
            zonaJugador2.limpiar();

            manoActual++;

            if (manoActual >= MAX_MANOS || jugador1.getPuntos() >= PUNTOS_PARA_GANAR || jugador2.getPuntos() >= PUNTOS_PARA_GANAR) {
                // FIN DE PARTIDA
                estadoActual = EstadoTurno.PARTIDA_TERMINADA;
                // Lógica para determinar el ganador final
                if (jugador1.getPuntos() > jugador2.getPuntos()) ganador = jugador1;
                else if (jugador2.getPuntos() > jugador1.getPuntos()) ganador = jugador2;

                System.out.println("[PARTIDA] Partida terminada. Ganador: " + (ganador != null ? ganador.getNombre() : "Empate"));

            } else {
                // AVANZAR A LA SIGUIENTE MANO: Vuelve a empezar quien es 'jugadorMano' (o quien ganó la mano anterior)
                // Asumo que el ganador de la mano anterior establece el nuevo 'jugadorMano' en evaluarRonda().
                estadoActual = (jugadorMano == TipoJugador.JUGADOR_1)
                        ? EstadoTurno.ESPERANDO_JUGADOR_1
                        : EstadoTurno.ESPERANDO_JUGADOR_2;
                System.out.println("[PARTIDA] Empezando mano " + (manoActual + 1) + ". Turno de: " + estadoActual);
            }
        } else {
            // **********************************************
            // SOLO JUGÓ UNO: CAMBIO DE TURNO
            // **********************************************
            estadoActual = (jugadorQueJugo == TipoJugador.JUGADOR_1)
                    ? EstadoTurno.ESPERANDO_JUGADOR_2 // Cambia el turno al J2
                    : EstadoTurno.ESPERANDO_JUGADOR_1; // Cambia el turno al J1

            System.out.println("[PARTIDA] Carta jugada. Turno de: " + estadoActual);
        }
    }


}
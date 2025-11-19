package juego.pantallas;

import juego.elementos.*;
import juego.personajes.Jugador;
import juego.personajes.RivalBot;
import juego.utilidades.Aleatorio;
import juego.utilidades.Global;

import java.util.ArrayList;
import java.util.Collections;


public class Partida {

    private ArrayList<Carta> mazoRevuelto = new ArrayList<>();
    private int indiceMazo = 0;

    public enum TipoJugador { JUGADOR_1, JUGADOR_2 }


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

    private final int MAX_MANOS = 3;

    private TipoJugador jugadorMano;

    private boolean trucoUsado = false;
    private int manoTrucoUsada = -1;
    private TipoJugador jugadorQueCanto = null;

    public Partida() {
        Mazo mazoOriginal = new Mazo();
        for (int i = 0; i < mazoOriginal.getCantCartas(); i++) {
            mazoRevuelto.add(mazoOriginal.getCarta(i));
        }
        Collections.shuffle(mazoRevuelto);
    }

    public void inicializar(ZonaJuego zonaJug1, ZonaJuego zonaJug2, RivalBot bot,
                            Jugador jug1, Jugador jug2) {
        this.zonaJugador1 = zonaJug1;
        this.zonaJugador2 = zonaJug2;
        this.rivalBot = bot;
        this.jugador1 = jug1;
        this.jugador2 = jug2;

        // Determinar quién empieza de forma aleatoria
        this.jugadorMano = Aleatorio.r.nextBoolean() ? TipoJugador.JUGADOR_1 : TipoJugador.JUGADOR_2;

        Global.estadoTurno = (jugadorMano == TipoJugador.JUGADOR_1)
                ? Global.EstadoTurno.ESPERANDO_JUGADOR_1
                : Global.EstadoTurno.ESPERANDO_JUGADOR_2;

        System.out.println("INICIO DE PARTIDA - Empieza: " +
                (jugadorMano == TipoJugador.JUGADOR_1 ? jug1.getNombre() : jug2.getNombre()));

        this.cartasJugador1Antes = 0;
        this.cartasJugador2Antes = 0;

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
                Global.estadoTurno = Global.EstadoTurno.FINALIZANDO_MANO;
                esperandoFinalizacion = false;
                delayFinalizacion = 0;
            } else {
                return;
            }
        }

        switch (Global.estadoTurno) {
            case ESPERANDO_JUGADOR_1:
                int cartasJug1Actual = zonaJugador1.getCantidadCartas();

                if (cartasJug1Actual > cartasJugador1Antes) {
                    System.out.println(jugador1.getNombre() + " tiró una carta. Turno de " + jugador2.getNombre());
                    cartasJugador1Antes = cartasJug1Actual;

                    if (cartasJugador1Antes == cartasJugador2Antes) {
                        Global.manoActual++;
                        System.out.println("=== Completada mano " + Global.manoActual + " de " + MAX_MANOS + " ===");

                        if (Global.manoActual >= MAX_MANOS) {
                            esperandoFinalizacion = true;
                            delayFinalizacion = 0;
                        } else {
                            // Siguiente mano - quien es "mano" sigue empezando
                            Global.estadoTurno = (jugadorMano == TipoJugador.JUGADOR_1)
                                    ? Global.EstadoTurno.ESPERANDO_JUGADOR_1
                                    : Global.EstadoTurno.ESPERANDO_JUGADOR_2;

                            if (Global.estadoTurno == Global.EstadoTurno.ESPERANDO_JUGADOR_2 && rivalBot != null) {
                                rivalBot.activarTurno();
                            }
                        }
                    } else {
                        Global.estadoTurno = Global.EstadoTurno.ESPERANDO_JUGADOR_2;
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
                            Global.manoActual++;
                            System.out.println("=== Completada mano " + Global.manoActual + " de " + MAX_MANOS + " ===");

                            if (Global.manoActual >= MAX_MANOS) {
                                esperandoFinalizacion = true;
                                delayFinalizacion = 0;
                            } else {
                                // Siguiente mano - quien es "mano" sigue empezando
                                Global.estadoTurno = (jugadorMano == TipoJugador.JUGADOR_1)
                                        ? Global.EstadoTurno.ESPERANDO_JUGADOR_1
                                        : Global.EstadoTurno.ESPERANDO_JUGADOR_2;

                                if (Global.estadoTurno == Global.EstadoTurno.ESPERANDO_JUGADOR_2 && rivalBot != null) {
                                    rivalBot.activarTurno();
                                }
                            }
                        } else {
                            Global.estadoTurno = Global.EstadoTurno.ESPERANDO_JUGADOR_1;
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
                Global.puntosJ1 += puntosEnJuego;
                System.out.println("Mano " + (i + 1) + ": GANÓ " + jugador1.getNombre() +
                        " (+" + puntosEnJuego + " puntos)");
            } else if (cartaJug1.getJerarquia() > cartaJug2.getJerarquia()) {
                Global.puntosJ2 += puntosEnJuego;
                System.out.println("Mano " + (i + 1) + ": GANÓ " + jugador2.getNombre() +
                        " (+" + puntosEnJuego + " puntos)");
            } else {
                System.out.println("Mano " + (i + 1) + ": EMPATE (parda)");
            }
        }

        System.out.println("\nResultado: " + jugador1.getNombre() + " " +
                Global.puntosJ1 + " - " +
                Global.puntosJ2 + " " + jugador2.getNombre());

        if (Global.puntosJ1 >= PUNTOS_PARA_GANAR) {
            ganador = jugador1;
            Global.estadoTurno = Global.EstadoTurno.PARTIDA_TERMINADA;
        } else if (Global.puntosJ2 >= PUNTOS_PARA_GANAR) {
            ganador = jugador2;
            Global.estadoTurno = Global.EstadoTurno.PARTIDA_TERMINADA;
        }
    }

    public boolean esTurnoJugador1() {
        return Global.estadoTurno == Global.EstadoTurno.ESPERANDO_JUGADOR_1;
    }

    public boolean esTurnoJugador() {
        return esTurnoJugador1();
    }

    public boolean esTurnoJugador2() {
        return Global.estadoTurno == Global.EstadoTurno.ESPERANDO_JUGADOR_2;
    }

    public boolean rondaTerminada() {
        return Global.estadoTurno == Global.EstadoTurno.FINALIZANDO_MANO && ganador == null;
    }

    public boolean partidaTerminada() {
        return Global.estadoTurno == Global.EstadoTurno.PARTIDA_TERMINADA;
    }

    public Jugador getGanador() {
        return ganador;
    }

    public void nuevaRonda() {
        // Alternar quién es mano
        jugadorMano = (jugadorMano == TipoJugador.JUGADOR_1)
                ? TipoJugador.JUGADOR_2
                : TipoJugador.JUGADOR_1;

        Global.estadoTurno = (jugadorMano == TipoJugador.JUGADOR_1)
                ? Global.EstadoTurno.ESPERANDO_JUGADOR_1
                : Global.EstadoTurno.ESPERANDO_JUGADOR_2;

        System.out.println("NUEVA RONDA - Empieza: " +
                (jugadorMano == TipoJugador.JUGADOR_1 ? jugador1.getNombre() : jugador2.getNombre()));

        cartasJugador1Antes = 0;
        cartasJugador2Antes = 0;
        Global.manoActual = 0;

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
        return Global.manoActual;
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
        manoTrucoUsada = Global.manoActual;
        jugadorQueCanto = jugador;

        String nombreJugador = (jugador == TipoJugador.JUGADOR_1)
                ? jugador1.getNombre()
                : jugador2.getNombre();

        System.out.println("¡" + nombreJugador + " CANTÓ TRUCO! La mano " +
                (Global.manoActual + 1) + " vale 2 puntos");

        return true;
    }

    public boolean isTrucoUsado() {
        return trucoUsado;
    }

    public boolean isTrucoActivoEnManoActual() {
        return trucoUsado && manoTrucoUsada == Global.manoActual;
    }

    public int getManoTrucoUsada() {
        return manoTrucoUsada;
    }
}
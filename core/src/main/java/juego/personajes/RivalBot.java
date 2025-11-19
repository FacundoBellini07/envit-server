package juego.personajes;

import juego.elementos.Carta;
import juego.elementos.ZonaJuego;
import juego.pantallas.Partida;

import java.util.Random;

/**
 * Bot provisorio para simular un rival.
 * Tira cartas de forma automática con un delay.
 * ✅ ACTUALIZADO: Solo puede cantar truco si es el primer turno de la mano
 *
 * TODO: Reemplazar con sistema online en la versión final.
 */
public class RivalBot {

    private Jugador jugador;
    private ZonaJuego zonaJuego;
    private Random random;

    // Estado del bot
    private boolean esperandoTurno = false;
    private float tiempoEspera = 0f;
    private float DELAY_ENTRE_CARTAS = 2.0f;

    // Índice de la carta que va a jugar
    private int siguienteCartaIndex = 0;

    // Referencia a la partida para poder cantar truco
    private Partida partida = null;

    // Probabilidad de cantar truco (30%)
    private float PROBABILIDAD_TRUCO = 0.30f;

    public RivalBot(Jugador jugador, ZonaJuego zona) {
        this.jugador = jugador;
        this.zonaJuego = zona;
        this.random = new Random();
    }

    /**
     * Vincular la partida al bot para que pueda cantar truco
     */
    public void setPartida(Partida partida) {
        this.partida = partida;
    }

    /**
     * Actualiza el estado del bot (llamar en cada frame)
     */
    public void update(float delta) {
        if (!esperandoTurno) {
            return;
        }

        tiempoEspera += delta;

        // Cuando pasa el tiempo de espera, tira una carta
        if (tiempoEspera >= DELAY_ENTRE_CARTAS) {
            // ✅ ACTUALIZADO: Antes de tirar la carta, considerar si cantar truco
            // (solo si es el primer turno de la mano)
            considerarCantarTruco();

            tirarCarta();
            tiempoEspera = 0f;
            esperandoTurno = false;
        }
    }

    /**
     * Activa el turno del bot para que tire una carta
     */
    public void activarTurno() {
        if (todasLasCartasJugadas()) {
            System.out.println("El bot ya jugó todas sus cartas");
            return;
        }

        esperandoTurno = true;
        tiempoEspera = 0f;
    }

    /**
     * El bot elige y tira una carta (lógica súper simple)
     */
    private void tirarCarta() {
        Carta[] mano = jugador.getMano();

        // Buscar la primera carta disponible (que no esté en la zona)
        for (int i = 0; i < mano.length; i++) {
            Carta carta = mano[i];

            if (carta != null && !zonaJuego.contieneCartaJugada(carta)) {
                // Tirar esta carta
                zonaJuego.agregarCarta(carta);
                System.out.println("Bot jugó: " + carta.getNombre());
                return;
            }
        }
    }

    /**
     * Tira una carta aleatoria (estrategia alternativa)
     */
    public void tirarCartaAleatoria() {
        Carta[] mano = jugador.getMano();

        // Hacer una lista de cartas disponibles
        int[] disponibles = new int[3];
        int count = 0;

        for (int i = 0; i < mano.length; i++) {
            if (mano[i] != null && !zonaJuego.contieneCartaJugada(mano[i])) {
                disponibles[count++] = i;
            }
        }

        if (count > 0) {
            // Elegir una al azar
            int indexAleatorio = disponibles[random.nextInt(count)];
            Carta carta = mano[indexAleatorio];
            zonaJuego.agregarCarta(carta);
            System.out.println("Bot jugó (aleatorio): " + carta.getNombre());
        }
    }

    /**
     * ✅ ACTUALIZADO: El bot considera si cantar truco antes de jugar
     * Solo puede hacerlo si es el primer turno de la mano
     */
    private void considerarCantarTruco() {
        if (partida == null || partida.isTrucoUsado()) {
            return; // No hay partida vinculada o el truco ya fue usado
        }

        // ✅ NUEVO: Solo intentar si es el primer turno de la mano
        if (!partida.esPrimerTurnoEnMano()) {
            return;
        }

        // ✅ NUEVO: Solo intentar si es turno del jugador 2
        if (!partida.esTurnoJugador2()) {
            return;
        }

        // El bot tiene una probabilidad de cantar truco
        if (random.nextFloat() < PROBABILIDAD_TRUCO) {
            boolean exito = partida.cantarTruco(TipoJugador.JUGADOR_2);
            if (exito) {
                System.out.println("¡El BOT cantó TRUCO!");
            }
        }
    }

    /**
     * Cambiar la probabilidad de que el bot cante truco
     * @param probabilidad Entre 0.0 (0%) y 1.0 (100%)
     */
    public void setProbabilidadTruco(float probabilidad) {
        this.PROBABILIDAD_TRUCO = Math.max(0f, Math.min(1f, probabilidad));
    }

    /**
     * Verifica si el bot ya jugó todas sus cartas
     */
    public boolean todasLasCartasJugadas() {
        Carta[] mano = jugador.getMano();

        for (Carta carta : mano) {
            if (carta != null && !zonaJuego.contieneCartaJugada(carta)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Obtiene cuántas cartas jugó el bot
     */
    public int getCartasJugadas() {
        int count = 0;
        Carta[] mano = jugador.getMano();

        for (Carta carta : mano) {
            if (carta != null && zonaJuego.contieneCartaJugada(carta)) {
                count++;
            }
        }

        return count;
    }

    /**
     * Reinicia el estado del bot para una nueva ronda
     */
    public void reset() {
        esperandoTurno = false;
        tiempoEspera = 0f;
        siguienteCartaIndex = 0;
    }

    /**
     * Cambia el delay entre cartas (para hacerlo más rápido/lento)
     */
    public void setDelay(float segundos) {
        this.DELAY_ENTRE_CARTAS = segundos;
    }

    public Jugador getJugador() {
        return jugador;
    }

    public boolean isEsperandoTurno() {
        return esperandoTurno;
    }
}
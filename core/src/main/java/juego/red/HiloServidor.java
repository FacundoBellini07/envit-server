package juego.red;

import juego.elementos.Carta;
import juego.elementos.EstadoTurno;
import juego.elementos.Palo;
import juego.pantallas.Partida;
import juego.personajes.Jugador;
import juego.personajes.TipoJugador;

import java.io.IOException;
import java.net.*;
import java.util.Timer;
import java.util.TimerTask;

public class HiloServidor extends Thread {
    private DatagramSocket conexion;
    private boolean fin = false;
    private Cliente[] clientes = new Cliente[2];
    private int cantClientes = 0;

    private Partida partidaLogica;
    private boolean esperandoNuevaRonda = false;

    public HiloServidor(Partida partida) {
        this.partidaLogica = partida;
        try {
            conexion = new DatagramSocket(30243);
            System.out.println("[SERVIDOR] Escuchando en puerto 30243");

            // ✅ NUEVO: Detector de desconexiones forzadas
            new Timer().scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    chequearDesconexiones();
                }
            }, 2000, 1000); // Revisa cada 1 segundo

        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void enviarMensaje(String mensaje, InetAddress ip, int puerto) {
        byte[] data = mensaje.getBytes();
        DatagramPacket dp = new DatagramPacket(data, data.length, ip, puerto);
        try {
            conexion.send(dp);
            System.out.println("[SERVIDOR] Enviado a " + ip + ":" + puerto + ": " + mensaje);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void enviarAmbos(String mensaje) {
        if (cantClientes >= 2) {
            enviarMensaje(mensaje, clientes[0].getIp(), clientes[0].getPuerto());
            enviarMensaje(mensaje, clientes[1].getIp(), clientes[1].getPuerto());
        }
    }
    private void chequearDesconexiones() {
        if (cantClientes == 0) return;
        long tiempoActual = System.currentTimeMillis();
        final long TIEMPO_LIMITE = 3000;

        boolean desconexionDetectada = false;

        for (int i = 0; i < cantClientes; i++) {
            Cliente cliente = clientes[i];
            if (cliente != null) {
                if (tiempoActual - cliente.getUltimoMensaje() > TIEMPO_LIMITE) {
                    System.out.println("[SERVIDOR] 🚨 Cliente " + i + " ha excedido el tiempo de espera (TIMEOUT). Forzando desconexión.");
                    desconexionDetectada = true;
                    // Al detectar una desconexión, no necesitamos seguir chequeando
                    break;
                }
            }
        }

        if (desconexionDetectada) {
            enviarAmbos("RIVAL_SE_FUE");

            cantClientes = 0;
            clientes = new Cliente[2]; // Limpiar el array para recibir nuevas conexiones
            esperandoNuevaRonda = false; // Liberar cualquier bloqueo del servidor
            partidaLogica.resetearTotal();

            System.out.println("[SERVIDOR] ✅ Desconexión forzada completada. Sala reiniciada. Esperando jugadores...");
        }
    }

    @Override
    public void run() {
        do {
            byte[] data = new byte[1024];
            DatagramPacket dp = new DatagramPacket(data, data.length);
            try {
                conexion.receive(dp);
                procesarMensaje(dp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } while (!fin);
    }

    private void procesarMensaje(DatagramPacket dp) {
        String mensaje = (new String(dp.getData())).trim();
        System.out.println("[SERVIDOR] Recibido de " + dp.getAddress() + ": " + mensaje);

        int idxCliente = getIndiceCliente(dp.getAddress(), dp.getPort());
        if (idxCliente != -1) {
            clientes[idxCliente].actualizarUltimoMensaje();
        }

        if (mensaje.equals("PING")) {
            return;
        }

        if (mensaje.equals("Conexion")) {
            procesarConexion(dp);
        }

        else if (mensaje.startsWith("CARTA_JUGADA:")) {
            procesarCartaJugada(dp, mensaje);
        } else if (mensaje.equals("TRUCO")) {
            procesarTruco(dp);
        }
    }

    private void procesarConexion(DatagramPacket dp) {

        if (cantClientes < 2) {
            clientes[cantClientes++] = new Cliente(dp.getAddress(), dp.getPort());
            int idx = cantClientes - 1;
            enviarMensaje("OK", clientes[idx].getIp(), clientes[idx].getPuerto());
            enviarMensaje("ID:" + idx, clientes[idx].getIp(), clientes[idx].getPuerto());

            if (cantClientes == 2) {
                iniciarPartida();
            }
        }
        else {
            System.out.println("[SERVIDOR] Rechazando conexión (FULL)");
            enviarMensaje("FULL", dp.getAddress(), dp.getPort());
            return;
        }
    }

    private void iniciarPartida() {
        System.out.println("[SERVIDOR] Iniciando partida con 2 jugadores");

        partidaLogica.repartirNuevasCartas();

        TipoJugador quienEmpieza = partidaLogica.getJugadorMano();
        int idEmpieza = (quienEmpieza == TipoJugador.JUGADOR_1) ? 0 : 1;
        enviarAmbos("EMPIEZA:" + idEmpieza);

        repartirCartasAJugadores();
        enviarEstadoActual();
    }

    private void repartirCartasAJugadores() {
        System.out.println("[SERVIDOR] Repartiendo cartas a los jugadores");

        Carta[] cartasJ1 = partidaLogica.getCartasJugador1();
        Carta[] cartasJ2 = partidaLogica.getCartasJugador2();

        enviarCartasAJugador(0, cartasJ1);
        enviarCartasAJugador(1, cartasJ2);
    }

    private void enviarCartasAJugador(int idJugador, Carta[] cartas) {
        if (cartas == null || cartas.length < 3) {
            System.err.println("[SERVIDOR] Error: Cartas insuficientes para jugador " + idJugador);
            return;
        }

        StringBuilder mensaje = new StringBuilder("CARTAS:");
        for (int i = 0; i < 3; i++) {
            if (cartas[i] != null) {
                mensaje.append(cartas[i].getValor())
                        .append(":")
                        .append(cartas[i].getPalo().name());
                if (i < 2) mensaje.append(",");
            }
        }

        enviarMensaje(
                mensaje.toString(),
                clientes[idJugador].getIp(),
                clientes[idJugador].getPuerto()
        );

        System.out.println("[SERVIDOR] Cartas enviadas a jugador " + idJugador + ": " + mensaje);
    }

    private void procesarCartaJugada(DatagramPacket dp, String mensaje) {
        if (esperandoNuevaRonda) return;
        String[] partes = mensaje.split(":");
        if (partes.length >= 3) {
            int valor = Integer.parseInt(partes[1]);
            Palo palo = Palo.valueOf(partes[2]);

            int idx = getIndiceCliente(dp.getAddress(), dp.getPort());
            if (idx == -1) return;

            TipoJugador jugadorQueJugo = (idx == 0) ? TipoJugador.JUGADOR_1 : TipoJugador.JUGADOR_2;

            Carta cartaJugada = new Carta(valor, palo);

            partidaLogica.jugarCarta(jugadorQueJugo, cartaJugada);

            int rival = (idx == 0) ? 1 : 0;
            enviarMensaje(
                    "CARTA_RIVAL:" + valor + ":" + palo.name(),
                    clientes[rival].getIp(),
                    clientes[rival].getPuerto()
            );

            enviarEstadoActual();
            if (partidaLogica.getEstadoActual() == EstadoTurno.PARTIDA_TERMINADA) {
                Jugador ganador = partidaLogica.getGanador();
                int idGanador = (ganador == partidaLogica.getJugador1()) ? 0 : 1;

                System.out.println("[SERVIDOR] ¡PARTIDA TERMINADA! Ganó ID: " + idGanador);
                enviarAmbos("GANADOR:" + idGanador);
            }
           else if (partidaLogica.rondaCompletada()) {
                System.out.println("[SERVIDOR] ¡Ronda completada! Esperando 2s para la siguiente...");

                esperandoNuevaRonda = true;

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        iniciarNuevaRonda();

                        esperandoNuevaRonda = false;
                    }
                }, 2000);
            }
        }
    }

    private void iniciarNuevaRonda() {
        System.out.println("[SERVIDOR] Iniciando nueva ronda");

        partidaLogica.repartirNuevasCartas();

        enviarAmbos("NUEVA_RONDA");

        repartirCartasAJugadores();
        enviarEstadoActual();

        System.out.println("[SERVIDOR] Nueva ronda iniciada");
    }

    private void procesarTruco(DatagramPacket dp) {
        System.out.println("[SERVIDOR] TRUCO recibido");

        int idx = getIndiceCliente(dp.getAddress(), dp.getPort());
        if (idx == -1) {
            System.out.println("[SERVIDOR] Cliente desconocido intentó cantar truco");
            return;
        }

        TipoJugador jugadorQueCanto = (idx == 0) ? TipoJugador.JUGADOR_1 : TipoJugador.JUGADOR_2;

        boolean trucoValido = partidaLogica.cantarTruco(jugadorQueCanto);

        if (trucoValido) {
            System.out.println("[SERVIDOR] ✅ TRUCO ACEPTADO");

            // Notificar al rival
            int rival = (idx == 0) ? 1 : 0;
            enviarMensaje(
                    "TRUCO_RIVAL",
                    clientes[rival].getIp(),
                    clientes[rival].getPuerto()
            );

            // Enviar estado actualizado a AMBOS (incluye info del truco)
            enviarEstadoActual();
        } else {
            enviarEstadoActual();
        }
    }

    private void enviarEstadoActual() {
        if (partidaLogica == null) return;

        EstadoTurno estado = partidaLogica.getEstadoActual();
        String jugadorManoStr = partidaLogica.getJugadorMano().name();

        // ✅ NUEVO: Incluir estado del truco en el mensaje
        String mensaje = "ESTADO:" +
                partidaLogica.getManoActual() + ":" +
                partidaLogica.getPuntosJ1() + ":" +
                partidaLogica.getPuntosJ2() + ":" +
                estado.name() + ":" +
                jugadorManoStr + ":" +
                (partidaLogica.isTrucoUsado() ? "1" : "0") + ":" +
                partidaLogica.getManoTrucoUsada();

        enviarAmbos(mensaje);

        System.out.println("[SERVIDOR] Estado enviado - Truco usado: " + partidaLogica.isTrucoUsado());
    }

    private int getIndiceCliente(InetAddress ip, int puerto) {
        for (int i = 0; i < cantClientes; i++) {
            if (clientes[i].getIp().equals(ip) && clientes[i].getPuerto() == puerto) {
                return i;
            }
        }
        return -1;
    }
    public void detener() {
        fin = true;
        if (conexion != null && !conexion.isClosed()) {
            conexion.close();
        }
    }
}
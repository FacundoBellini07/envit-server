package juego.red;
import juego.elementos.Carta;
import juego.elementos.EstadoTurno;
import juego.elementos.Palo;
import juego.elementos.ZonaJuego;
import juego.pantallas.Partida;
import juego.personajes.Jugador;
import juego.personajes.TipoJugador;

import java.io.IOException;
import java.net.*;

public class HiloServidor extends Thread {
    private DatagramSocket conexion;
    private boolean fin = false;
    private Cliente[] clientes = new Cliente[2];
    private int cantClientes = 0;

    private Partida partidaLogica;
    private Jugador srvJugador1;
    private Jugador srvJugador2;

    public HiloServidor(Partida partida) {
        this.partidaLogica = partida;
        try {
            conexion = new DatagramSocket(30243);
            System.out.println("[SERVIDOR] Escuchando en puerto 30243");
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

        if (mensaje.equals("Conexion")) {
            if (cantClientes < 2) {
                clientes[cantClientes++] = new Cliente(dp.getAddress(), dp.getPort());
                int idx = cantClientes - 1;
                enviarMensaje("OK", clientes[idx].getIp(), clientes[idx].getPuerto());
                enviarMensaje("ID:"+ idx, clientes[idx].getIp(), clientes[idx].getPuerto());
                System.out.println("[SERVIDOR] Cliente conectado. Total: " + cantClientes);

                if (cantClientes == 2) {

                    // 1. Obtener quién empieza según la partida del servidor
                    TipoJugador quienEmpieza = partidaLogica.getJugadorMano();

                    // 2. Convertirlo a número (0 = J1, 1 = J2)
                    int idEmpieza = (quienEmpieza == TipoJugador.JUGADOR_1) ? 0 : 1;

                    // 3. Enviar el mensaje con el dato: "EMPIEZA:0" o "EMPIEZA:1"
                    enviarAmbos("EMPIEZA:" + idEmpieza);

                    enviarEstadoActual();
                }
            }
        }
        else if (mensaje.startsWith("CARTA_JUGADA:")) {

            procesarCartaJugada(dp, mensaje);
        }
        else if (mensaje.equals("TRUCO")) {
            // NUEVO: Procesar truco
            procesarTruco(dp);
        }
    }

    private void procesarCartaJugada(DatagramPacket dp, String mensaje) {
        String[] partes = mensaje.split(":");
        if (partes.length >= 3) {
            int valor = Integer.parseInt(partes[1]); // Parsear a int
            Palo palo = Palo.valueOf(partes[2]);     // Parsear a Enum

            System.out.println("[SERVIDOR] Carta jugada: " + valor + " de " + palo);

            int idx = getIndiceCliente(dp.getAddress(), dp.getPort());
            if (idx == -1) return;

            TipoJugador jugadorQueJugo = (idx == 0) ? TipoJugador.JUGADOR_1 : TipoJugador.JUGADOR_2;

            // Si partidaLogica es null, evitamos error, aunque no debería serlo
            if (partidaLogica != null) {
                partidaLogica.procesarJugadaServidor(jugadorQueJugo, valor, palo);
            }

            Carta cartaJugada = new Carta(valor, palo);
            partidaLogica.jugarCarta(jugadorQueJugo, cartaJugada);

            int rival = (idx == 0) ? 1 : 0;

            enviarMensaje(
                    "CARTA_RIVAL:" + valor + ":" + palo.name(),
                    clientes[rival].getIp(),
                    clientes[rival].getPuerto()
            );


            enviarEstadoActual();
        }
    }

    private void procesarTruco(DatagramPacket dp) {
        System.out.println("[SERVIDOR] TRUCO recibido");

        int idx = getIndiceCliente(dp.getAddress(), dp.getPort());
        if (idx == -1) {
            System.out.println("[SERVIDOR] Cliente desconocido intentó cantar truco");
            return;
        }

        TipoJugador jugadorQueCanto = (idx == 0) ? TipoJugador.JUGADOR_1 : TipoJugador.JUGADOR_2;
        int rival = (idx == 0) ? 1 : 0;

        enviarMensaje(
                "TRUCO_RIVAL",
                clientes[rival].getIp(),
                clientes[rival].getPuerto()
        );


    }

    private void enviarEstadoActual() {
        if (partidaLogica == null) return; // O partidaLogica, como la hayas llamado

        // 1. Obtener el estado de forma segura
        EstadoTurno estado = partidaLogica.getEstadoActual();
        String nombreEstado = estado.name();
        String jugadorManoStr = partidaLogica.getJugadorMano().name();
        // 2. Construir el mensaje usando nombreEstado
        String mensaje = "ESTADO:" +
                partidaLogica.getManoActual() + ":" +
                partidaLogica.getPuntosJ1() + ":" +
                partidaLogica.getPuntosJ2() + ":" +
                nombreEstado + ":"
                + jugadorManoStr;

        enviarAmbos(mensaje);
    }
    private int getIndiceCliente(InetAddress ip, int puerto) {
        for (int i = 0; i < cantClientes; i++) {
            if (clientes[i].getIp().equals(ip) && clientes[i].getPuerto() == puerto) {
                return i;
            }
        }
        return -1;
    }

}
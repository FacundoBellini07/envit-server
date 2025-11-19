package juego.red;
import juego.utilidades.Global;
import java.io.IOException;
import java.net.*;

public class HiloServidor extends Thread {
    private DatagramSocket conexion;
    private boolean fin = false;
    private Cliente[] clientes = new Cliente[2];
    private int cantClientes = 0;

    // NUEVO: Guardar estado de la partida en el servidor
    private int[] puntosJugadores = {0, 0};
    private int manoActual = 0;
    private int turnoActual = 0; // 0 = jugador 1, 1 = jugador 2

    public HiloServidor() {
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
            System.out.println("[SERVIDOR] Enviado a " + ip + ": " + mensaje);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // NUEVO: Enviar a ambos clientes
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
                System.out.println("[SERVIDOR] Cliente conectado. Total: " + cantClientes);

                if (cantClientes == 2) {
                    Global.empieza = true;
                    enviarAmbos("Empieza");
                }
            }
        }
        else if (mensaje.startsWith("CARTA_JUGADA:")) {
            // NUEVO: Procesar carta jugada
            procesarCartaJugada(dp, mensaje);
        }
        else if (mensaje.equals("TRUCO")) {
            // NUEVO: Procesar truco
            procesarTruco(dp);
        }
    }

    private void procesarCartaJugada(DatagramPacket dp, String mensaje) {
        // Ej: "CARTA_JUGADA:1:ESPADAS"
        String[] partes = mensaje.split(":");
        if (partes.length >= 3) {
            String valor = partes[1];
            String palo = partes[2];
            System.out.println("[SERVIDOR] Carta jugada: " + valor + " de " + palo);



            // Enviar estado actualizado a ambos clientes
            enviarEstadoActual();
        }
    }

    private void procesarTruco(DatagramPacket dp) {
        System.out.println("[SERVIDOR] TRUCO cantado");


        enviarEstadoActual();
    }

    private void enviarEstadoActual() {
        String estado = String.format(
                "ESTADO:mano=%d:puntosJ1=%d:puntosJ2=%d:turno=%d",
                manoActual, puntosJugadores[0], puntosJugadores[1], turnoActual
        );
        enviarAmbos(estado);
    }
}
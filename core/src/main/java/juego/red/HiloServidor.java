package juego.red;
import juego.utilidades.Global;

import java.io.IOException;
import java.net.*;

public class HiloServidor extends Thread {

    private DatagramSocket conexion;
    private boolean fin = false;
    private Cliente[] clientes = new Cliente[2];
    private int cantClientes = 0;

    public HiloServidor() {

        try {
            conexion = new DatagramSocket(30243);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void enviarMensaje(String mensaje, InetAddress ip, int puerto) {
        byte[] data = mensaje.getBytes();
        DatagramPacket dp = new DatagramPacket(data, data.length, ip, puerto);
        try {
            conexion.send(dp);
        } catch (IOException e) {
            e.printStackTrace();
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
        if (mensaje.equals("Conexion")) {
            if (cantClientes < 2) {
                clientes[cantClientes++] = new Cliente(dp.getAddress(), dp.getPort());
                System.out.println("mensaje recibido de: " + dp.getAddress() + ":" + dp.getPort());
                int idx = cantClientes - 1;
                enviarMensaje("OK", clientes[idx].getIp(), clientes[idx].getPuerto());
                if (cantClientes == 2) {
                    Global.empieza = true;
                    for (int i = 0; i < clientes.length; i++) {
                        enviarMensaje("Empieza", clientes[i].getIp(), clientes[i].getPuerto());
                    }
                }
            }
        }
    }
}
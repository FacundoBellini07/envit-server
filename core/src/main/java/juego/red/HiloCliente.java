package juego.red;
import juego.utilidades.Global;

import java.io.IOException;
import java.net.*;

public class HiloCliente extends Thread {

    private DatagramSocket conexion;
    private InetAddress ipserver;
    private int puerto = 30243;
    private boolean fin = false;
public HiloCliente() {

    try {
        ipserver = InetAddress.getByName("255.255.255.255");
        conexion = new DatagramSocket();
    } catch (SocketException | UnknownHostException e) {
        e.printStackTrace();
    }
    enviarMensaje("Conexion");
}
public void enviarMensaje(String mensaje) {
    byte[] data = mensaje.getBytes();
    DatagramPacket dp = new DatagramPacket(data, data.length, ipserver, puerto);
    try {
        conexion.send(dp);
    } catch (IOException e) {
        e.printStackTrace();
    }
}
    @Override
    public void run() {
        do{
            byte[] data = new byte[1024];
            DatagramPacket dp = new DatagramPacket(data, data.length);
            try {
                conexion.receive(dp);
                procesarMensaje(dp);
            } catch (IOException e){
                e.printStackTrace();
            }
        }while(!fin);
    }
    private void procesarMensaje(DatagramPacket dp) {
        String mensaje = (new String(dp.getData())).trim();
        if(mensaje.equals("OK")){
            System.out.println("Conectado al servidor");
            ipserver = dp.getAddress();
        }
        else if(mensaje.equals("Empieza")){
            Global.empieza = true;
        }
    }
}

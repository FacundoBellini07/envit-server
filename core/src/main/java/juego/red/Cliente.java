package juego.red;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Cliente {
    private InetAddress ip;
    private int puerto;
    // ✅ NUEVO: Timestamp del último mensaje recibido
    private long ultimoMensaje;

    public Cliente(InetAddress ip, int puerto) {
        this.ip = ip;
        this.puerto = puerto;
        this.ultimoMensaje = System.currentTimeMillis(); // Inicializar con hora actual
    }

    public InetAddress getIp() { return ip; }
    public int getPuerto() { return puerto; }

    // ✅ NUEVO: Métodos para gestionar el timeout
    public void actualizarUltimoMensaje() {
        this.ultimoMensaje = System.currentTimeMillis();
    }

    public long getUltimoMensaje() {
        return ultimoMensaje;
    }
}

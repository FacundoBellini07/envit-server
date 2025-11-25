package juego.red;

import juego.elementos.Carta;
import juego.elementos.EstadoTruco;
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

    private boolean partidaEnProgreso = false;

    // ✅ NUEVO: Timer para el checker de desconexiones
    private Timer timerDesconexiones;

    public HiloServidor(Partida partida) {
        this.partidaLogica = partida;
        try {
            conexion = new DatagramSocket(30243);
            System.out.println("[SERVIDOR] Escuchando en puerto 30243");

            // Detector de desconexiones forzadas
            iniciarCheckerDesconexiones();

        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private void iniciarCheckerDesconexiones() {
        if (timerDesconexiones != null) {
            timerDesconexiones.cancel();
        }

        timerDesconexiones = new Timer();
        timerDesconexiones.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                chequearDesconexiones();
            }
        }, 2000, 1000);
    }

    // ✅ NUEVO: Método para detener el checker
    private void detenerCheckerDesconexiones() {
        if (timerDesconexiones != null) {
            timerDesconexiones.cancel();
            timerDesconexiones = null;
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

    private void vaciarSala() {
        System.out.println("[SERVIDOR] Vaciando sala...");

        // 1. Primero detenemos el juego para evitar que el timer actual mate a alguien
        partidaEnProgreso = false;
        esperandoNuevaRonda = false;

        // 2. Reseteamos datos
        cantClientes = 0;
        clientes = new Cliente[2]; // Borra referencias a clientes viejos
        partidaLogica.resetearTotal();

        // 3. Reiniciar el checker de desconexiones DESDE CERO
        detenerCheckerDesconexiones();
        iniciarCheckerDesconexiones();

        System.out.println("[SERVIDOR] ✅ Sala vaciada. Esperando nuevos jugadores...");
    }

    private void chequearDesconexiones() {
        if (cantClientes == 0) return;
        long tiempoActual = System.currentTimeMillis();
        final long TIEMPO_LIMITE = 3000;

        for (int i = 0; i < cantClientes; i++) {
            Cliente cliente = clientes[i];
            if (cliente != null) {
                if (tiempoActual - cliente.getUltimoMensaje() > TIEMPO_LIMITE) {
                    System.out.println("[SERVIDOR] 🚨 Cliente " + i + " ha excedido el tiempo de espera (TIMEOUT).");

                    // ✅ Si está EN PROGRESO: desconectar a AMBOS y vaciar
                    if (partidaEnProgreso) {
                        System.out.println("[SERVIDOR] ⚠️ Partida en progreso. Enviando RIVAL_SE_FUE a ambos y vaciando sala.");
                        enviarAmbos("RIVAL_SE_FUE");
                        vaciarSala();
                        return;
                    }
                    // ✅ Si NO está en progreso: solo vaciar sin enviar mensaje
                    else {
                        System.out.println("[SERVIDOR] Partida no en progreso. Solo vaciando sala.");
                        vaciarSala();
                        return;
                    }
                }
            }
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
        }
        else if (mensaje.equals("TRUCO")) {
            procesarTruco(dp);
        }
        else if (mensaje.startsWith("QUIERO")) {
            procesarRespuestaTruco(dp, "QUIERO");
        }
        else if (mensaje.startsWith("RETRUCO")) {
            procesarRespuestaTruco(dp, "RETRUCO"); // O VALE_CUATRO, si lo incluyes aquí
        }
    }

    private void procesarConexion(DatagramPacket dp) {
        // Si hay espacio, aceptamos
        if (cantClientes < 2) {
            // ✅ IMPORTANTE: Verificar si el cliente ya existe (por IP/Puerto) para no duplicarlo
            // aunque con vaciarSala() esto es raro, es buena práctica.
            int idxExistente = getIndiceCliente(dp.getAddress(), dp.getPort());

            if (idxExistente != -1) {
                // Si ya existe (spam de conexión), solo reenviamos el OK pero no agregamos uno nuevo
                System.out.println("[SERVIDOR] Cliente ya conectado, reenviando OK.");
                enviarMensaje("OK", clientes[idxExistente].getIp(), clientes[idxExistente].getPuerto());
                enviarMensaje("ID:" + idxExistente, clientes[idxExistente].getIp(), clientes[idxExistente].getPuerto());
                return;
            }

            clientes[cantClientes] = new Cliente(dp.getAddress(), dp.getPort());
            // Asegurar que el timestamp inicial es AHORA MISMO
            clientes[cantClientes].actualizarUltimoMensaje();

            int idx = cantClientes;
            cantClientes++;

            enviarMensaje("OK", clientes[idx].getIp(), clientes[idx].getPuerto());
            enviarMensaje("ID:" + idx, clientes[idx].getIp(), clientes[idx].getPuerto());

            System.out.println("[SERVIDOR] Jugador " + idx + " conectado. Total: " + cantClientes + "/2");

            if (cantClientes == 2 && !partidaEnProgreso) {
                // Pequeña pausa antes de iniciar para asegurar que ambos clientes procesaron el "OK"
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (cantClientes == 2) iniciarPartida();
                    }
                }, 500);
            }
        } else {
            System.out.println("[SERVIDOR] Rechazando conexión (FULL)");
            enviarMensaje("FULL", dp.getAddress(), dp.getPort());
        }
    }

    private void iniciarPartida() {
        System.out.println("[SERVIDOR] Iniciando partida con 2 jugadores");

        partidaEnProgreso = true;

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
            // ✅ VERIFICAR BLOQUEO AL INICIO
            if (esperandoNuevaRonda) {
                System.out.println("[SERVIDOR] ⏸️ Carta bloqueada: esperando nueva ronda");
                return;
            }

            if (partidaLogica.isTrucoPendiente()) {
                System.out.println("[SERVIDOR] ⏸️ Carta bloqueada: hay truco pendiente de respuesta");
                return;
            }

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

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            vaciarSala();
                        }
                    }, 3000);

                    partidaEnProgreso = false;
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
    private void procesarRespuestaTruco(DatagramPacket dp, String tipoRespuesta) {
        System.out.println("[SERVIDOR] ===== PROCESANDO RESPUESTA: " + tipoRespuesta + " =====");

        int idx = getIndiceCliente(dp.getAddress(), dp.getPort());
        if (idx == -1) {
            System.out.println("[SERVIDOR] ❌ Cliente desconocido");
            return;
        }

        TipoJugador jugadorRespuesta = (idx == 0) ? TipoJugador.JUGADOR_1 : TipoJugador.JUGADOR_2;

        System.out.println("[SERVIDOR] Respuesta de: " + jugadorRespuesta + " (Cliente " + idx + ")");

        // ✅ VALIDAR que haya un truco pendiente
        if (!partidaLogica.isTrucoPendiente()) {
            System.out.println("[SERVIDOR] ⚠️ No hay truco pendiente, ignorando respuesta");
            return;
        }

        if (tipoRespuesta.equals("QUIERO")) {
            System.out.println("[SERVIDOR] Jugador aceptó el truco (QUIERO)");

            // ✅ Desbloquear el juego
            partidaLogica.aceptarTruco();

            // ✅ Notificar a AMBOS que el truco fue aceptado
            enviarAmbos("RESPUESTA_TRUCO:QUIERO");

            // ✅ AHORA SÍ enviar el estado actualizado
            enviarEstadoActual();

            System.out.println("[SERVIDOR] Juego desbloqueado, ambos pueden jugar cartas");
        }
        else if (tipoRespuesta.equals("RETRUCO") || tipoRespuesta.equals("VALE_CUATRO")) {
            // ✅ El jugador subió la apuesta
            EstadoTruco estadoAnterior = partidaLogica.getEstadoTruco();
            EstadoTruco nuevoEstado = estadoAnterior.siguiente();

            System.out.println("[SERVIDOR] Jugador subió de " + estadoAnterior + " a " + nuevoEstado);

            // ✅ Actualizar pero MANTENER bloqueado
            partidaLogica.subirTruco(nuevoEstado, jugadorRespuesta);

            // ✅ Notificar a AMBOS la subida
            enviarAmbos("RESPUESTA_TRUCO:SUBIDA:" + nuevoEstado.name());

            // ✅ Enviar estado actualizado (aún bloqueado, esperando nueva respuesta)
            enviarEstadoActual();

            System.out.println("[SERVIDOR] Esperando respuesta al " + nuevoEstado);
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
        System.out.println("[SERVIDOR] ===== PROCESANDO TRUCO =====");

        // 1. Identificar quién cantó
        int idx = getIndiceCliente(dp.getAddress(), dp.getPort());
        if (idx == -1) {
            System.out.println("[SERVIDOR] ❌ Cliente desconocido intentó cantar truco");
            return;
        }

        TipoJugador jugadorQueCanto = (idx == 0) ? TipoJugador.JUGADOR_1 : TipoJugador.JUGADOR_2;

        System.out.println("[SERVIDOR] Jugador que cantó: " + jugadorQueCanto + " (Cliente " + idx + ")");

        // 2. Validar en la lógica (esto actualiza el estado interno)
        boolean trucoValido = partidaLogica.cantarTruco(jugadorQueCanto);

        if (trucoValido) {
            System.out.println("[SERVIDOR] ✅ Truco válido aceptado");

            // ✅ NO ENVIAR ESTADO AQUÍ - Solo notificar al rival
            int rival = (idx == 0) ? 1 : 0;

            System.out.println("[SERVIDOR] Enviando TRUCO_RIVAL al cliente " + rival);
            enviarMensaje(
                    "TRUCO_RIVAL",
                    clientes[rival].getIp(),
                    clientes[rival].getPuerto()
            );

            // ✅ NO llamar a enviarEstadoActual() aquí
            // El estado se enviará cuando el rival responda

        } else {
            System.out.println("[SERVIDOR] ❌ Truco rechazado por validación");
            // Solo enviar estado si fue rechazado
            enviarEstadoActual();
        }
    }

    private void enviarEstadoActual() {
        if (partidaLogica == null) return;

        EstadoTurno estado = partidaLogica.getEstadoActual();
        String jugadorManoStr = partidaLogica.getJugadorMano().name();

        // ✅ NUEVO: Enviar estado del truco completo
        String estadoTrucoStr = partidaLogica.getEstadoTruco().name();
        String ultimoCantoStr = (partidaLogica.getUltimoQueCanto() != null)
                ? partidaLogica.getUltimoQueCanto().name()
                : "null";

        String mensaje = "ESTADO:" +
                partidaLogica.getManoActual() + ":" +
                partidaLogica.getPuntosJ1() + ":" +
                partidaLogica.getPuntosJ2() + ":" +
                estado.name() + ":" +
                jugadorManoStr + ":" +
                estadoTrucoStr + ":" +
                partidaLogica.getManoTrucoUsada() + ":" +
                ultimoCantoStr;

        enviarAmbos(mensaje);

        System.out.println("[SERVIDOR] Estado enviado - Truco: " + estadoTrucoStr);
    }

    private int getIndiceCliente(InetAddress ip, int puerto) {
        for (int i = 0; i < cantClientes; i++) {
            if (clientes[i] != null && clientes[i].getIp().equals(ip) && clientes[i].getPuerto() == puerto) {
                return i;
            }
        }
        return -1;
    }

    public void detener() {
        fin = true;
        detenerCheckerDesconexiones();
        if (conexion != null && !conexion.isClosed()) {
            conexion.close();
        }
    }
}
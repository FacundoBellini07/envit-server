package juego.pantallas;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import juego.elementos.ZonaJuego;
import juego.personajes.Jugador;
import juego.personajes.TipoJugador;
import juego.red.HiloServidor;

public class PantallaServidor implements Screen {
    Game game;
    private SpriteBatch batch;
    private BitmapFont font;
    private HiloServidor hiloServidor;
    private Partida partidaLogica; // La partida del servidor

    public PantallaServidor(Game game) {
        this.game = game;
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2);

        // 1. CREAMOS LA PARTIDA AQUÍ (HILO PRINCIPAL)
        // Como estamos en el hilo principal, Carta carga su textura SIN ERROR
        partidaLogica = new Partida();

        // Necesitamos inicializarla con zonas dummy y jugadores para que no de error null
        Jugador srvJ1 = new Jugador("ServerJ1");
        Jugador srvJ2 = new Jugador("ServerJ2");
        ZonaJuego zonaDummy1 = new ZonaJuego(0,0,0,0); // Zonas vacías
        ZonaJuego zonaDummy2 = new ZonaJuego(0,0,0,0);

        // Inicializamos la lógica
        partidaLogica.repartirCartas(srvJ1, srvJ2);
        partidaLogica.inicializar(zonaDummy1, zonaDummy2, srvJ1, srvJ2, 0);

        // 2. CREAMOS EL HILO Y LE PASAMOS LA PARTIDA LISTA
        hiloServidor = new HiloServidor(partidaLogica);
        hiloServidor.start();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        font.draw(batch, "SERVIDOR ACTIVO (CON VENTANA)", 50, 400);

        // Opcional: Puedes mostrar datos de la partida en pantalla para debug
        if (partidaLogica != null) {
            font.draw(batch, "Mano: " + partidaLogica.getManoActual(), 50, 300);
            font.draw(batch, "Turno: " + partidaLogica.getEstadoActual(), 50, 250);
        }

        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        if(hiloServidor != null) {
            hiloServidor.detener();
        }
    }

    // Métodos vacíos requeridos por Screen
    @Override public void show() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
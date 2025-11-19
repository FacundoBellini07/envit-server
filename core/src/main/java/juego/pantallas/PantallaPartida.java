package juego.pantallas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import juego.elementos.*;
import juego.personajes.Jugador;
import juego.personajes.RivalBot;
import juego.red.HiloCliente;
import juego.utilidades.Global;

import java.util.ArrayList;

public class PantallaPartida implements Screen {

    private Game game;
    private Mazo mazo;
    private Partida partida;
    private ArrayList<Jugador> jugadores = new ArrayList<Jugador>();
    private SpriteBatch batch = new SpriteBatch();
    private Texture fondoPartida;
    private Texture mazoSprite;
    private Texture dorsoCartaSprite;
    private boolean inicioRonda = true;
    private Animacion animacion;

    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private CartaRenderer cartaRenderer;
    private ManoManager manoManager;

    private RivalBot rivalBot;
    private ManoRivalRenderer manoRivalRenderer;
    private ZonaJuego zonaJuegoRival;
    private ZonaJuego zonaJuegoJugador;

    private Hud hud;
    private BitmapFont font;

    private final float WORLD_WIDTH = 640;
    private final float WORLD_HEIGHT = 480;

    private final float CARTA_PROPORCION_ANCHO = 0.1f;
    private final float CARTA_RELACION_ASPECTO = 1.4f;

    private final float CARTA_ANCHO = WORLD_WIDTH * CARTA_PROPORCION_ANCHO;
    private final float CARTA_ALTO = CARTA_ANCHO * CARTA_RELACION_ASPECTO;
    private int mano;

    private PantallaFinal pantallaFinal;

    private BotonTruco botonTruco;

    private boolean debeVolverAlMenu = false;
    private HiloCliente hc;

    
    public PantallaPartida(Game game) {
        this.game = game;
        this.crearJugadores();
    }

    private void crearJugadores() {
        Jugador jugador = new Jugador("Tú");
        Jugador rival = new Jugador("Rival");

        jugadores.add(jugador);
        jugadores.add(rival);
    }

    @Override
    public void show() {
        fondoPartida = new Texture(Gdx.files.internal("fondos/fondoPartida.png"));
        mazoSprite = new Texture(Gdx.files.internal("sprites/mazo_sprite.png"));
        dorsoCartaSprite = new Texture(Gdx.files.internal("sprites/dorso.png"));
        mazo = new Mazo();
        partida = new Partida();
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply();
        shapeRenderer = new ShapeRenderer();
        cartaRenderer = new CartaRenderer(batch, shapeRenderer, viewport);

        font = new BitmapFont();
        font.getData().setScale(1.2f);
        hud = new Hud(font, jugadores.get(0), jugadores.get(1), WORLD_WIDTH, WORLD_HEIGHT);

        // Crear las dos zonas de juego
        float zonaAncho = CARTA_ANCHO * 1.5f;
        float zonaAlto = CARTA_ALTO * 1.3f;

        float zonaJugadorX = (WORLD_WIDTH - zonaAncho) / 2f;
        float zonaJugadorY = (WORLD_HEIGHT / 2f) - zonaAlto;
        zonaJuegoJugador = new ZonaJuego(zonaJugadorX, zonaJugadorY, zonaAncho, zonaAlto);
        zonaJuegoJugador.setCartaRenderer(cartaRenderer);

        float zonaRivalX = (WORLD_WIDTH - zonaAncho) / 2f;
        float zonaRivalY = (WORLD_HEIGHT / 2f) + 20;
        zonaJuegoRival = new ZonaJuego(zonaRivalX, zonaRivalY, zonaAncho, zonaAlto);
        zonaJuegoRival.setCartaRenderer(cartaRenderer);

        manoManager = new ManoManager(
                jugadores.get(0),
                cartaRenderer,
                viewport,
                WORLD_WIDTH,
                WORLD_HEIGHT,
                CARTA_ANCHO,
                CARTA_ALTO
        );
        manoManager.setZonaJuego(zonaJuegoJugador);

        rivalBot = new RivalBot(jugadores.get(1), zonaJuegoRival);
        rivalBot.setDelay(1.5f);
        rivalBot.setProbabilidadTruco(0.3f);

        manoRivalRenderer = new ManoRivalRenderer(
                jugadores.get(1),
                cartaRenderer,
                dorsoCartaSprite,
                zonaJuegoRival,
                WORLD_WIDTH,
                WORLD_HEIGHT,
                CARTA_ANCHO,
                CARTA_ALTO
        );

        animacion = new Animacion(
                WORLD_WIDTH,
                WORLD_HEIGHT,
                CARTA_ANCHO,
                CARTA_ALTO,
                dorsoCartaSprite,
                manoManager
        );

        partida.inicializar(zonaJuegoJugador, zonaJuegoRival, rivalBot,
                jugadores.get(0), jugadores.get(1), mano);

        rivalBot.setPartida(partida);

        Gdx.input.setInputProcessor(manoManager.getInputMultiplexer());
        
        float btnTrucoAncho = 80f;
        float btnTrucoAlto = 60f;
        float margenIzq = 20f;
        float btnTrucoY = (WORLD_HEIGHT - btnTrucoAlto) / 2f;

        botonTruco = new BotonTruco(
                margenIzq,
                btnTrucoY,
                btnTrucoAncho,
                btnTrucoAlto,
                font,
                viewport,
                partida
        );

        pantallaFinal = new PantallaFinal(
                font,
                viewport,
                hud,
                WORLD_WIDTH,
                WORLD_HEIGHT
        );
        hc = new HiloCliente();
        hc.start();
        
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void render(float delta) {

        if(!Global.empieza){
            Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            hud.dibujarMensajeCentral(batch, "ESPERANDO RIVAL...", Color.WHITE);
        }
        else {
            if (debeVolverAlMenu) {
                game.setScreen(new PantallaMenu((juego.Principal) game));
                dispose();
                return;
            }

            update(delta);
            Gdx.gl.glClearColor(0, 0.1f, 0, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


            if (pantallaFinal.isActiva()) {
                pantallaFinal.render(batch, shapeRenderer);
                return;
            }

            // 1. DIBUJAR FONDO Y MAZO
            this.batch.setProjectionMatrix(viewport.getCamera().combined);
            this.batch.begin();
            batch.draw(fondoPartida, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);

            float mazoAncho = CARTA_ANCHO;
            float mazoAlto = CARTA_ALTO;
            float margenHorizontal = WORLD_WIDTH * 0.05f;
            float mazoX = WORLD_WIDTH - mazoAncho - margenHorizontal;
            float mazoY = (WORLD_HEIGHT - mazoAlto) / 2f;

            animacion.render(batch);
            batch.draw(mazoSprite, mazoX, mazoY, mazoAncho, mazoAlto);

            this.batch.end();

            // 2. DIBUJAR LOS FONDOS DE LAS ZONAS DE JUEGO
            zonaJuegoJugador.renderFondo(shapeRenderer);
            zonaJuegoRival.renderFondo(shapeRenderer);

            // 3. DIBUJAR LAS CARTAS EN MANO
            this.batch.setProjectionMatrix(viewport.getCamera().combined);

            this.batch.begin();
            manoRivalRenderer.render(batch);
            this.batch.end();

            manoManager.render();

            // 4. DIBUJAR LAS CARTAS DENTRO DE LAS ZONAS (jugadas)
            zonaJuegoJugador.renderCartas();
            zonaJuegoRival.renderCartas();

            // 5. DIBUJAR BOTÓN DE TRUCO
            botonTruco.render(batch, shapeRenderer);

            // 6. DIBUJAR HUD
            this.batch.setProjectionMatrix(viewport.getCamera().combined);
            hud.render(batch, partida.getManoActual(), partida.esTurnoJugador(),
                    partida.isTrucoActivoEnManoActual(), partida.getManoTrucoUsada());
        }
    }



    public void update(float delta) {
        animacion.update(delta);
        partida.update(delta);

        manoManager.setEsMiTurno(partida.esTurnoJugador());

        // ✅ REFACTORIZADO: Actualizar botón de truco y pantalla final
        if (!pantallaFinal.isActiva() && !partida.partidaTerminada()) {
            botonTruco.update(delta);
            botonTruco.detectarClick();
        }

        // ✅ REFACTORIZADO: Actualizar pantalla final si está activa
        if (pantallaFinal.isActiva()) {
            boolean solicitudVolver = pantallaFinal.update(delta);
            if (solicitudVolver) {
                // ✅ FIX: Solo setear el flag, no cambiar de pantalla aquí
                debeVolverAlMenu = true;
            }
            return;
        }

        // Verificar si la partida terminó
        if (partida.partidaTerminada() && !pantallaFinal.isActiva()) {
            // ✅ REFACTORIZADO: Activar la pantalla final
            pantallaFinal.activar(
                    partida.getGanador(),
                    jugadores.get(0),
                    jugadores.get(1)
            );

            // Desactivar input del jugador
            Gdx.input.setInputProcessor(null);

            System.out.println("¡PARTIDA TERMINADA! Ganador: " + partida.getGanador().getNombre());
            return;
        }

        // Si la partida está lista para nueva ronda
        if (partida.rondaTerminada()) {
            inicioRonda = true;
            partida.nuevaRonda();
        }

        if (inicioRonda) {
            zonaJuegoJugador.limpiar();
            zonaJuegoRival.limpiar();
            jugadores.get(0).limpiarMazo();
            jugadores.get(1).limpiarMazo();

            partida.repartirCartas(jugadores.get(0), jugadores.get(1));

            manoManager.inicializarMano();
            manoRivalRenderer.inicializarPosiciones();
            animacion.iniciarAnimacionReparto();
            if (!partida.esTurnoJugador() && rivalBot != null) {
                rivalBot.activarTurno();
            }
            inicioRonda = false;
        }
    }


    private void volverAlMenu() {
        debeVolverAlMenu = true;
    }

    @Override
    public void resize(int width, int height) {
        System.out.println("Se ha redimensionado la pantalla a: " + width + "x" + height);
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        if (fondoPartida != null) fondoPartida.dispose();
        if (mazoSprite != null) mazoSprite.dispose();
        if (batch != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (font != null) font.dispose();
    }
}
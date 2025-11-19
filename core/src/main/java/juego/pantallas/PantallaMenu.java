package juego.pantallas;

import juego.Principal;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;


import java.util.Random;

public class PantallaMenu implements Screen {

    private final Principal game;

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    private Texture backgroundTexture;
    private Texture optionsBackgroundTexture;

    private boolean inOptionsMode = false;


    private boolean crtEnabled = true;
    private boolean flickerEnabled = true;
    private boolean shakeEnabled = true;

    private float scanlineOffset = 0f;
    private float crtFlicker = 0f;

    private Random random = new Random();

    private static final float VIRTUAL_WIDTH = 1280;
    private static final float VIRTUAL_HEIGHT = 720;
    private Viewport viewport;


    private Rectangle btnPlayRect = new Rectangle();
    private Rectangle btnOptionsRect = new Rectangle();
    private Rectangle btnExitRect = new Rectangle();
    private Rectangle btnCloseOptionsRect = new Rectangle();
    private Rectangle chkCRTBox = new Rectangle();
    private Rectangle chkFlickerBox = new Rectangle();
    private Rectangle chkShakeBox = new Rectangle();


    private Texture btnPlayTexture, btnOptionsTexture, btnExitTexture, btnCloseOptionsTexture;
    private Texture chkCheckedTexture, chkUncheckedTexture;
    private Texture titleTexture;


    private Music[] canciones;
    private String[] rutasCanciones = {
        "sounds/fuego.mp3"

    };

    public PantallaMenu(final Principal game) {
        this.game = game;

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();


        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);


        loadFont();
        loadBackgrounds();
        loadButtonTextures();
        setButtonRects();
        cargarMusicaFondo();
    }

    private void loadFont() {
        try {
             FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fuentes/medieval.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = 36;
            font = generator.generateFont(parameter);
            generator.dispose();
        } catch (Exception e) {
            font = new BitmapFont(); // fallback default
            Gdx.app.log("PantallaMenu", "No se pudo cargar la fuente medieval.ttf, usando fuente por defecto");
        }
    }

    private void loadBackgrounds() {
         if (Gdx.files.internal("fondos/fondo.png").exists()) {
            backgroundTexture = new Texture(Gdx.files.internal("fondos/fondo.png"));
            Gdx.app.log("PantallaMenu", "Fondo principal cargado correctamente");
        } else {
            backgroundTexture = null;
            Gdx.app.error("PantallaMenu", "No se encontró fondos/fondo.png, usando fondo procedural");
        }
        if (Gdx.files.internal("fondos/fondoOpciones.png").exists()) {
            optionsBackgroundTexture = new Texture(Gdx.files.internal("fondos/fondoOpciones.png"));
            Gdx.app.log("PantallaMenu", "Fondo de opciones cargado correctamente");
        } else {
            optionsBackgroundTexture = null;
            Gdx.app.error("PantallaMenu", "No se encontró fondos/fondoOpciones.png, usando fondo procedural para opciones");
        }
    }

    private void loadButtonTextures() {
     /*
        btnPlayTexture = tryLoadTexture("sprites/btn_jugar.png");
        btnOptionsTexture = tryLoadTexture("sprites/btn_jugar.png");
        btnExitTexture = tryLoadTexture("sprites/btn_jugar.png");
        btnCloseOptionsTexture = tryLoadTexture("sprites/btn_jugar.png");
        chkCheckedTexture = tryLoadTexture("sprites/btn_jugar.png");
        chkUncheckedTexture = tryLoadTexture("sprites/pepi.png");
        titleTexture = tryLoadTexture("sprites/btn_jugar.png");
    */}

    private Texture tryLoadTexture(String path) {
        if (Gdx.files.internal(path).exists()) {
            return new Texture(Gdx.files.internal(path));
        } else {
            Gdx.app.error("PantallaMenu", "No se encontró: " + path);
            return null;
        }
    }

    private void setButtonRects() {
        float w = VIRTUAL_WIDTH;
        float h = VIRTUAL_HEIGHT;
        float btnW = 240, btnH = 60;
        float espacio = 32;
        float totalH = 3 * btnH + 2 * espacio;
        float startY = h / 3.5f - totalH / 2f;

        float centerX = w / 1.15f - btnW / 2f;
        btnPlayRect.set(centerX, startY + 2 * (btnH + espacio), btnW, btnH);
        btnOptionsRect.set(centerX, startY + (btnH + espacio), btnW, btnH);
        btnExitRect.set(centerX, startY, btnW, btnH);


        btnCloseOptionsRect.set(centerX-64, -startY + 2 * (btnH + espacio), btnW, btnH);
        //checkbox
        float chkW = 56, chkH = 56;
        float chkEspacio = 48;
        float chkTotalH = 3 * chkH + 2 * chkEspacio;
        float chkStartY = chkTotalH - 160;
        float chkX = w / 2.5f - chkW / 2f;
        chkCRTBox.set(chkX, chkStartY + 2 * (chkH + chkEspacio), chkW, chkH);
        chkFlickerBox.set(chkX, chkStartY + (chkH + chkEspacio), chkW, chkH);
        chkShakeBox.set(chkX, chkStartY, chkW, chkH);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        setButtonRects();
    }

    @Override
    public void render(float delta) {

        if (crtEnabled) {
            scanlineOffset += 60 * delta * 0.5f;
            if (scanlineOffset > 4) scanlineOffset = 0;
        }

        if (flickerEnabled) {
            crtFlicker += 60 * delta * 0.1f;
            if (crtFlicker > Math.PI * 2) crtFlicker = 0;
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();


          if (!inOptionsMode) {
            if (backgroundTexture != null) {
                batch.draw(backgroundTexture, 0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
            } else {
                drawProceduralBackground(batch);
            }
        } else {
            if (optionsBackgroundTexture != null) {
                batch.draw(optionsBackgroundTexture, 0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
            } else {
                drawProceduralOptionsBackground(batch);
            }
        }


       if (titleTexture != null) {
            batch.draw(titleTexture, VIRTUAL_WIDTH/2f - titleTexture.getWidth()/2f, VIRTUAL_HEIGHT - 120);
        } else if (font != null) {
            font.setColor(Color.valueOf("F0C850"));
            font.getData().setScale(4.75f);
            font.draw(batch, "Envit", VIRTUAL_WIDTH/2f - 170, VIRTUAL_HEIGHT - 220);
        }


        if (!inOptionsMode) {
            drawButton(batch, btnPlayTexture, btnPlayRect, "JUGAR");
            drawButton(batch, btnOptionsTexture, btnOptionsRect, "OPCIONES");
            drawButton(batch, btnExitTexture, btnExitRect, "SALIR");
        } else {
            drawButton(batch, btnCloseOptionsTexture, btnCloseOptionsRect, "CERRAR");
            // Checkboxes
            drawCheckbox(batch, chkCRTBox, crtEnabled, "Efectos CRT");
            drawCheckbox(batch, chkFlickerBox, flickerEnabled, "Destellos");
            drawCheckbox(batch, chkShakeBox, shakeEnabled, "Temblor");
        }
        batch.end();

         if (crtEnabled) {
            drawCRTEffect();
        }

          handleInput();
    }

    private void drawButton(SpriteBatch batch, Texture texture, Rectangle rect, String texto) {
         com.badlogic.gdx.math.Vector2 mouse = viewport.unproject(new com.badlogic.gdx.math.Vector2(Gdx.input.getX(), Gdx.input.getY()));
        boolean hovered = rect.contains(mouse.x, mouse.y);

          if (texture != null) {
            batch.setColor(hovered ? new Color(15, 55, 175, 1) : Color.WHITE);
            batch.draw(texture, rect.x, rect.y, rect.width, rect.height);
            batch.setColor(Color.GOLDENROD);
        } else if (font != null) {
            batch.setColor(hovered ? new Color(15, 55, 175, 0.25f) : new Color(0,0,0,0.25f));
            batch.draw(getWhitePixel(), rect.x, rect.y, rect.width, rect.height);
            batch.setColor(Color.WHITE);
        }

         if (font != null) {
            font.setColor(hovered ? Color.valueOf("F0C850") : Color.WHITE);
            font.getData().setScale(1.2f);

            GlyphLayout layout = new GlyphLayout(font, texto);

            float textX = rect.x + (rect.width - layout.width) / 2f;
            float textY = rect.y + (rect.height + layout.height) / 2f;

            font.draw(batch, layout, textX, textY);
        }
    }


    private void drawCheckbox(SpriteBatch batch, Rectangle rect, boolean checked, String label) {
        com.badlogic.gdx.math.Vector2 mouse = viewport.unproject(new com.badlogic.gdx.math.Vector2(Gdx.input.getX(), Gdx.input.getY()));
        boolean hovered = rect.contains(mouse.x, mouse.y);
        Texture tex = checked ? chkCheckedTexture : chkUncheckedTexture;
        if (tex != null) {
            batch.setColor(hovered ? new Color(0.7f, 1f, 1f, 1f) : Color.WHITE);
            batch.draw(tex, rect.x, rect.y, rect.width, rect.height);
            batch.setColor(Color.WHITE);
        } else {
            batch.setColor(hovered ? new Color(0,1,1,0.25f) : new Color(0,0,0,0.25f));
            batch.draw(getWhitePixel(), rect.x, rect.y, rect.width, rect.height);
            batch.setColor(Color.WHITE);
        }
        if (font != null) {
            font.setColor(hovered ? Color.valueOf("F0C850") : Color.WHITE);
            font.getData().setScale(1f);
            // texto checkbox
            float labelX = rect.x + rect.width + 16;
            float labelY = rect.y + rect.height - 16;
            font.draw(batch, label, labelX, labelY);
        }
    }

    private void handleInput() {
        if (Gdx.input.justTouched()) {

            com.badlogic.gdx.math.Vector2 touch = viewport.unproject(new com.badlogic.gdx.math.Vector2(Gdx.input.getX(), Gdx.input.getY()));
            float x = touch.x;
            float y = touch.y;

            if (!inOptionsMode) {
                if (btnPlayRect.contains(x, y)) startGame();
                else if (btnOptionsRect.contains(x, y)) toggleOptions();
                else if (btnExitRect.contains(x, y)) Gdx.app.exit();
            } else {
                if (btnCloseOptionsRect.contains(x, y)) toggleOptions();
                else if (chkCRTBox.contains(x, y)) crtEnabled = !crtEnabled;
                else if (chkFlickerBox.contains(x, y)) flickerEnabled = !flickerEnabled;
                else if (chkShakeBox.contains(x, y)) shakeEnabled = !shakeEnabled;
            }
        }
    }

    private void toggleOptions() {
        inOptionsMode = !inOptionsMode;
    }

    private void startGame() {
        // Cambia a la pantalla de partida
        game.setScreen(new PantallaPartida(game));
    }

    private void cargarMusicaFondo() {
        canciones = new Music[rutasCanciones.length];
        for (int i = 0; i < rutasCanciones.length; i++) {
            if (Gdx.files.internal(rutasCanciones[i]).exists()) {
                canciones[i] = Gdx.audio.newMusic(Gdx.files.internal(rutasCanciones[i]));
                canciones[i].setLooping(true);
                canciones[i].setVolume(0.5f);
                canciones[i].play();
            }
        }
    }

    private void drawProceduralBackground(SpriteBatch batch) {

        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();


        shapeRenderer.rect(0, 0, w, h, new Color(0.04f, 0.04f, 0.12f,1), new Color(0.16f, 0.04f, 0.24f,1), new Color(0.24f, 0.08f, 0.39f,1), new Color(0.04f, 0.16f, 0.39f,1));


        for (int i = 0; i < 15; i++) {
            float x = random.nextInt(w);
            float y = random.nextInt(h);
            float size = random.nextInt(150) + 50;
            shapeRenderer.setColor(0, 1, 1, 0.12f);
            shapeRenderer.circle(x, y, size);
        }


        shapeRenderer.setColor(1, 0, 1, 0.16f);
        for (int i = 0; i < 20; i++) {
            float x1 = random.nextInt(w);
            float y1 = random.nextInt(h);
            float x2 = x1 + random.nextInt(200) - 100;
            float y2 = y1 + random.nextInt(200) - 100;
            shapeRenderer.rectLine(x1, y1, x2, y2, 2);
        }

        shapeRenderer.end();

        batch.begin();
    }

    private void drawProceduralOptionsBackground(SpriteBatch batch) {
        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();



        shapeRenderer.rect(0, 0, w / 2, h / 2, new Color(0.04f, 0.04f, 0.12f,1), new Color(0.16f, 0.04f, 0.24f,1), new Color(0.24f, 0.08f, 0.39f,1), new Color(0.04f, 0.16f, 0.39f,1));

        shapeRenderer.rect(w / 2, 0, w / 2, h, new Color(0.24f, 0.08f, 0.39f, 0.39f), new Color(0.08f, 0.16f, 0.39f, 0.39f), new Color(0.08f, 0.16f, 0.39f, 0.39f), new Color(0.24f, 0.08f, 0.39f, 0.39f));

        shapeRenderer.end();

        batch.begin();
    }

    private void drawCRTEffect() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        int w = (int)VIRTUAL_WIDTH;
        int h = (int)VIRTUAL_HEIGHT;


        shapeRenderer.setColor(0, 0, 0, 0.13f);
        for (int y = (int) scanlineOffset; y < h; y += 3) {
            shapeRenderer.rect(0, y, w, 1);
        }


        if (random.nextInt(60) < 2) {
            shapeRenderer.setColor(0, 0, 0, 0.06f);
            for (int x = 0; x < w; x += 2) {
                shapeRenderer.rect(x, 0, 1, h);
            }
        }

        shapeRenderer.end();


        if (flickerEnabled && random.nextInt(100) < 8) {
            batch.begin();
            Color flickerColor = new Color(1,1,1, 0.04f + 0.07f * (float)Math.sin(crtFlicker));
            batch.setColor(flickerColor);
            batch.draw(getWhitePixel(), 0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
            batch.setColor(Color.WHITE);
            batch.end();
        }


        if (random.nextInt(150) < 2) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            for (int i = 0; i < 40; i++) {
                float x = random.nextInt(w);
                float y = random.nextInt(h);
                float alpha = random.nextFloat() * 0.2f;
                shapeRenderer.setColor(1, 1, 1, alpha);
                shapeRenderer.rect(x, y, 2, 2);
            }
            shapeRenderer.end();
        }


        if (shakeEnabled && random.nextInt(300) < 2) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            int distortY = random.nextInt(h);
            shapeRenderer.setColor(1, 1, 1, 0.08f);
            shapeRenderer.rect(0, distortY, w, 3);
            shapeRenderer.end();
        }
    }


    private Texture whitePixel;

    private Texture getWhitePixel() {
        if (whitePixel == null) {
            Pixmap pixmap = new Pixmap(1,1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.WHITE);
            pixmap.fill();
            whitePixel = new Texture(pixmap);
            pixmap.dispose();
        }
        return whitePixel;
    }
    @Override
    public void pause() {}
    @Override
    public void resume() {}
    @Override
    public void hide() {}
    @Override
    public void show() {}
    
    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (optionsBackgroundTexture != null) optionsBackgroundTexture.dispose();
        if (whitePixel != null) whitePixel.dispose();
        if (font != null) font.dispose();
        if (btnPlayTexture != null) btnPlayTexture.dispose();
        if (btnOptionsTexture != null) btnOptionsTexture.dispose();
        if (btnExitTexture != null) btnExitTexture.dispose();
        if (btnCloseOptionsTexture != null) btnCloseOptionsTexture.dispose();
        if (chkCheckedTexture != null) chkCheckedTexture.dispose();
        if (chkUncheckedTexture != null) chkUncheckedTexture.dispose();
        if (titleTexture != null) titleTexture.dispose();
        // Detener y liberar música
        if (canciones != null) {
            for (Music musica : canciones) {
                if (musica != null) {
                    musica.stop();
                    musica.dispose();
                }
            }
        }
    }
}

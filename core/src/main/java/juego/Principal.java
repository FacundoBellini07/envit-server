package juego;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import juego.pantallas.PantallaMenu;

public class Principal extends Game {

	public SpriteBatch batch;

	@Override
	public void create() {
		batch = new SpriteBatch();
		setScreen(new PantallaMenu(this));
	}

	@Override
	public void render() {
		super.render(); // Esto llama al render() de la pantalla actual
	}

	@Override
	public void dispose() {
		batch.dispose();
		super.dispose(); // Por si la pantalla también tiene cosas que liberar
	}
}

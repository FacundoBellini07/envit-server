package juego;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import juego.pantallas.PantallaMenu;
import juego.pantallas.PantallaPartida;

public class Principal extends Game {

	public SpriteBatch batch;

	@Override
	public void create() {
		batch = new SpriteBatch();
		setScreen(new PantallaPartida(this));
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

// si no funciona el puerto netstat -ano | findstr 30243 tira <pid>, despues taskkill /PID <pid> /F
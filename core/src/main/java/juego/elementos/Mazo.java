package juego.elementos;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

import java.util.ArrayList;

public class Mazo {

    private ArrayList<Carta> cartas = new ArrayList<>();

    public Mazo() {
        for (int i = 0; i < Palo.values().length; i++) {
            for (int j = 0; j < 12; j++) {
                if(j!=7 && j!=8) {
                    this.cartas.add(new Carta((j + 1), Palo.values()[i]));
                }
            }
        }
    }

    public int getCantCartas(){
        return this.cartas.size();
    }

    public Carta getCarta(int indice){
        return this.cartas.get(indice);
    }
}
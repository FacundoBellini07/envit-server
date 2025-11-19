package juego.utilidades;

import java.util.InputMismatchException;
import java.util.Scanner;

public abstract class Consola {
public static Scanner s= new Scanner(System.in);
private Consola() {
	
}

	public static int ingresarEntero(final int MIN,final int MAX) {
		int num=0;
		boolean error=false;
		do {
			error=false;
			try {

				num=s.nextInt();
				if(num<MIN||num>MAX) {
					System.out.println("numero fuera del rango "+MIN+"-"+MAX);
					error=true;
				}
			}catch(InputMismatchException e) {
				System.out.println("tipo de dato mal ingresado");
				error=true;

			}catch(Exception e) {
				System.out.println("error no contemplado");
			}
			
		}while(error);
		return num;
	}

    public static void esperar(int milis) {
        try {
            Thread.sleep(milis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // preservar el estado de interrupción
        }
    }

}

package controllers;

import dungeon.DungeonLoader;
import dungeon.play.GameCharacter;
import dungeon.play.PlayMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Scanner;

import util.math2d.Point2D;


public class QLearning extends Controller {
	double[][][][] Q;

	double learningRate 	= 0.5;
	double discountFactor 	= 0.9;
	String tablaQ = "./tablasQ/tabla";
	Random random = new Random();

	int vida 		= 3;
	int acciones 	= 4;

	public QLearning(PlayMap map, GameCharacter controllingChar){
		super(map, controllingChar, "ZombieController");
		cargarTablaQ();
	}
	public QLearning(PlayMap map, GameCharacter controllingChar, String label){
		super(map, controllingChar, label);
		cargarTablaQ();
	}

	public int getNextAction(){
		Point2D pos = controllingChar.getPosition();
		int x = (int)pos.x;		
		int y = (int)pos.y;
		int v = -1;

		int vidaActual = controllingChar.getHitpoints();
		if(vidaActual <= 14) v = 0;
		else if(vidaActual <= 30) v = 1;
		else v = 2;

		// Elegir mejor accion
		int accion = -1;
		double maxQ = Double.NEGATIVE_INFINITY;

		for(int a=0; a<acciones; a++){
			if(Q[x][y][v][a] > maxQ){
				maxQ = Q[x][y][v][a];
				accion = a;
			}
		}

		return accion;
	}
		
	public int entrenar(double epsilon){
		// estado actual
		Point2D state = controllingChar.getPosition();
		int x = (int)state.x;		
		int y = (int)state.y;
		int v = -1;

		int vidaActual = controllingChar.getHitpoints();
		if(vidaActual <= 14) v = 0;
		else if(vidaActual <= 30) v = 1;
		else v = 2;


		// e-greedy
		int accion = -1;
		double maxQ = Double.NEGATIVE_INFINITY;
	
		if(random.nextDouble() < epsilon){
			for(int a=0; a<acciones; a++){
				if(Q[x][y][v][a] > maxQ){
					maxQ = Q[x][y][v][a];
					accion = a;
				}
			}
		}else accion = random.nextInt(4);


		// siguiente estado
		Point2D next_state = controllingChar.getNextPosition(accion);
		int next_x = (int)next_state.x;
		int next_y = (int)next_state.y;
		int next_v = v;

		if(!map.isValidMove(next_state)){
			next_x = x;
			next_y = y;
		}

		int vidaSiguiente = vidaActual;
		if(map.isMonster(next_x, next_y)) vidaSiguiente -= map.getMonsterChar( map.getMonsterIndex(next_x, next_y) ).getDamage(); 
	
		if(vidaSiguiente <= 14) next_v = 0;
		else if(vidaSiguiente <= 30) next_v = 1;
		else next_v = 2;	


		// recompensas
		double reward = -0.01;// -(40-vidaActual)/400.0; //-0.01;
		if(map.isExit(next_x, next_y)) reward = 100;
		else if(vidaSiguiente <= 0) reward = -1;
		//else if(map.isPotion(next_x, next_y) && v == 0) reward = 0.5;

		
		// actualizar tabla q
		maxQ = Double.NEGATIVE_INFINITY;
		for(int a=0; a<acciones; a++) maxQ = Math.max(maxQ, Q[next_x][next_y][next_v][a]);
		Q[x][y][v][accion] += learningRate * (reward + discountFactor*maxQ - Q[x][y][v][accion]);

		return accion;
	}


	public void cargarTablaQ() {
		tablaQ = tablaQ + buscarMapaActual();

		if(Files.exists(Paths.get(tablaQ))){
			try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(tablaQ))) {
				Q = (double[][][][]) ois.readObject();
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}

		} else {
			int X = map.getMapSizeX();	
			int Y = map.getMapSizeY();

			Q = new double[X][Y][vida][acciones];		

			for(int x=0; x<X; x++){
				for(int y=0; y<Y; y++){
					for(int v=0; v<vida; v++){
						for(int a=0; a<acciones; a++){
							Q[x][y][v][a] = 0;		
						}
					}
				}
			}
		}
	}

	public int buscarMapaActual(){
		String mapaActual = map.toASCII(false).replace("@", "E");
		
		int mapa = -1;
		String asciiMap = "";
		for(int i=0; i<=10; i++){
			try { 
				Scanner scanner = new Scanner( new File("./dungeons/map" + i + ".txt") );
				asciiMap = scanner.useDelimiter("\\A").next();
				scanner.close();

			} catch(Exception e){
				System.out.println(e.toString());
			}

			PlayMap testPlay = new PlayMap(DungeonLoader.loadAsciiDungeon(asciiMap));
			
			if(mapaActual.equals(testPlay.toASCII(false))){
				mapa = i;
				break;
			}
		}

		return mapa;
	}

	public void guardarTablaQ() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tablaQ))) {
            oos.writeObject(Q);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package controllers;

import dungeon.Dungeon;
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


public class DobleQLearning extends Controller {
	double[][][][] Q1, Q2;

	double learningRate 	= 0.5;
	double discountFactor 	= 0.9;
	String tablaQ = "./tablasQ/tabla";
	Random random = new Random();

	int vida 		= 3;
	int acciones 	= 4;

	public DobleQLearning(PlayMap map, GameCharacter controllingChar){
		super(map, controllingChar, "ZombieController");
		cargarTablaQ();
	}
	public DobleQLearning(PlayMap map, GameCharacter controllingChar, String label){
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
			if(Q1[x][y][v][a] + Q2[x][y][v][a] > maxQ){
				maxQ = Q1[x][y][v][a] + Q2[x][y][v][a];
				accion = a;
			}
		}

		return accion;
	}
		
	public int entrenar(double epsilon){
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
				if(Q1[x][y][v][a] + Q2[x][y][v][a] > maxQ){
					maxQ = Q1[x][y][v][a] + Q2[x][y][v][a];
					accion = a;
				}
			}
		}else accion = random.nextInt(4);


		// max_a Q(s', a)
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
		double reward = -0.01;
		if(map.isExit(next_x, next_y)) reward = 1;
		else if(vidaSiguiente <= 0) reward = -1;
		

		int b = 1;
		if(b == 1){
			maxQ = Double.NEGATIVE_INFINITY;
			for(int a=0; a<acciones; a++) maxQ = Math.max(maxQ, Q1[next_x][next_y][next_v][a]);
			Q1[x][y][v][accion] += learningRate * (reward + discountFactor*maxQ - Q1[x][y][v][accion]);

			return accion;
		}else{
			



		// actualizar tablas Q
		maxQ = Double.NEGATIVE_INFINITY;
		int maxA = -1;
		if(random.nextDouble() < 0.5){
			for(int a=0; a<acciones; a++){
				if(Q1[x][y][v][a] > maxQ){
					maxQ = Q1[x][y][v][a];
					maxA = a;
				}
			}
			Q1[x][y][v][accion] += learningRate * (reward + discountFactor * Q2[next_x][next_y][next_v][maxA] - Q1[x][y][v][accion]);

		}else{
			for(int a=0; a<acciones; a++){
				if(Q2[x][y][v][a] > maxQ){
					maxQ = Q2[x][y][v][a];
					maxA = a;
				}
			}
			Q2[x][y][v][accion] += learningRate * (reward + discountFactor * Q1[next_x][next_y][next_v][maxA] - Q2[x][y][v][accion]);
		}

		return accion;
		}
	}


	public void cargarTablaQ() {
		tablaQ = tablaQ + buscarMapaActual();

		if(Files.exists(Paths.get(tablaQ + "-1"))){
			try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(tablaQ + "-1"))) {
				Q1 = (double[][][][]) ois.readObject();
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}

			try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(tablaQ + "-2"))) {
				Q2 = (double[][][][]) ois.readObject();
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}

		} else {
			int X = map.getMapSizeX();	
			int Y = map.getMapSizeY();

			Q1 = new double[X][Y][vida][acciones];			
			Q2 = new double[X][Y][vida][acciones];

			for(int x=0; x<X; x++){
				for(int y=0; y<Y; y++){
					for(int v=0; v<vida; v++){
						for(int a=0; a<acciones; a++){
							Q1[x][y][v][a] = 0;						
							Q2[x][y][v][a] = 0;
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
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tablaQ + "-1"))) {
            oos.writeObject(Q1);
        } catch (IOException e) {
            e.printStackTrace();
        }

		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tablaQ + "-2"))) {
            oos.writeObject(Q2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package controllers;

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
	double[][][] Q1;	
	double[][][] Q2;

	double learningRate 	= 0.5;
	double discountFactor 	= 0.9;
	public double epsilon	= 0.1;
	String tablaQ = "./tablasQ/tabla";
	Random random = new Random();

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

		// Elegir mejor accion
		int accion = -1;
		double maxQ = Double.NEGATIVE_INFINITY;

		if(random.nextDouble() < epsilon){
			for(int a=0; a<4; a++){
				if(Q1[x][y][a] + Q2[x][y][a] > maxQ){
					maxQ = Q1[x][y][a] + Q2[x][y][a];
					accion = a;
				}
			}
		}

		return accion;
	}
		
	public int entrenar(){
		Point2D state = controllingChar.getPosition();
		int x = (int)state.x;		
		int y = (int)state.y;

		// e-greedy
		int accion = -1;
		double maxQ = Double.NEGATIVE_INFINITY;
	
		if(random.nextDouble() < epsilon){
			for(int a=0; a<4; a++){
				if(Q1[x][y][a] + Q2[x][y][a] > maxQ){
					maxQ = Q1[x][y][a] + Q2[x][y][a];
					accion = a;
				}
			}
		}else accion = random.nextInt(4);


		// max_a Q(s', a)
		Point2D next_state = controllingChar.getNextPosition(accion);
		int next_x = (int)next_state.x;
		int next_y = (int)next_state.y;

		if(!map.isValidMove(next_state)){
			next_x = x;
			next_y = y;
		}



		// recompensas
		double reward = -0.5; //-0.01;
		if(map.isExit(next_x, next_y)) reward = 10;
		//else if(map.isReward(next_x, next_y)) reward = 1;
		//else if((map.isPotion(next_x, next_y)) && controllingChar.getHitpoints() <= 30) reward = 0.1;
		else if(map.isMonster(next_x, next_y)){
			reward = -0.1;

			//if(controllingChar.getHitpoints() <= 14) reward = -0.01;
			//else reward = 1;
		}
		

		// actualizar valor q
		maxQ = Double.NEGATIVE_INFINITY;
		if(random.nextDouble() < 0.5){
			for(int a=0; a<4; a++) maxQ = Math.max(maxQ, Q2[next_x][next_y][a]);
			Q1[x][y][accion] = Q1[x][y][accion] + learningRate * (reward + discountFactor*maxQ - Q1[x][y][accion]);

		}else{
			for(int a=0; a<4; a++) maxQ = Math.max(maxQ, Q1[next_x][next_y][a]);
			Q2[x][y][accion] = Q2[x][y][accion] + learningRate * (reward + discountFactor*maxQ - Q2[x][y][accion]);
		}

		return accion;
	}


	public void cargarTablaQ() {
		tablaQ = tablaQ + buscarMapaActual();

		if(Files.exists(Paths.get(tablaQ + "-1"))){
			try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(tablaQ + "-1"))) {
				Q1 = (double[][][]) ois.readObject();
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}

			try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(tablaQ + "-2"))) {
				Q2 = (double[][][]) ois.readObject();
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}

		} else {
			int X = map.getMapSizeX();	
			int Y = map.getMapSizeY();
			int acciones = 4;

			Q1 = new double[X][Y][acciones];			
			Q2 = new double[X][Y][acciones];


			for(int x=0; x<X; x++){
				for(int y=0; y<Y; y++){
					for(int a=0; a<acciones; a++){
						Q1[x][y][a] = 0;						
						Q2[x][y][a] = 0;
					}
				}
			}
		}
	}

	public int buscarMapaActual(){
		String mapaActual="";
		for(int y=0;y<map.getMapSizeY();y++){
			for(int x=0;x<map.getMapSizeX();x++){
				if(!map.isPassable(x, y)){ 
					mapaActual+="#"; 
				} else if(map.isEntrance(x,y)){
					mapaActual+="E"; 
				} else if(map.isExit(x,y)){
					mapaActual+="X"; 
				} else if(map.isMonster(x,y)){
					mapaActual+="m"; 
				} else if(map.isReward(x,y)){
					mapaActual+="r"; 
				} else if(map.isPotion(x,y)){
					mapaActual+="p"; 
				} else {
					mapaActual+=".";
				}				
			}
			mapaActual+="\n";
		}
		mapaActual+=(char)13;
		mapaActual+=(char)10;

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

			if(mapaActual.equals(asciiMap)){
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

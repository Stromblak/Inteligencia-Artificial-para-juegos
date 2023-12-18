package experiment;

import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;

import controllers.*;

import dungeon.Dungeon;
import dungeon.DungeonLoader;
import dungeon.play.PlayMap;
import dungeon.visualization.PlayVisualizer;

import util.math2d.Matrix2D;
import util.statics.StatisticUtils;



public class DebugMode {
	final int maxActions = 300;

	public void runTest(String filename, int ep, int MAXEPS){
		String[] temp = filename.split("/");
		String mapFile = temp[temp.length-1];
		
		String asciiMap = "";
		try { 
			asciiMap = new Scanner(new File(filename)).useDelimiter("\\A").next(); 
		} catch(Exception e){
			System.out.println(e.toString());
		}
		Dungeon testDungeon = DungeonLoader.loadAsciiDungeon(asciiMap);
		PlayMap testPlay = new PlayMap(testDungeon);
		testPlay.startGame();

		//Controller testAgent = new RoombaController(testPlay,testPlay.getHero());
		//DobleQLearning testAgent = new DobleQLearning(testPlay,testPlay.getHero());		
		QLearning testAgent = new QLearning(testPlay,testPlay.getHero());

		double epsilon = 0.01; //Math.max(0.1, 1.0 - ep*0.9/MAXEPS );
		

		int actions = 0;

		//System.out.println(testPlay.toASCII(true));
		//System.out.println(asciiMap);


		while(!testPlay.isGameHalted() && actions<maxActions){
			//testPlay.updateGame(testAgent.getNextAction());			
			testPlay.updateGame(testAgent.entrenar(epsilon));
				
			actions++;


			//System.out.println("----- ACTION "+actions+" -----");
			//System.out.println(testPlay.toASCII(true));
			//System.out.println(PlayVisualizer.renderHeatmapDungeon(testPlay));
		}

		testAgent.guardarTablaQ();
	}
	
	public static void main(String[] args) {
		int MAXEPS = 10000;

		DebugMode exp = new DebugMode();

		for(int i=8; i<=8; i++){
			System.out.println(i);
			for(int j=0; j<MAXEPS; j++){
				if(j%1000 == 0) System.out.println(j);
				exp.runTest("./dungeons/map" + i + ".txt", j, MAXEPS);
			}
		}

		CompetitionMode exp2 = new CompetitionMode();
		for(int i=8;i<=8;i++){
			System.out.println("\n--------------\nMAP"+i+"\n--------------\n");
			exp2.runCompetition("./dungeons/map"+i+".txt");
		}
	}
}

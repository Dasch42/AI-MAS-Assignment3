package client.node.map;


import java.io.*;
import java.util.*;

import java.awt.Point;


import client.node.storage.*;


/*
	@author: Daniel Schougaard
*/

public class Level implements LevelInterface{


	/*
			-----------> col
			|
			|
			|
			|
			|
		    \/
		    row


	*/

	/*
		Private variables for Level
	*/

	private int maxRow;
	private int maxCol;
	private DistanceMap dm;


	public class Cell{

		private Type type;
		private char letter;

		public Cell(Type type){
			this.type = type;
		}

		public Cell(Type type, char letter){
			this.letter = letter;
			this.type = type;
		}
	}

	private static Cell[][] map;

	public enum Type { SPACE, WALL, GOAL, BOX, AGENT }

	private HashMap<Character, ArrayList<Goal> > goals;






	// Constructor
	public Level(int maxRow, int maxCol){
		this.maxCol 	= maxCol;
		this.maxRow 	= maxRow;

		map 			= new Cell[maxCol][maxRow];
		this.goals 		= new HashMap<Character, ArrayList<Goal>>();

	}	

	public Level(DistanceMap dmap){
		this.goals 				= new HashMap<Character, ArrayList<Goal> >();
		this.dm 				= dmap;
	}


	// Setup methods for the Level
	public void addWall(int row, int col){
		this.map[row][col] = new Cell(Type.WALL);
	}

	public void addGoal(int row, int col, char letter){
		this.map[row][col] = new Cell(Type.GOAL, letter);

		if( !goals.containsKey(new Character(letter)) ){
			goals.put( new Character(letter), new ArrayList<Goal>() );
		}

		ArrayList<Goal> tempGoals = goals.get( new Character(letter) );
		tempGoals.add(new Goal(letter, row, col));
	}

	public void addSpace(int row, int col){
		this.map[row][col]  = new Cell(Type.SPACE);
	}



	/*
		Interface for the Heuristic to use
	*/

	public int getCol(){
		return this.maxCol;
	}

	public int getRow(){
		return this.maxRow;
	}


	public char isGoal(int row, int col){
		if( this.map[row][col] .type == Type.GOAL )
			return this.map[row][col] .letter;

		return '\0';
	}

	public boolean isWall(int col, int row){
		return ( this.map[col][row].type == Type.WALL );
	}


	public ArrayList<Goal> getGoals(char chr){
		return this.goals.get(new Character(chr));
	}


	public HashMap<Character, ArrayList<Goal>> getGoalMap(){
		return this.goals;
	}

	public ArrayList<Goal> getAllGoals(){
		ArrayList<Goal> returnGoals = new ArrayList<Goal>();
		for( Character c  : goals.keySet() ){
			returnGoals.addAll(this.goals.get(c));
		}
		return returnGoals;
	}

	public int distance(int rowFrom, int colFrom, int rowTo, int colTo){
		return this.dm.distance(rowFrom, colFrom, rowTo, colTo);
	}
}



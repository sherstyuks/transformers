package com.compmodel.sim.trsfr.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;

public class Coordinates  implements Serializable{
	private static final long serialVersionUID = 7477570124358252379L;
	private static final long RANDOM_SEED = 5772891L;
	public static Random rand = new Random(RANDOM_SEED);
	private int[] coords = new int[World.SPACE_DIM];
	public Coordinates(int[] coords) {
		for(int i=0;i<World.SPACE_DIM;i++) {
			this.coords[i] = coords[i];
		}
	}
	
	public int[] getCoords() {
		return coords;
	}

	public void setCoords(int[] coords) {
		this.coords = coords;
	}
	public static String deltaToString(Coordinates c1, Coordinates c2) {
		return "["+(c1.getCoords()[0]-c2.getCoords()[0])+"]["
				+(c1.getCoords()[1]-c2.getCoords()[1])+"]";
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<World.SPACE_DIM;i++) {
			sb.append("[").append(coords[i]).append("]");
		}
		return sb.toString();
	}
	
	/**
	 * Return array of points located within given distance from center,
	 * inclusive.
	 * 
	 * @param center
	 * @param distance
	 * @return
	 */
	public static ArrayList<Coordinates> getVicinity(Coordinates center, int distance){
		int[] startIdx = new int[World.SPACE_DIM];
		int[] endIdx = new int[World.SPACE_DIM];
		int[] centerc = center.getCoords();
		ArrayList<Coordinates> result = new ArrayList<Coordinates>();
		// Determine a cube
		for(int i=0;i<World.SPACE_DIM;i++) {
			startIdx[i] = centerc[i] < distance?0:centerc[i] - distance;
			endIdx[i] = centerc[i] >= World.SPACE_SIZE - distance?World.SPACE_SIZE-1:centerc[i] + distance;
		}
		/* Check each point in a cube if the distance is within the given range.
		 */
		for(int i=startIdx[0];i<=endIdx[0];i++) {
			for(int j=startIdx[1];j<=endIdx[1];j++) {
				if(calcDistance(center,i,j)<=distance) {
					result.add(new Coordinates(new int[] {i,j}));
				}
			}
		}
		return result;
	}

	/**
	 * Return array of points located on the surface with given distance from center,
	 * Note, algorithm is not optimized for our definition of distance, could be calculated more efficiently.
	 * @param center
	 * @param distance
	 * @return
	 */
	public static ArrayList<Coordinates> getSurface(Coordinates center, int distance){
		int[] startIdx = new int[World.SPACE_DIM];
		int[] endIdx = new int[World.SPACE_DIM];
		int[] centerc = center.getCoords();
		ArrayList<Coordinates> result = new ArrayList<Coordinates>();
		// Determine a cube
		for(int i=0;i<World.SPACE_DIM;i++) {
			startIdx[i] = centerc[i] < distance?0:centerc[i] - distance;
			endIdx[i] = centerc[i] >= World.SPACE_SIZE - distance?World.SPACE_SIZE-1:centerc[i] + distance;
		}
		/* Check each point in a cube if the distance is within the given range.
		 */
		for(int i=startIdx[0];i<=endIdx[0];i++) {
			for(int j=startIdx[1];j<=endIdx[1];j++) {
				if(calcDistance(center,i,j) == distance) {
					result.add(new Coordinates(new int[] {i,j}));
				}
			}
		}
		return result;
	}
	
	/**
	 * Distance is calculated as a sum of differences on each dimension.
	 * This is to simplify calculation, euclidean distance takes too much resources.
	 * @param center
	 * @param i
	 * @param j
	 * @return
	 */
	
	public static int calcDistance(Coordinates center, int... axisCoords) {
		int distance = 0;
		for(int i=0;i<World.SPACE_DIM;i++) {
			distance += Math.abs(center.getCoords()[i]-axisCoords[i]);
		}
		return distance;
	}

	public static int calcDistance(Coordinates coord1, Coordinates coord2) {
		int distance = 0;
		for(int i=0;i<World.SPACE_DIM;i++) {
			distance += Math.abs(coord1.getCoords()[i]-coord2.getCoords()[i]);
		}
		return distance;
	}

	public static Coordinates createRandom() {
		int[] coords = new int[World.SPACE_DIM];
		for(int i=0;i<World.SPACE_DIM;i++) {
			coords[i] = rand.nextInt(World.SPACE_SIZE);
		}
		return new Coordinates(coords);
	}
}

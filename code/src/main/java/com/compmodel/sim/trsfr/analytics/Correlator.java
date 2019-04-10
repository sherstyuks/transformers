package com.compmodel.sim.trsfr.analytics;

import java.util.HashMap;
import java.util.Map;

import com.compmodel.sim.trsfr.core.Chain;
/**
 * Calculates correlation function for two input chains.
 * Only transformer types count, coordinates are ignored.
 * 
 * Result is normalized on the average chain length,
 * so that if it is autocorrelation, result(0) = 1.
 * 
 * Variable is a position shift of one chain relatively to another.
 * For example:
 * chain1 = (AB,BC,BA,BC,CB)
 * chain2 = (DA,AD,AB)
 * result (-2):
 *       (AB,BC,BA,BC,CB)
 * (DA,AD,AB)
 * result (-1):
 *       (AB,BC,BA,BC,CB)
 *    (DA,AD,AB)    
 * result (0):
 *       (AB,BC,BA,BC,CB)
 *       (DA,AD,AB)
 * result (4):
 *       (AB,BC,BA,BC,CB)
 *                   (DA,AD,AB)
 * 
 */
public class Correlator {
	public static Map<Integer, Double> correlate(Chain chain1, Chain chain2) {
		Map<Integer, Double> result= new HashMap<Integer, Double>();
		double avgLength = (chain1.getLength() + chain2.getLength())/2.0;
		int minX = 1 - chain2.getLength();
		int maxX = chain1.getLength() - 1;
		for (int curX=minX;curX<=maxX;curX++) {
			double curCorr = calculateCorrPoint(chain1,chain2,curX);
			result.put(curX, curCorr / avgLength) ;
		}
		return result;		
	}

	private static double calculateCorrPoint(Chain chain1, Chain chain2, int curX) {
		int sum = 0;
		/* For two chains above:
		 *  curX=-2, maxI=3, minI = 2, sum = chain1[0]xchain2[2]
		 *  curX=-1, maxI=3, minI = 1, sum = chain1[0]xchain2[1] + chain1[1]xchain2[2]
		 *  curX=-0, maxI=3, minI = 0, sum = chain1[0]xchain2[0] + chain1[1]xchain2[1] + chain1[2]xchain2[2]
		 *  curX= 1, maxI=3, minI = 0, sum = chain1[1]xchain2[0] + chain1[2]xchain2[1] + chain1[3]xchain2[2]
		 *  curX= 2, maxI=3, minI = 0, sum = chain1[2]xchain2[0] + chain1[3]xchain2[1] + chain1[4]xchain2[2]
		 *  curX= 3, maxI=2, minI = 0, sum = chain1[3]xchain2[0] + chain1[4]xchain2[1] 
		 *  curX= 4, maxI=1, minI = 0, sum = chain1[4]xchain2[0]  
		 */
		int maxI = Math.min(chain1.getLength() - curX, chain2.getLength()); 
		int minI = Math.max((-1) * curX, 0); // curX=-1 -> 1
		for(int i=minI;i<maxI;i++) {
			int curCross = chain1.getLinks().get(curX + i).cross(chain2.getLinks().get(i));
			sum += curCross;
		}
		return sum;
	}

}

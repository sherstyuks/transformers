package com.compmodel.sim.trsfr.core;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Notes
 * General principle:
 * Random input (seeding) should generate non-random, periodical behavior.
 *   Obvious example - self-sufficient groups.
 *   
 * Expected behavior:
 * transformers gathered in groups
 * when appropriate atom seeded in vicinity, transformers perform actions.
 *   size of the group is limited to the capacity of vicinity, because we cannot move atoms
 *     makes sense to try make attraction force to move both participants: transformer and atom
 *     this way we can expand cluster indefinitely.
 *   if there is another compatible atom next to the group, it will destroy the group.
 *      which means, atoms should be scarce. 
 * 
 * Happy path for group: atom seeded, each transformer performs matching action, 
 *   so that there always is next in line to consume the result.
 *     one scenario is when group is self-sufficient, i.e. end result can be consumed again.
 * 
 * Problem 1: transformers act randomly during the turn (same would be if we did it using multithreading)
 *   One of the consequences - we cannot synchronize groups. Which means, we need a mechanism that will
 *   ensure sequential acting.
 *   
 * Problem 2: groups are falling apart
 * 
 * Solution:
 *   When transformer acts:
 *     - atom moves closer by 1 always (if possible)
 *     - transformer moves closer by 1 with probability 1/MASS_RATIO.
 *     
 *  The result is that random groups are created. 
 *    - There is no spatial arrangements within group. On average  they have circular shape changing wildly.
 *    - Small groups, 2-6 transformers, travels randomly for long distances
 *    - The bigger group is, more stationary it is. 
 *    - Big groups are no stable. The can:
 *      - spawn single trsfr and often capture it back
 *      - shear small group, usually it breaks down completely and moves away
 *      - fall apart creating two groups.
 *      - merge with another group
 *    - There does not seem any patterns exists how trsfrs are grouped. 
 *    - Looks like all combinations behave the same way. No indications that certain set of trsfrs is more stable than the others.
 *    - There does not seem to be any patterns how groups travel and interact.
 *     
 *   So the question now is: how can next level groups can be created? Or in other words, how should patterns be created
 *   when we have randomly populated groups and random seeding? Where resonance can occur?
 *   
 *   Note, resonance requires sequential execution. 
 *   
 *   In the meantime, add features to the program:
 *   1. Verify we do not place transformers one onto another
 *   2. Optimize colors
 *   3. Save/load world history.
 *      - allow to resume from saved state.
 *   4. UI
 *      - load history
 *      - play history with params:
 *        - show every N-th seed
 *        - frame rate
 *        - view size
 *        - show atoms
 *      - generate shots for a history with params:
 *        - show every N-th seed
 *        - view size
 *        - show atoms
 *      - change settings
 *      - resume run, displaying world every N seeds
 *    
 *    Run header:
 *      - spaceSize
 *      - atomTypeVariations
 *      - initSearchDistance
 *      - randomSeed
 *      - atomsCntInSeed
 *      - transformersCnt
 *      - massRatio
 *      - turnsPerSeed
 *      - maxIdleCnt
 *      - temperature
 *    
 *    Run history record
 *      - timestamp
 *      - turnNum
 *      - seedNum
 *      - record number
 *      - transformers
 *      - atoms, optional
 *      
 *  Problem 3: Transformers create large groups with quasi-circular shape. Such shapes exhibit random behavior:
 *   - split and merge with time, without any regularity.
 *   - do not form bonds to other groups.
 *   
 *  We need to invent a mechanism that will suppress "randomness". Group action must be repeatable to be able to build a resonance
 *  with other groups. 
 *  - we cannot allow interactions between groups during one seed cycle. Because transformers are acting randomly,
 *    such interaction breaks "repetitiveness".
 *      This requirement can be achieved by following constructs:
 *      - transformers linked in chains, valence can be 2 or 3, in 3-D world
 *      - atom to interact with could be no further than 1 point
 *      - after interaction atom moves right into the same spot as transformer, 
 *        thus allowing the next transformer in chain to pick it up. 
 *        It also hides atom from the reach of external transformers.
 *      - transformers cannot be placed next to each other, unless they are in chain.
 *        Which means it will be at least one point between two transformers belonging to diff chains. 
 *      - chains will be formed as usual - when transformers move towards interacting atom.
 *   - rules for transformer to be torn off the chain are not obvious. 
 *     Because of repetitiveness, there should no reasons for part of the chain to not be executed,
 *     when proper input is provided.
 *     If we impose a rule:
 *       - after each seed cycle check transformer activity, and the less activity, the bigger chances to move.
 *     The result of this rule will be that beginning of the chain is more likely to move than the end,
 *     because chain works as a collector of appropriate atoms flying nearby. Each such atom will be pushed
 *     down the chain, thus making ending part of the chain acting more frequently than the beginning one.
 *     
 *   - the expected result will be a number of chains with relatively stable endings and more volatile beginnings.
 *     Such chain survives by gathering appropriate atoms that are seeded or move in the close vicinity of a chain.
 *     
 *   - now, the very end of such collector becomes clogged. Atoms will occupy the free slots under transformers,
 *     not allowing the latter to act. This process will cause the end of the chain to become susceptible to be detached.
 *       
 *   - To be able to behave like described above, i.e. have channeling chains with stable core,
 *     we need a proper way of seeding atoms. Full replacement of atoms array destroys what has been
 *     accumulated by the end of seed cycle. We need a less disruptive way of seeding, so that
 *     some history could be preserved. For example:
 *     - after the initial seed we run seed cycles
 *     - after all turns in the cycle are finished, we make a partial seed:
 *       - RESEED_PCT atoms will be removed randomly
 *       - RESEED_PCT atoms will be seeded randomly.
 *       
 *     
 *   
 *      
 * @author Sergey Sherstyuk
 *
 */

public class World implements Serializable{
	private static final long serialVersionUID = 237404165099225379L;

	private static final Logger log = LoggerFactory.getLogger(World.class);

	/**
	 * World default parameters
	 */
	public static final int ATOM_TYPE_NUMBER=4;	// Number of atom types
	public static final int SPACE_DIM = 2;
	public static final int SPACE_SIZE = 200;
	public static final int ATOMS_NUMBER = 3000;
	public static final int TRSFR_NUMBER = 1000;
	public static final int MAX_TEMPERATURE = 100;
	public static final int MAX_NEIGHBOR_DISTANCE = 1;
	public static final int MAX_NEIGHBOR_NUMBER = 2;
	public static final int SEARCH_DISTANCE = 3;   // Max distance between transform and atom for transformation to be allowed
	public static final double ACTION_DISTANCE_PENALTY = 0.4; // The further atom is, the smaller the probability of action. The smaller, the easier
	public static final double ENERGY_SCALE = 1.0; // Determines probability to climb over energy barrier. The smaller, the harder
	public static final int TURNS_PER_SEED = 15;
	public static final int RESEED_PCT = 10;  // Percent of atoms to be replaced in reseed
	public static final int MASS_RATIO = 4;   //Determines how we adjust atom and transformer positions after interaction	
	public static final int MASS_RATIO_LINKED = 4;  // Determines positions adjustment when trsf is in the chain	
	public static final int TEMPERATURE = 10; // determines probability of random move
	public static final int IDLE_WAIT = 5;	  // seeds transformer can wait without making any actions before it starts to move randomly 
	private static final int MAX_FILES_CNT = 3000;
	private static final int  SAVE_SHOT_PERIOD = 500;
	private static final int  SAVE_SNAPSHOT_PERIOD = 10; // one snapshot per 100 shots
	private static final boolean SHOW_ATOMS = true; 
	private static final String FILE_DIR = "d:\\sergey\\javaproj\\trsfr\\shots\\shots02\\"; //"c:\\Users\\Aii3x\\sergey\\shots\\shots06\\";
	private static final long RANDOM_SEED = 3432716543l;
	private static Random rand = new Random(RANDOM_SEED);
	
	public Transformer[][] trsfSpace;
	public Atom[][] atomSpace;
	
	private int atomTypeNumber;
	private int searchDistance;
	private double actionDistancePenalty;
	private int maxNeighborDistance;
	private int maxNeighborNumber = MAX_NEIGHBOR_NUMBER;
	private double energyScale;
	private int turnsPerSeed;
	private int reseedPct;
	private int atomsNumber;
	private int trsfrNumber;
	private int massRatio;
	private int massRatioLinked;
	private double trsfrMoveChance;
	private int temperature;
	private int idleWait;
	private int maxFilesCnt;
	private int saveShotPeriod;
	private int saveSnapShotPeriod;
	private boolean showAtoms;
	private String fileDir;
	public ArrayList<Atom> atoms;
	public ArrayList<Transformer> transformers;
			
	private transient int  turnCnt = 0;
	private long  totalTurns = 0;
	private transient boolean isPaused = false;
	private long seedCnt = 0;
	private int fileCntTransformers = 0;
	
	public World() {
		init();
	}
	
	private void init() {
		atomTypeNumber = ATOM_TYPE_NUMBER;
		searchDistance = SEARCH_DISTANCE;
		actionDistancePenalty = ACTION_DISTANCE_PENALTY;
		maxNeighborDistance = MAX_NEIGHBOR_DISTANCE;
		energyScale = ENERGY_SCALE;
		turnsPerSeed = TURNS_PER_SEED;
		reseedPct = RESEED_PCT;
		atomsNumber = ATOMS_NUMBER;
		trsfrNumber = TRSFR_NUMBER;
		massRatio = MASS_RATIO;
		massRatioLinked = MASS_RATIO_LINKED;
		trsfrMoveChance = 1.0/massRatio;
		temperature = TEMPERATURE;
		idleWait = IDLE_WAIT;
		maxFilesCnt = MAX_FILES_CNT;
		saveShotPeriod =  SAVE_SHOT_PERIOD;
		saveSnapShotPeriod =  SAVE_SNAPSHOT_PERIOD;
		showAtoms = SHOW_ATOMS;
		fileDir = FILE_DIR;
		atoms = new ArrayList<Atom>();
		transformers = new ArrayList<Transformer>();
	}

	public void run() {
		seedCnt = 0;
		while (!isPaused){
			seedAtoms();
			resetActionCount();
			for(turnCnt=0;turnCnt<turnsPerSeed;turnCnt++) {
				//saveShotForTransformers();
				nextTurn();
			}
			updateIdleTransformers();
			if(seedCnt % saveShotPeriod == 0) {
				saveShotForTransformers();
				if(fileCntTransformers % saveSnapShotPeriod == 0) {
					saveWorldSnapshot();
				}
			}
			if(fileCntTransformers >= maxFilesCnt) {  
				log.info("===== run finished =====");
				return;
			}
		}
	}

	/**
	 * Serialize current instance of the world.
	 */
	private void saveWorldSnapshot() {
		try {  
			String fileName = fileDir+"world_snapshot_"+String.format("%05d",fileCntTransformers)+".trsf";
            FileOutputStream file = new FileOutputStream(fileName); 
            ObjectOutputStream out = new ObjectOutputStream(file); 
            out.writeObject(this); 
            out.close(); 
            file.close();
            log.info(" === Saved snapshot {}",fileName);
        }  catch(IOException ex) { 
        	ex.printStackTrace(); 
        } 
		
	}
	

	/**
	 * After all turns finished check all transformers,
	 * if there were actions during the seed.
	 * If no actions, increase transformer idleCnt.
	 * If idleCnt reached IDLE_WAIT:
	 *   - move transformer randomly
	 *   - reset idleCnt.
	 */
	private void updateIdleTransformers() {
		transformers.stream().forEach(t -> {
			if(t.getActionCnt() == 0) {
				t.setIdleCnt(t.getIdleCnt() + 1);
				if(moveTransformerRandomly(t)) {
					t.setIdleCnt(0);
				}
			}
		});		
	}

	/**
	 * After each seed, before running turns,
	 * reset actionCnt for transformers
	 */
	private void resetActionCount() {
		transformers.stream().forEach(t -> t.setActionCnt(0));	
	}

	/**
	 * World runs by turns.
	 * During the turn each transformer gets a chance to act.
	 * If transformer was able to find atom and transform it,
	 * we move the transformer and atom closer to each other.
	 */
	private void nextTurn() {
		Collections.shuffle(transformers);
		transformers.stream().forEach(t -> {
			turnForTransformer(t);
		});		
		totalTurns++;
	}

	/**
	 * Transformer tries to find and transform atom in the vicinity.
	 * First it searches for atoms with distance =  0,
	 * then with distance = 1, etc. up to searchDistance.
	 * When distance increases, probability of interaction decreases.
	 * 
	 * @param t
	 */
	private void turnForTransformer(Transformer t) {		
		for(int curDist=0; curDist<=searchDistance;curDist++) {
			double actionThreshold = curDist * actionDistancePenalty;
			if(Math.abs(rand.nextGaussian()) > actionThreshold) {
				ArrayList<Atom> atoms = getAtomsOnSurface(t.getCoords(), curDist);
				Collections.shuffle(atoms);
				for(Atom a: atoms) {
					if(t.tryTransform(a) != null) {
						moveAfterAction(t, a);
						return;
					}
				}
			}
		}
	}

	/**
	 * When atom was transformed:
	 *   - we move atom closer to transformer
	 *   - we also move transformer closer to atom, with probability 1/MASS_RATIO.
	 *   
	 * The purpose is on one hand, to have a mechanism to group transformers
	 * that are linked as Producer/Consumer close to each other.
	 * And on the other hand, to let such groups be stable.
	 * 
	 * @param trsf
	 * @param atom
	 */
	private boolean moveAfterAction(Transformer trsf, Atom atom) {
		int curDistance = Coordinates.calcDistance(atom.getCoords(), trsf.getCoords());
		if(curDistance == 0) {
			return false;
		}
		tryMoveAtomTowardsTo(atom, trsf.getCoords());
		curDistance = Coordinates.calcDistance(atom.getCoords(), trsf.getCoords());
		if(curDistance == 0) {
			// If atom new position is the same as trsf, no need to move the latter
			return false;
		}
		return tryMoveTransformerTowardsTo(trsf, atom.getCoords());
	}
	
	/**
	 * Scan immediate vicinity of a transformer,
	 * if:
	 *  - there is a free spot that is closer to the newCoord,
	 *  - if this free spot is not within immediate vicinity of another transformer
	 *  - if energy level difference and temperature allows, turned off
	 * relocate atom to this spot.
	 * 
	 * @param newCoords
	 * @param newCoord
	 * @return
	 */
	private boolean tryMoveTransformerTowardsTo(Transformer trsf, Coordinates newCoord) {
		double trsfrMoveChance = 0.;
		boolean pullWholeChain = false;
		int neighbCnt = trsf.getBonds().size();
		switch(neighbCnt ) {
		case 0:
			trsfrMoveChance =  1.0/massRatio;	// Move standalone trsf
			break;
		case 1:
			int linkedCnt = trsf.getLinkedCount(null);
			trsfrMoveChance = 1.0/(massRatioLinked * (linkedCnt +1));	// Pull the whole chain by the ending trsf
			pullWholeChain = true;
			break;
		default:
			trsfrMoveChance = 1.0/massRatio;	// Tear off trsf out of chain
		}
		if(rand.nextDouble() > trsfrMoveChance) {
			//log.info("=== after action, bad luck with trsfrMoveChance:{}",trsfrMoveChance );
			return false;
		}
		Coordinates curCoord = trsf.getCoords();
		double curLevel = getEnergyLevel(trsf, curCoord, pullWholeChain);
		ArrayList<Coordinates> vicinity = Coordinates.getVicinity(curCoord, 1);
		Collections.shuffle(vicinity);
		int origDistance = Coordinates.calcDistance(curCoord, newCoord);
		for(Coordinates tmpCoord : vicinity) {
			//Transformer var = getTransformerAt(tmpCoord);
			//int tmpDistance = Coordinates.calcDistance(tmpCoord, newCoord);
			if(Coordinates.calcDistance(tmpCoord, newCoord) < origDistance
				&& !isCoordForbidden(trsf, tmpCoord, pullWholeChain)){
				// Compare energy levels
				double tmpLevel = getEnergyLevel(trsf,tmpCoord, pullWholeChain);
				if(enoughEnergyForMove(tmpLevel - curLevel)) {
					if(pullWholeChain) {
						relocateTransformerWithNeigborsTo(trsf, tmpCoord);
						//log.info("=== after action. whole chain, trsfrMoveChance:{}",trsfrMoveChance );
					}else {
						relocateTransformerTo(trsf, tmpCoord);
						//log.info("=== after action, trsf, neighbCnt:"+neighbCnt+", curLevel:"+curLevel+", tmpLevel:"+tmpLevel);
					}
					return true;
				}
			}
		}
		return false;
		//log.debug("moved {} delta: {}",trsf.getName(),Coordinates.deltaToString(trsf.getCoords(), newCoord));
	}
	

	/**
	 * Simplified version of calculating energy level.
	 * It takes into account only increasing length of the existing bonds.
	 * It does not calculate possible benefits if in new location new bonds will be added.
	 * 
	 * @param pullWholeChain 
	 * 
	 * @param newCoords
	 * @return
	 */
	private double getEnergyLevel(Transformer trsf, Coordinates newCoord, boolean pullWholeChain) {
		if(pullWholeChain) {
			// When we calculate energy for a tentative location,
			// and it will be a case with pulling the chain, the old neighbor stays,
			// energy level remains zero
			return 0;
		}else {
			return trsf.getEnergyLevel(newCoord, seedCnt);
		}
	}
		
	
	/**
	 * Check:
	 * - new coord is already occupied with another trsf.
	 * - if there are transformers in 1 point vicinity of the coord which cannot accept more neighbors.
	 * - current transformer neighbor list will exceed the maxNeighborNumber, 
	 *   if moved to new location.
	 * 
	 * @param trsf
	 * @param coord
	 * @return
	 */
	private boolean isCoordForbidden(Transformer trsf, Coordinates coord, boolean withPull) {
		if(getTransformerAt(coord) != null) {
			return true;
		}
		// Get the number of neighbors that will be removed if trsf moves to new location
		ArrayList<Transformer> newNeighb = getTransformersWithin(coord, 1);	
		newNeighb.remove(trsf);
		int neighborsToRemoveCnt = 0;
		for(Bond bond:trsf.getBonds()){
			if(Coordinates.calcDistance(bond.getTransformer().getCoords(), coord) > maxNeighborDistance) {
				neighborsToRemoveCnt++;
			}
		}
		if(withPull) {
			// When we want to pull the whole chain, 
			// After relocation trsf will keep the existing neighbor
			neighborsToRemoveCnt = 0;
		}
		// Get the number of neighbors that will be added if trsf moves to new location
		int newNeigbCont = 0;
		for(Transformer tmpTrsf: newNeighb) {
			if(tmpTrsf.getBonds().size() >= maxNeighborNumber
				&& !tmpTrsf.hasNeighbor(trsf)) {
				// One of the transformer around the new location already has full list
				// and won't accept a new neighbor, stop further check
				return true;	
			}
			/* Not sure if it possible to have an existing neighbor to be preserved
			   after the move to a new location, when MAX_NEIGHBOR_DISTANCE=1,
			   but making check just in case
			   */
			if(!tmpTrsf.hasNeighbor(trsf)) {
				newNeigbCont++;
			}
		}
		if(newNeigbCont - neighborsToRemoveCnt > maxNeighborNumber ) {
			return true;	// after removing current neighbors and adding new ones, we exceed maxNeighborNumber
		}
		return false;
	}
	
	/**
	 * When  energy level for the next position is higher,
	 * the probability of move depends on the energy difference and temperature.
	 * Formula for the threshold:
	 * treshold = initPenalty + (delta/energyScale - temperature/MAX_TEMPERATURE)
	 * Random value is generated using Gaussian formula.
	 * 
	 * @param delta
	 */
	private long tryMoveCnt =0;
	private long allowedMoveCnt = 0;
	private boolean enoughEnergyForMove(double delta) {
		double initPenalty = 0.5;
		if (delta <= 0) {
			return true;
		}
		// linear
		//double treshold = initPenalty + (1.0 - initPenalty)*((delta-1)/maxEnergyBarier - temperature/MAX_TEMPERATURE);
		// Gaussian
		double treshold = initPenalty + (delta/energyScale - temperature/MAX_TEMPERATURE);
		double val = Math.abs(rand.nextGaussian());
		if(val > treshold) {
			//log.info("move allowed, delta:"+delta+", treshold:"+treshold+", val:"+val+", %:"+100.0*((float)++allowedMoveCnt/(float)++tryMoveCnt));
			return true;
		}
		//log.info("move stopped, delta:"+delta+", treshold:"+treshold+", val:"+val+", %:"+100.0*((float)allowedMoveCnt/(float)++tryMoveCnt));
		return false;
	}
	

	/**
	 * Scan immediate vicinity of an atom,
	 * if there is a free spot that is closer to the newCoord,
	 * relocate atom to this spot.
	 * 
	 * @param atom
	 * @param newCoord
	 * @return
	 */
	private boolean tryMoveAtomTowardsTo(Atom atom, Coordinates newCoord) {
		Coordinates oldCoord = atom.getCoords();
		ArrayList<Coordinates> vicinity = Coordinates.getVicinity(oldCoord, 1);
		Collections.shuffle(vicinity);
		int origDistance = Coordinates.calcDistance(oldCoord, newCoord);
		for(Coordinates curCoord : vicinity) {
			if(getAtomAt(curCoord) == null
				&& Coordinates.calcDistance(curCoord, newCoord) < origDistance){
				relocateAtomTo(atom, curCoord);
				return true;
			}
		}
		return false;
		//log.debug("moved {} delta: {}",trsf.getName(),Coordinates.deltaToString(trsf.getCoords(), newCoord));
	}
	

	
	private void relocateAtomTo(Atom atom, Coordinates newCoord) {
		int[] coordsArr = atom.getCoords().getCoords();
		atomSpace[coordsArr[0]][coordsArr[1]] = null;
		atom.setCoords(newCoord);
		coordsArr = newCoord.getCoords();
		atomSpace[coordsArr[0]][coordsArr[1]] = atom;
	}

	/**
	 * Create a 2-D image of transformers and save to a file
	 */
	private void saveShotForTransformers() {
	    try {
	    	// http://www.java2s.com/Code/Java/2D-Graphics-GUI/DrawanImageandsavetopng.htm
	        int width = 1000, titleHeight=20, height = width+titleHeight;
	        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
	        Graphics2D ig2 = bi.createGraphics();
	        ig2.setColor(Color.WHITE);
	        ig2.fillRect(0, 0, width, height);
	        Font font = new Font("TimesRoman", Font.PLAIN, 14);
	        ig2.setFont(font);
	        String message = "Transformers, T:"+temperature+
	        		", eScale:"+energyScale+
	        		", idleWait:"+idleWait+
	        		", massR:"+massRatio+
	        		", massRlnkd:"+massRatioLinked+
	        		", searchDst:" + searchDistance +
	        		", distPnlty:"+actionDistancePenalty +
	        		//", neibDst:" + maxNeighborDistance +
	        		//", neibNum:"+maxNeighborNumber +
	        		", atoms:"+ atomsNumber+", trsfrs:"+trsfrNumber+", seedCnt:"+seedCnt;
	        FontMetrics fontMetrics = ig2.getFontMetrics();
	        int stringWidth = fontMetrics.stringWidth(message);
	        int stringHeight = fontMetrics.getAscent();
	        ig2.setPaint(Color.black);
	        ig2.drawString(message, (width - stringWidth) / 2, 10 + stringHeight / 4);
	        Font fontForAtom = new Font("Consolas", Font.PLAIN, 8);
	        ig2.setFont(fontForAtom);
	        FontMetrics fontMetricsForAtom = ig2.getFontMetrics();
	        int letterWidth = fontMetricsForAtom.stringWidth("A");
	        int letterHeight = fontMetricsForAtom.getAscent();
	        double delta = width /SPACE_SIZE;
	        int x1, y1, x2, y2;
			int ox,oy,ow,oh;
			for(int i=0;i<SPACE_SIZE;i++) {
				for(int j=0;j<SPACE_SIZE;j++) {
					Color color = null;
					Transformer curTrsf = trsfSpace[i][j];
					if (curTrsf != null) { 
						ow=7;
						oh=7;
						color = curTrsf.getColor();
					}else {
						ow=2;
						oh=2;
						color = Color.GRAY;
					}
					ox = (int)(delta * i + (delta - ow)/2);
					oy = titleHeight + (int)(delta * j + (delta - oh)/2);
					ig2.setColor(color);
					ig2.fillOval(ox, oy, ow, oh);
					if (curTrsf != null) { 
						// Draw vertexes to neighbors
						for(Bond bond: curTrsf.getBonds()) {
							int[] neighbCoord = bond.getTransformer().getCoords().getCoords();
							x1 = (int)(delta * i + delta/2);
							y1 =  titleHeight + (int)(delta * j + delta/2);
							x2 = (int)(delta * neighbCoord[0] + delta/2);
							y2 =  titleHeight + (int)(delta * neighbCoord[1] + delta/2);
							ig2.setColor(Color.WHITE);
							ig2.drawLine(x1, y1, x2, y2);
						}
						if(curTrsf.getActionCnt()>0) {
							// If action occurred, draw an edge
							ig2.setColor(Color.BLACK);
							ig2.drawOval(ox, oy, ow, oh);
						}
					}
					if(showAtoms && atomSpace != null && atomSpace[i][j] != null) { 
						/* Draw atom type
						ig2.setPaint(Color.GRAY);
						ox = (int)(delta * i + (delta - letterWidth)/2);
						oy = titleHeight + (int)(delta * j + 5);
				        ig2.drawString(atomSpace[i][j].getType().name(), ox, oy);
				        */
						/* Draw a dot for an atom */
						ow=3;
						oh=3;
						color = Color.BLACK;
						ox = (int)(delta * i + (delta - ow)/2);
						oy = titleHeight + (int)(delta * j + (delta - oh)/2);
						ig2.setColor(color);
						ig2.fillOval(ox, oy, ow, oh);						
					}
				}
			}
			++fileCntTransformers;
	        String fileName = fileDir+"transformers_"+String.format("%05d",fileCntTransformers);
	        ImageIO.write(bi, "PNG", new File(fileName+".png"));
	        //ImageIO.write(bi, "JPEG", new File("c:\\yourImageName.JPG"));	        
	      	} catch (IOException ie) {
	      		ie.printStackTrace();
	      	}	
    		log.info(" === Saving transformes shot, seedCnt:{}, fileCntTransformers:{}", seedCnt, fileCntTransformers);
	    }

	/**
	 * Imitates random motion of a transformer
	 *  Gather the immediate vicinity, i.e. distance = 1
	 *  For each location, randomly:
	 *     if location not occupied with another transformer 
	 *     and not repulsed by other trsf
	 *     try to move to it.
	 *       if move allowed by energy level and temperature, return true.
	 *       if move not allowed, return false, without trying other locations
	 *     
	 * @param trsf
	 * @return
	 */
	private boolean moveTransformerRandomly(Transformer trsf) {
		double trsfrMoveChance = 0.;
		boolean pullWholeChain = false;
		int neighbCnt = trsf.getBonds().size();
		switch(neighbCnt ) {
		case 0:
			trsfrMoveChance =  (double)trsf.getIdleCnt()/(idleWait*massRatio);	// Move standalone trsf
			break;
		case 1:
			int linkedCnt = trsf.getLinkedCount(null);
			trsfrMoveChance = (double)trsf.getIdleCnt()/(idleWait*massRatioLinked * (linkedCnt+1));	// Pull the whole chain by the ending trsf
			pullWholeChain = true;
			break;
		default:
			trsfrMoveChance = (double)trsf.getIdleCnt()/(idleWait*massRatio);	// Tear off trsf out of chain
		}
		if(rand.nextDouble() > trsfrMoveChance) {
			return false;
		}
		//log.info("=== move trsf randomly with moveChance:{}, pullWholeChain:{}",trsfrMoveChance,pullWholeChain);
		double curLevel = getEnergyLevel(trsf, trsf.getCoords(),pullWholeChain);
		ArrayList<Coordinates> vicinity = Coordinates.getVicinity(trsf.getCoords(), 1);
		Collections.shuffle(vicinity);
		for(Coordinates tmpCoord : vicinity) {
			if(getTransformerAt(tmpCoord) == null
				&& !isCoordForbidden(trsf, tmpCoord,pullWholeChain)){
				// Compare energy levels
				double tmpLevel = getEnergyLevel(trsf,tmpCoord,pullWholeChain);
				if(enoughEnergyForMove(tmpLevel - curLevel)) {
					if(pullWholeChain) {
						relocateTransformerWithNeigborsTo(trsf, tmpCoord);
						//log.info("=== random. whole chain, trsfrMoveChance:{}",trsfrMoveChance );
					}else {
						relocateTransformerTo(trsf, tmpCoord);
						//log.info("=== random, trsf, neighbCnt:"+neighbCnt+", curLevel:"+curLevel+", tmpLevel:"+tmpLevel);
					}
					return true;
				}
				/* Once free spot found, only one attempt is allowed, 
				 * do not try others from vicinity
				 */
				//log.info("=== random no luck");
				return false;
			}	
		}
		return false;
	}
	

	/**
	 * Get list of atoms with coords not further than given 
	 * distance from the given center.
	 * 
	 * @param center
	 * @param distance
	 * @return
	 */
	public ArrayList<Atom> getAtomsInVicinity(Coordinates center, int distance){
		ArrayList<Atom> result = new ArrayList<Atom>();
		ArrayList<Coordinates> vicinity = Coordinates.getVicinity(center, distance);
		vicinity.stream().forEach(coord -> {
			Atom atom = getAtomAt(coord);
			if(atom != null) {
				if(distance == 0) {
					log.info("found atom at zero distance from: "+ center);
				}
				result.add(atom);
			}
		});
		return result;
	}		


	/**
	 * Get list of atoms with coords on the given 
	 * distance from the given center.
	 * 
	 * @param center
	 * @param distance
	 * @return
	 */
	public ArrayList<Atom> getAtomsOnSurface(Coordinates center, int distance){
		ArrayList<Atom> result = new ArrayList<Atom>();
		ArrayList<Coordinates> vicinity = Coordinates.getSurface(center, distance);
		vicinity.stream().forEach(coord -> {
			Atom atom = getAtomAt(coord);
			if(atom != null) {
				result.add(atom);
			}
		});
		return result;
	}		

	public ArrayList<Transformer> getTransformersWithin(Coordinates center, int distance){
		ArrayList<Transformer> result = new ArrayList<Transformer>();
		ArrayList<Coordinates> vicinity = Coordinates.getVicinity(center, distance);
		vicinity.stream().forEach(coord -> {
			Transformer trsf = getTransformerAt(coord);
			if(trsf != null) {
				result.add(trsf);
			}
		});
		return result;
	}
	
	private void printTrsfSpaceTypes() {
		StringBuilder sbTotal = new StringBuilder("\n=== Transformers space === \n");
		sbTotal.append("   .........1.........2.........3.........4.........5.........6.........7.........8.........9.........0\n");
		for(int i=0;i<SPACE_SIZE;i++) {
			StringBuilder sb = new StringBuilder(String.format("%3d",i));
			for(int j=0;j<SPACE_SIZE;j++) {
				if (trsfSpace[i][j] != null) {
					sb.append(trsfSpace[i][j].encodeTransformerType());
				}else {
					sb.append(".");
				}
			}
			sbTotal.append(sb).append("\n");
		}
		sbTotal.append("   .........1.........2.........3.........4.........5.........6.........7.........8.........9.........0");
		log.debug(sbTotal.toString());
	}

	private void printTrsfSpaceNames() {
		StringBuilder sbTotal = new StringBuilder("\n=== Transformers space === \n");
		sbTotal.append("   .........1.........2.........3.........4.........5.........6.........7.........8.........9.........0\n");
		for(int j=0;j<SPACE_SIZE;j++) {
			StringBuilder sb = new StringBuilder(String.format("%3d",j));
			for(int i=0;i<SPACE_SIZE;i++) {
				if (trsfSpace[i][j] != null) {
					String name = trsfSpace[i][j].getName();
					sb.append(name.substring(name.length()-4)).append(".");
				}else {
					sb.append(".....");
				}
			}
			sbTotal.append(sb).append("\n");
		}
		sbTotal.append("   .........1.........2.........3.........4.........5.........6.........7.........8.........9.........0");
		log.debug(sbTotal.toString());
	}

	private void printAtomSpace() {
		StringBuilder sbTotal = new StringBuilder("\n=== Atoms space === \n");
		sbTotal.append("   .........1.........2.........3.........4.........5.........6.........7.........8.........9.........0\n");
		for(int j=0;j<SPACE_SIZE;j++) {
			StringBuilder sb = new StringBuilder(String.format("%3d",j));
			for(int i=0;i<SPACE_SIZE;i++) {
				if (atomSpace[i][j] != null) {
					sb.append(atomSpace[i][j].getType().name());
				}else {
					sb.append(".");
				}
			}
			sbTotal.append(sb).append("\n");
		}
		sbTotal.append("   .........1.........2.........3.........4.........5.........6.........7.........8.........9.........0");
		log.debug(sbTotal.toString());
	}
	
	public Atom getAtomAt(Coordinates location) {
		return atomSpace[location.getCoords()[0]][location.getCoords()[1]];
	}
		
	public Transformer getTransformerAt(Coordinates location) {
		return trsfSpace[location.getCoords()[0]][location.getCoords()[1]];
	}
	
	/**
	 * Relocate transformer and adjust neighborhood:
	 * - add new neighbors if they are close enough now
	 * - remove neighbors that became too far.
	 * Note that we need to adjust neighborhood on both sides of relationship.
	 * 
	 * @param trsf
	 * @param newCoord
	 */
	private void relocateTransformerTo(Transformer trsf, Coordinates newCoord) {
		//log.debug("moved {} delta: {}",trsf.getName(),Coordinates.deltaToString(trsf.getCoords(), newCoord));
		// Move away from current neighbors:
		// check if trsf needs to be removed from their neighborhood
		ArrayList<Bond> bondsToRemove = new ArrayList<Bond>();
		for(Bond bond:trsf.getBonds()){
			if(Coordinates.calcDistance(bond.getTransformer().getCoords(), newCoord) > maxNeighborDistance) {
				boolean removed = bond.getTransformer().removeNeighbor(trsf); 
				bondsToRemove.add(bond);
			}
		}
		trsf.getBonds().removeAll(bondsToRemove);
		// Move to new position, adding neighbors if we're close to it, and there are free slots
		ArrayList<Transformer> possibleNeighbors = this.getTransformersWithin(newCoord, 1);
		for(Transformer pNeighbor:possibleNeighbors){
				// Some of these check are redundant. The newCoord supposed to be already verified by isCoordForbiddent method.
				if(!trsf.hasNeighbor(pNeighbor)
				&& !pNeighbor.hasNeighbor(trsf)
				&& trsf != pNeighbor
				&& trsf.getBonds().size() < maxNeighborNumber
				&& pNeighbor.getBonds().size() < maxNeighborNumber
				&& Coordinates.calcDistance(pNeighbor.getCoords(), newCoord) <= maxNeighborDistance) {
				pNeighbor.addNeighbor(trsf, seedCnt);
				trsf.addNeighbor(pNeighbor, seedCnt);
			}
		}	
		int[] coords = trsf.getCoords().getCoords();
		trsfSpace[coords[0]][coords[1]] = null;
		trsf.setCoords(newCoord);
		coords = trsf.getCoords().getCoords();
		trsfSpace[coords[0]][coords[1]] = trsf;
		
	}
			
	/**
	 * Relocate transformer and pull neighbors with it.
	 * Allowed only when transformer resides at the end of the chain.
	 * 
	 * @param trsf
	 * @param newCoord
	 */
	private void relocateTransformerWithNeigborsTo(Transformer trsf, Coordinates newCoord) {
		boolean pullWholeChain = false;
		if(trsf.getBonds().size() == 1) {
			pullWholeChain = true;
		}
		// Tear off neighbors
		ArrayList<Bond> bondsToRemove = new ArrayList<Bond>();
		for(Bond bond:trsf.getBonds()){
			if(Coordinates.calcDistance(bond.getTransformer().getCoords(), newCoord) > maxNeighborDistance) {
				boolean removed = bond.getTransformer().removeNeighbor(trsf); 
				bondsToRemove.add(bond);
			}
		}
		trsf.getBonds().removeAll(bondsToRemove);
		// Move trsf to new position, 
		Coordinates origCoords = trsf.getCoords();
		int[] spaceCoords = origCoords.getCoords();
		trsfSpace[spaceCoords[0]][spaceCoords[1]] = null;
		trsf.setCoords(newCoord);
		spaceCoords = trsf.getCoords().getCoords();
		trsfSpace[spaceCoords[0]][spaceCoords[1]] = trsf;
		if(pullWholeChain) {
			// at this moment neighborsToRemove.get(0) becomes an end of the chain,
			// so repeat the pull recursively
			relocateTransformerWithNeigborsTo(bondsToRemove.get(0).getTransformer(), origCoords);
		}
		// adding neighbors at new position
		// old neighbor supposed to be restored during this step
		ArrayList<Transformer> possibleNeighbors = this.getTransformersWithin(newCoord, 1);
		for(Transformer pNeighbor:possibleNeighbors){
				if(!trsf.hasNeighbor(pNeighbor)
				&& !pNeighbor.hasNeighbor(trsf)
				&& trsf != pNeighbor
				&& trsf.getBonds().size() < maxNeighborNumber
				&& pNeighbor.getBonds().size() < maxNeighborNumber) {
				pNeighbor.addNeighbor(trsf, seedCnt);
				trsf.addNeighbor(pNeighbor, seedCnt);
			}
		}	
		
	}
	
	public void seedTransformers() {
		trsfSpace = new Transformer[SPACE_SIZE][SPACE_SIZE];
		transformers.clear();
		for(int i=0;i<trsfrNumber;i++) {
			while(true) {
				Transformer trsf = createRandomTransformer();
				if(getTransformerAt(trsf.getCoords()) != null) {
					continue;
				}
				transformers.add(trsf);
				int[] coord = trsf.getCoords().getCoords();
				trsfSpace[coord[0]][coord[1]] = trsf;
				log.trace("seeding transformers, i="+i+", added "+trsf+" at "+coord[0]+","+coord[1]);
				break;
			}
		}		
		log.debug("seeding transformers,  done");
	}
	
	private void seedAtoms() {
		if(seedCnt == 0) {
			// Initial full seed
			atomSpace =  new Atom[SPACE_SIZE][SPACE_SIZE];
			atoms.clear();
			for(int i=0;i<atomsNumber;i++) {
				Atom atom = createRandomAtom();
				atoms.add(atom);
				int[] coord = atom.getCoords().getCoords();
				atomSpace[coord[0]][coord[1]] = atom;
				log.trace("Initial seed of atoms, i="+i+", added "+ atom+" at "+coord[0]+","+coord[1]);
			}
		}else {
			// Partial reseed, remove random portion
			int curAtomsSize = atomsNumber;
			int reseedNumber = (int)(atomsNumber + reseedPct /100.0);
			for(int i=0;i<reseedNumber;i++) {
				int rmvIdx = rand.nextInt(curAtomsSize);
				Atom rmvAtom = atoms.get(rmvIdx);
				int[] clrCoord = rmvAtom.getCoords().getCoords();
				atomSpace[clrCoord[0]][clrCoord[1]] = null;
				atoms.remove(rmvIdx);
				curAtomsSize--;
				log.trace("Partial seed of atoms, i="+i+", removed "+ rmvAtom+" at "+clrCoord[0]+","+clrCoord[1]);
			}
			// Partial reseed, add random portion
			for(int i=0;i<reseedNumber;i++) {
				while(true) {
					Atom atom = createRandomAtom();
					if(getAtomAt(atom.getCoords()) != null) {
						// place is occupied, try one more time
						continue;
					}
					int[] coord = atom.getCoords().getCoords();
					atomSpace[coord[0]][coord[1]] = atom;
					atoms.add(atom);
					log.trace("Partial seed of atoms, i="+i+", added "+ atom+" at "+coord[0]+","+coord[1]);
					break;
				}
			}
		}
		seedCnt++;
		//log.info("seeding atoms done, seedCnt:{}",seedCnt);
	}

	public Atom createRandomAtom() {
		int typeIdx = rand.nextInt(atomTypeNumber);
		while(true) {
			Coordinates coord = Coordinates.createRandom();
			if(getAtomAt(coord) == null) {
				return new Atom(coord, AtomTypeEnum.values()[typeIdx]);
			}
		}
	}

	/**
	 * Create random combination of input/outputType.
	 * Do not allow same type for input/output.
	 */
	public Transformer createRandomTransformer() {
		int typeIdx1, typeIdx2;
		typeIdx1 = rand.nextInt(atomTypeNumber);
		while(true) {
			typeIdx2 = rand.nextInt(atomTypeNumber);
			if (typeIdx2 != typeIdx1) {
				Coordinates coord = Coordinates.createRandom();
				if(getTransformerAt(coord) == null) {
					return new Transformer(coord, AtomTypeEnum.values()[typeIdx1],AtomTypeEnum.values()[typeIdx2]);
				}
			}
		}
	}

	private void initRunSet() {
		//history.clear();		
	}
	/**
	 * Verify that we did not place multiple transformers into one place.
	 */
	private void verifyTrsfPositions() {
		for(int i=0;i<transformers.size();i++) {
			int[] coord1 = transformers.get(i).getCoords().getCoords();
			for(int j=i+1;j<transformers.size();j++) {
				int[] coord2 = transformers.get(j).getCoords().getCoords();
				boolean sameCoords = true;
				for(int k=0;k<SPACE_DIM;k++) {
					if((coord1[k] != coord2[k])) {
						sameCoords = false;
						break;
					}
				}
				if(sameCoords) {
					log.error("===== Same coords: "+transformers.get(j).getCoords().toString()
							+", for: trsf["+i+"]: "+transformers.get(i).getName()
							+", for: trsf["+j+"]: "+transformers.get(j).getName());
				}
			}
		}
		
	}

	/**
	 * Create a 2-D image of atoms and save to a file
	 */
	/*
	private void saveShotForAtoms() {
	    try {
	        int width = 1000, titleHeight=20, height = width+titleHeight;
	        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
	        Graphics2D ig2 = bi.createGraphics();
	        ig2.setColor(Color.WHITE);
	        ig2.fillRect(0, 0, width, height);
	        Font font = new Font("TimesRoman", Font.BOLD, 18);
	        ig2.setFont(font);
	        String message = "Atoms, seedCnt:"+seedCnt;
	        FontMetrics fontMetrics = ig2.getFontMetrics();
	        int stringWidth = fontMetrics.stringWidth(message);
	        int stringHeight = fontMetrics.getAscent();
	        ig2.setPaint(Color.black);
	        ig2.drawString(message, (width - stringWidth) / 2, 10 + stringHeight / 4);
	        double delta = width /SPACE_SIZE;
			for(int i=0;i<SPACE_SIZE;i++) {
				for(int j=0;j<SPACE_SIZE;j++) {
					int ox,oy,ow,oh;
					Color color = null;
					if (atomSpace[i][j] != null) {
						ow=8;
						oh=8;
						color = atomSpace[i][j].getColor();
					}else {
						ow=2;
						oh=2;
						color = Color.GRAY;
					}
					ox = (int)(delta * i + (delta - ow)/2);
					oy = titleHeight + (int)(delta * j + (delta - oh)/2);
					ig2.setColor(color);
					ig2.fillOval(ox, oy, ow, oh);
					//log.debug("fillOval: ox="+ox+", oy="+oy+", ow="+ow+", oh="+oh+", color+"+color);
				}
			}
			//++fileCntAtoms;
	        //String fileName = "d:\\sergey\\transformers\\shots\\atoms_"+String.format("%05d",fileCntAtoms);
	        ImageIO.write(bi, "PNG", new File(fileName+".png"));
	        //ImageIO.write(bi, "JPEG", new File("c:\\yourImageName.JPG"));	        
	      	} catch (IOException ie) {
	      		ie.printStackTrace();
	      	}	
	    	log.info(" === Saving atoms shot, seedCnt:{}, fileCntAtom:{}", seedCnt, fileCntAtoms);
	    }
*/
	public int getSearchDistance() {
		return searchDistance;
	}

	public void setSearchDistance(int searchDistance) {
		this.searchDistance = searchDistance;
	}

	public int getMaxNeighborDistance() {
		return maxNeighborDistance;
	}

	public void setMaxNeighborDistance(int maxNeighborDistance) {
		this.maxNeighborDistance = maxNeighborDistance;
	}

	public int getTurnsPerSeed() {
		return turnsPerSeed;
	}

	public void setTurnsPerSeed(int turnsPerSeed) {
		this.turnsPerSeed = turnsPerSeed;
	}

	public int getAtomsNumber() {
		return atomsNumber;
	}

	public void setAtomsNumber(int atomsNumber) {
		this.atomsNumber = atomsNumber;
	}

	public int getTrsfrNumber() {
		return trsfrNumber;
	}

	public void setTrsfrNumber(int trsfrNumber) {
		this.trsfrNumber = trsfrNumber;
	}

	public int getMassRatio() {
		return massRatio;
	}

	public void setMassRatio(int massRatio) {
		this.massRatio = massRatio;
	}

	public double getTrsfrMoveChance() {
		return trsfrMoveChance;
	}

	public void setTrsfrMoveChance(double trsfrMoveChance) {
		this.trsfrMoveChance = trsfrMoveChance;
	}

	public int getTemperature() {
		return temperature;
	}

	public void setTemperature(int temperature) {
		this.temperature = temperature;
	}

	public int getIdleWait() {
		return idleWait;
	}

	public void setIdleWait(int idleWait) {
		this.idleWait = idleWait;
	}

	public int getMaxFilesCnt() {
		return maxFilesCnt;
	}

	public void setMaxFilesCnt(int maxFilesCnt) {
		this.maxFilesCnt = maxFilesCnt;
	}

	public String getFileDir() {
		return fileDir;
	}

	public void setFileDir(String fileDir) {
		this.fileDir = fileDir;
	}

	public ArrayList<Atom> getAtoms() {
		return atoms;
	}

	public void setAtoms(ArrayList<Atom> atoms) {
		this.atoms = atoms;
	}

	public ArrayList<Transformer> getTransformers() {
		return transformers;
	}

	public void setTransformers(ArrayList<Transformer> transformers) {
		this.transformers = transformers;
	}

	public int getTurnCnt() {
		return turnCnt;
	}

	public void setTurnCnt(int turnCnt) {
		this.turnCnt = turnCnt;
	}

	public boolean isPaused() {
		return isPaused;
	}

	public void setPaused(boolean isPaused) {
		this.isPaused = isPaused;
	}

	public long getSeedCnt() {
		return seedCnt;
	}

	public void setSeedCnt(long seedCnt) {
		this.seedCnt = seedCnt;
	}

	public int getFileCntTransformers() {
		return fileCntTransformers;
	}

	public void setFileCntTransformers(int fileCntTransformers) {
		this.fileCntTransformers = fileCntTransformers;
	}

	public int getSaveShotPeriod() {
		return saveShotPeriod;
	}

	public void setSaveShotPeriod(int saveShotPeriod) {
		this.saveShotPeriod = saveShotPeriod;
	}

	public long getTotalTurns() {
		return totalTurns;
	}

	public void setTotalTurns(long totalTurns) {
		this.totalTurns = totalTurns;
	}

	public int getSaveSnapShotPeriod() {
		return saveSnapShotPeriod;
	}

	public void setSaveSnapShotPeriod(int saveSnapShotPeriod) {
		this.saveSnapShotPeriod = saveSnapShotPeriod;
	}

	public boolean isShowAtoms() {
		return showAtoms;
	}

	public void setShowAtoms(boolean showAtoms) {
		this.showAtoms = showAtoms;
	}

	public double getEnergyScale() {
		return energyScale;
	}

	public void setEnergyScale(double energyScale) {
		this.energyScale = energyScale;
	}

	public double getActionDistancePenalty() {
		return actionDistancePenalty;
	}

	public void setActionDistancePenalty(double actionDistancePenalty) {
		this.actionDistancePenalty = actionDistancePenalty;
	}

	public int getMassRatioLinked() {
		return massRatioLinked;
	}

	public void setMassRatioLinked(int massRatioLinked) {
		this.massRatioLinked = massRatioLinked;
	}
	
}

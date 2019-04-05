package com.compmodel.sim.trsfr.core;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Sergey Sherstyuk
 *
 */

public class Transformer implements Serializable {
	private static final long serialVersionUID = -6590101303799725788L;
	private static final Logger log = LoggerFactory.getLogger(Transformer.class);
	private static final Color COLOR_AB = new Color(255,0,0); 			// Red
	private static final Color COLOR_AC = new Color(250,0,255); 		// Ryzhij
	private static final Color COLOR_AD = new Color(198,130,109); 		// Purple
	private static final Color COLOR_AE = Color.BLACK; // not used
	private static final Color COLOR_AF = Color.BLACK; // not used
	private static final Color COLOR_BA = new Color(156,244,243); 		// Light cyan
	private static final Color COLOR_BC = new Color(117,209,207); 		// Blue
	private static final Color COLOR_BD = new Color(0,0,153); 			// Green-blue
	private static final Color COLOR_BE = Color.BLACK; // not used
	private static final Color COLOR_BF = Color.BLACK; // not used
	private static final Color COLOR_CA = new Color(49,137,80); 		// Very dark green
	private static final Color COLOR_CB = new Color(183,209,117); 		// Green
	private static final Color COLOR_CD = new Color(58,163,254); 		// Dark green
	private static final Color COLOR_CE = Color.BLACK; // not used
	private static final Color COLOR_CF = Color.BLACK; // not used
	private static final Color COLOR_DA = new Color(244,236,124); 		// Light yellow
	private static final Color COLOR_DB = new Color(129,105,244); 		// Violet
	private static final Color COLOR_DC = new Color(144,58,163); 		// Very dark violet
	private static final Color COLOR_DE = Color.BLACK; // not used
	private static final Color COLOR_DF = Color.BLACK; // not used
	private static final Color COLOR_EA = Color.BLACK; // not used
	private static final Color COLOR_EB = Color.BLACK; // not used
	private static final Color COLOR_EC = Color.BLACK; // not used
	private static final Color COLOR_ED = Color.BLACK; // not used
	private static final Color COLOR_EF = Color.BLACK; // not used
	private static final Color COLOR_FA = Color.BLACK; // not used
	private static final Color COLOR_FB = Color.BLACK; // not used
	private static final Color COLOR_FC = Color.BLACK; // not used
	private static final Color COLOR_FD = Color.BLACK; // not used
	private static final Color COLOR_FE = Color.BLACK; // not used
	private static final long RANDOM_SEED = 6320981971L;
	public static Random rand = new Random(RANDOM_SEED);

	private UUID id;
	private Coordinates coords;
	private ArrayList<Bond> bonds;
	private int idleCnt;	// number of seeds with no actions, since last action
	private int actionCnt;	// number of actions since last seed
	private String name;

	private AtomTypeEnum inputType;
	private AtomTypeEnum outputType;

	public Transformer(Coordinates coords, AtomTypeEnum inputType, AtomTypeEnum outputType) {
		id = UUID.randomUUID();
		bonds = new ArrayList<Bond>();
		this.coords = coords;
		this.inputType = inputType;
		this.outputType = outputType;
		idleCnt = 0;
		actionCnt = 0;
		name = buildName();;
	}

	public String buildName() {
		return inputType.name()+outputType.name()+"_"+id.toString().substring(28);
	}

	/**
	 * Get number of all transformers linked as neighbors to each other.
	 * 
	 * @return
	 */
	public int getLinkedCount(ArrayList<Transformer> curLevelChain) {
		if(bonds.size() == 0) {
			return 0;	// Standalone transformer
		}
		if(curLevelChain == null) {
			// Call from the top level
			curLevelChain = new ArrayList<Transformer>();
			curLevelChain.add(this);	// to detect circular chain or link back to self
		}
		int linkedCount = 0;
		for(Bond curBond : bonds) {
			if(curLevelChain.contains(curBond.getNeighbor())) {
				continue;
			}
			linkedCount++;
			curLevelChain.add(curBond.getNeighbor()); 
			int linkedCountNeighb = curBond.getNeighbor().getLinkedCount(curLevelChain);
			linkedCount += linkedCountNeighb;
		}
		return linkedCount;	
	}

	/**
	 * Method that calculates list of all transformers that have linked to this via bonds.
	 * "This" is included
	 * Takes input curLevelLinks array and adds links from neighbors recursively.
	 * 
	 * @param curLevelLinks
	 * @return
	 */
	public ArrayList<Transformer> getLinked(ArrayList<Transformer> curLevelLinks) {
		if(bonds.size() == 0) {
			// Standalone transformer, unrealistic scenario, but handling just in case
			curLevelLinks = new ArrayList<Transformer>();
			curLevelLinks.add(this);	
			return curLevelLinks;	
		}
		if(curLevelLinks == null) {
			// Call from the top level
			curLevelLinks = new ArrayList<Transformer>();
		}
		curLevelLinks.add(this);	
		for(Bond curBond : bonds) {
			Transformer curBondTrsf = curBond.getNeighbor();
			if(!curLevelLinks.contains(curBondTrsf)) { // to detect circular chain or link back to self
				curLevelLinks = curBond.getNeighbor().getLinked(curLevelLinks); 
			}
		}
		return curLevelLinks;	
	}

	/**
	 * Try to find an atom within INIT_SEARCH_DISTANCE
	 * to transform it.
	 * If found:
	 *  - increase actionCnt - number of actions during current seed
	 *  - reset idleCnt - number of seeds when no actions occurred.
	 *  - return this atom
	 * otherwise:
	 *  - return null;
	 * @return
	 * 
	 * Not used
	 */
	private Atom findAndTransform(World world) {
		ArrayList<Atom> vicinity = world.getAtomsInVicinity(coords, World.SEARCH_DISTANCE);
		Collections.shuffle(vicinity);
		for(Atom atom : vicinity) {
			synchronized(atom) {
				if(atom.getType().equals(inputType)) {
					atom.setType(outputType);
					actionCnt++;
					idleCnt = 0;
					return atom;
				}
			}
		}
		return null;
	}

	/**
	 * If this transformer matches input atom type:
	 *  - transform (change atom type)
	 *  - increase actionCnt for the previous atom's actor
	 *  - increase actionCnt for bonds (this -> previousActor) and (previousActor -> this)
	 *  - set this as a new actor for the atom
	 *  - increase actionCnt for this transformer
	 *  - reset idleCnt for this transformer
	 *  
	 * @param input
	 * @return
	 */
	public Atom tryTransform(Atom input) {
		synchronized(input) {
			if(input.getType().equals(inputType)) {
				input.setType(outputType);
				updateBondsActionCnt(input.getActor());
				input.setActor(this);
				actionCnt++;
				idleCnt = 0;
				return input;
			}
		}
		return null;
	}
	
	/*
	 * Increase actionCnt for bonds (this -> previousActor) and (previousActor -> this)
	 */
	private synchronized void updateBondsActionCnt(Transformer prevActor) {
		for(Bond bond : bonds) {
			if(bond.getNeighbor() == prevActor) {
				long newActionCnt = bond.getActionCnt() + 1;
				bond.setActionCnt(newActionCnt);
				for(Bond prevActorBond : prevActor.getBonds()) {
					if(prevActorBond.getNeighbor() == this) {
						prevActorBond.setActionCnt(newActionCnt);
						log.trace("updateBondsActionCnt, set newActionCnt="+newActionCnt
							+" for bonds between this:"+getShortInfo()+" and prevActor:"+prevActor.getShortInfo()
							+", this.bond:"+bond+", prevActor.bond:"+prevActorBond);
						break;
					}
				}
				break;
			}
		}		
	}

	/**
	 * Calculate energy level for the transformer
	 * if it will be in curCoord.
	 * Energy level is a measure how far is transformer to its neighbors
	 * multiplied by the bond strength.
	 * When all distances are equal to 1, level is 0.
	 * 
	 * We do not take into account the fact that at newCoords there could be new bonds.
	 * @param curCoord
	 * @return
	 */
	public double getEnergyLevel(Coordinates newCoords, long curSeedCnt) {
		double level = 0.0;
		for(Bond bond:bonds){
			level += (Coordinates.calcDistance(bond.getNeighbor().getCoords(), newCoords) - 1)
					* bond.getStrength(curSeedCnt);
		}	
		return level;
	}
	
	public boolean hasNeighbor(Transformer trsf) {
		for(Bond bond :bonds) {
			if(bond.getNeighbor() == trsf) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Removes bond from this to neighborToRemove.
	 * if removeBothSides==true also remove bond from neighborToRemove to this.
	 * 
	 * @param neighborToRemove
	 * @return
	 */
	public synchronized boolean removeNeighbor(Transformer neighborToRemove, boolean removeBothSides, String comments) {
		Bond toRemoveFromThis = null, toRemoveFromNeighbor=null;
		for(Bond bond :bonds) {
			if(bond.getNeighbor() == neighborToRemove) {
				toRemoveFromThis = bond;
				break;
			}
		}
		if(toRemoveFromThis != null) {
			bonds.remove(toRemoveFromThis);
			log.debug("removeNeighbor, direct bond, from this:"+getShortInfo()+" removed neighbor:"+neighborToRemove.getShortInfo()
				+", bondFromThis:"+toRemoveFromThis+comments);
			if(removeBothSides) {
				for(Bond neighborBond:neighborToRemove.getBonds()) {
					if(neighborBond.getNeighbor() == this) {
						toRemoveFromNeighbor = neighborBond;
						break;
					}
				}
				if(toRemoveFromNeighbor != null) {
					neighborToRemove.getBonds().remove(toRemoveFromNeighbor);
					log.debug("removeNeighbor, backward bond, from neighborToRemove:"+neighborToRemove.getShortInfo()+" removed this:"+getShortInfo()
					+", bondFromNeighbor:"+toRemoveFromNeighbor+comments);
				}else {
					log.error("Neighbor transformer:"+neighborToRemove.getShortInfo()+" does not have this:"+getShortInfo()+" as a neighbor"+comments);					
				}
			}
			return true;
		}
		log.error("This transformer "+getShortInfo()+" does not have requested neighbor to remove:"+neighborToRemove.getShortInfo()+comments);
		return false;
	}

	public void resetStatus() {
		idleCnt = 0;
		actionCnt = 0;
		bonds.stream().forEach(b -> b.setActionCnt(1l));
	}


	/**
	 * Create pair of bonds with initial strength 1
	 * (this -> trsf) and (trsf -> this)
	 * @param trsf
	 */
	public synchronized void addNeighbor(Transformer trsf, long createdSeedCnt, String comments) {
		if(!hasNeighbor(trsf)) {
			if(bonds.size()>2) {
				log.error("seedCnt="+createdSeedCnt+"Too many bonds in trsfr-initiator, this:"+getShortInfo()+comments);
			}
			if(trsf.hasNeighbor(this)) {
				log.error("seedCnt="+createdSeedCnt+"Attaching trsf that has this as a neighbor already, trsf:"+getShortInfo()+comments);			
			}
			if(trsf.getBonds().size()>2) {
				log.error("seedCnt="+createdSeedCnt+"Too many bonds in trsfr-attachment, trsf:"+trsf.getShortInfo()+comments);
			}
			bonds.add(new Bond(trsf, 1l, createdSeedCnt));
			trsf.getBonds().add(new Bond(this, 1l, createdSeedCnt));
			log.debug("seedCnt="+createdSeedCnt+", created bonds between "+this+" and "+trsf + comments);
		}else {
			log.error("seedCnt="+createdSeedCnt+", attmept to add tsrf that is already a neighbor, this:"+getShortInfo()+", tsrf:"+trsf.getShortInfo()+comments);
		}			
	}

	public String getShortInfo() {
		String name =  this.toString(); 
		int atPos = name.indexOf("@");
		return inputType.toString()+outputType.toString()+name.substring(atPos)+ getCoords();
	}

	public String getShortInfoWithBonds() {
		StringBuilder sb = new StringBuilder();
		sb.append(getShortInfo()).append("(");
		for(Bond bond:getBonds()) {
			sb.append(bond.getNeighbor().getShortInfo()).append(":");
		}
		sb.append(")");
		return sb.toString();
	}

	public String getFullInfoWithBonds(long curSeedCnt) {
		StringBuilder sb = new StringBuilder();
		sb.append(getShortInfo())
			.append(", idleCnt=").append(idleCnt)
			.append(", actionCnt=").append(actionCnt)
			.append("{");
		for(Bond bond:getBonds()) {
			sb.append("[").append(bond.getNeighbor().getShortInfo())
				.append(",bondActionCnt=").append(bond.getActionCnt())
				.append(",bondCreatedCnt=").append(bond.getCreatedSeedCnt())
				.append(",bondStrength=").append(String.format("%5.2f",bond.getStrength(curSeedCnt)))
			.append("] ");
		}
		sb.append("}");
		return sb.toString();
	}

	public Coordinates getCoords() {
		return coords;
	}

	public void setCoords(Coordinates coords) {
		this.coords = coords;
	}

	public AtomTypeEnum getInputType() {
		return inputType;
	}

	public void setInputType(AtomTypeEnum inputType) {
		this.inputType = inputType;
	}

	public AtomTypeEnum getOutputType() {
		return outputType;
	}

	public void setOutputType(AtomTypeEnum outputType) {
		this.outputType = outputType;
	}

	public String encodeTransformerType() {
		switch(inputType) {
		case A:
			return encodeTransformerA();
		case B:
			return encodeTransformerB();
		case C:
			return encodeTransformerC();
		case D:
			return encodeTransformerD();
		case E:
			return encodeTransformerE();
		case F:
			return encodeTransformerF();
		default:
			return "*";
		}
	}

	private String encodeTransformerA() {
		switch(outputType) {
		case A:
			return "A";
		case B:
			return "B";
		case C:
			return "C";
		case D:
			return "D";
		case E:
			return "E";
		case F:
			return "F";
		default:
			return "*";
		}
	}
	private String encodeTransformerB() {
		switch(outputType) {
		case A:
			return "G";
		case B:
			return "H";
		case C:
			return "I";
		case D:
			return "J";
		case E:
			return "K";
		case F:
			return "L";
		default:
			return "*";
		}
	}
	private String encodeTransformerC() {
		switch(outputType) {
		case A:
			return "M";
		case B:
			return "N";
		case C:
			return "O";
		case D:
			return "P";
		case E:
			return "Q";
		case F:
			return "R";
		default:
			return "*";
		}
	}
	private String encodeTransformerD() {
		switch(outputType) {
		case A:
			return "S";
		case B:
			return "T";
		case C:
			return "U";
		case D:
			return "V";
		case E:
			return "W";
		case F:
			return "X";
		default:
			return "*";
		}
	}
	private String encodeTransformerE() {
		switch(outputType) {
		case A:
			return "Y";
		case B:
			return "Z";
		case C:
			return "Я";
		case D:
			return "Д";
		case E:
			return "Ф";
		case F:
			return "Ч";
		default:
			return "*";
		}
	}
	private String encodeTransformerF() {
		switch(outputType) {
		case A:
			return "Й";
		case B:
			return "Ж";
		case C:
			return "Ю";
		case D:
			return "Э";
		case E:
			return "Ш";
		case F:
			return "Ы";
		default:
			return "*";
		}
	}
	/**
	 * Color if to paint transformer as a single-color circle
	 * from the 8-bit RGB range
	 * @return
	 */	
	public Color getColor() {
		String code=encodeTransformerType();
		switch(code) {
		case "A":
			return Color.BLACK;	// AA never used
		case "B":
			return COLOR_AB; // AB ok
		case "C":
			return COLOR_AC; // AC ok
		case "D":
			return COLOR_AD; // AD ok
		case "E":
			return COLOR_AE;	// AE not used with 4 types
		case "F":
			return COLOR_AF; // AF not used with 4 types 
		case "G":
			return COLOR_BA; // BA ok
		case "H":
			return Color.BLACK;	// BB never used
		case "I":
			return COLOR_BC; // BC 
		case "J":
			return COLOR_BD; // BD 
		case "K":
			return COLOR_BE; // BE not used with 4 types
		case "L":
			return COLOR_BF; // BF not used with 4 types
		case "M":
			return COLOR_CA; // CA 
		case "N":
			return COLOR_CB; // CB 
		case "O":
			return Color.BLACK; // CC never used
		case "P":
			return COLOR_CD; // CD 
		case "Q":
			return COLOR_CE; // CE not used with 4 types
		case "R":
			return COLOR_CF; // CF not used with 4 types
		case "S":
			return COLOR_DA; // DA 
		case "T":
			return COLOR_DB; // DB 
		case "U":
			return COLOR_DC; // DC 
		case "V":
			return Color.BLACK;	// DD never used
		case "W":
			return COLOR_DE; // DE not used with 4 types
		case "X":
			return COLOR_DF; // DF not used with 4 types
		case "Y":
			return COLOR_EA; // EA not used with 4 types
		case "Z":
			return COLOR_EB; // EB not used with 4 types
		case "Я":
			return COLOR_EC; // EC not used with 4 types
		case "Д":
			return COLOR_ED; // ED not used with 4 types
		case "Ф":
			return Color.BLACK; // EE never used
		case "Ч":
			return COLOR_EF; // EF not used with 4 types
		case "Й":
			return COLOR_FA; // FA not used with 4 types
		case "Ж":
			return COLOR_FB; // FB not used with 4 types
		case "Ю":
			return COLOR_FC; // FC not used with 4 types
		case "Э":
			return COLOR_FD; // FD not used with 4 types
		case "Ш":
			return COLOR_FE; // FE not used with 4 types
		case "Ы":
			return Color.BLACK; // FF never used
		default:
			return Color.BLACK;
		}
	}

	public int getIdleCnt() {
		return idleCnt;
	}

	public void setIdleCnt(int idleCnt) {
		this.idleCnt = idleCnt;
	}

	public int getActionCnt() {
		return actionCnt;
	}

	public void setActionCnt(int actionCnt) {
		this.actionCnt = actionCnt;
	}

	public ArrayList<Bond> getBonds() {
		return bonds;
	}

	public void setBonds(ArrayList<Bond> bonds) {
		this.bonds = bonds;
	}

	public double getTotalBondStrength(long curSeedCnt) {
		double  total = 0.;
		for(Bond b:bonds) {
			total += b.getStrength(curSeedCnt);
		}
		return total;
	}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}



}

package com.compmodel.sim.trsfr.core;

import java.io.Serializable;
import java.time.LocalDateTime;

public class HistoryRecord  implements Serializable{
	private static final long serialVersionUID = 4966447623502849836L;
	private long id;	// sequential number of record
	private LocalDateTime whenOccurred;
	private long seedCnt = 0;
	private int turnCnt = 0;
	private String	actorName;
	private Coordinates	actorCoords;
	private AtomTypeEnum atomConsumedType;
	private AtomTypeEnum atomProducedType;
	private Coordinates	atomCoords;
	private boolean moved;
	private boolean transformed;
	
	public HistoryRecord(long id, long seedCnt, int turnCnt, Transformer actor, 
			Atom carrier, boolean moved) {
		this.id = id;
		whenOccurred = LocalDateTime.now();
		this.setSeedCnt(seedCnt);
		this.setTurnCnt(turnCnt);
		this.actorName = actor.getName();
		atomConsumedType = actor.getInputType();
		atomProducedType = actor.getOutputType();
		if(carrier != null) {
			transformed = true;
			atomCoords = new Coordinates(carrier.getCoords().getCoords());
		}else {
			transformed = false;
			atomCoords = null;
		}
		actorCoords = new Coordinates(actor.getCoords().getCoords());
		this.setMoved(moved);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("%06d",id)).append(",seed:").
			append(String.format("%06d",seedCnt)).append(",turn:").
			append(String.format("%02d",turnCnt)).append(",actor:").
			append(actorName).append(",at").
			append(actorCoords.toString()).append(",");
		if(transformed) {
			sb.append("act:").append(atomConsumedType.name()).
				append("->").append(atomProducedType.name()).
				append(",atom at").
				append(atomCoords.toString()).append(",");
		}else {
			sb.append(",idle,");
		}
		if(moved) {
			sb.append("moved");
		}else {
			sb.append("not moved");
		}
		
		return sb.toString();
		
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public LocalDateTime getWhenOccurred() {
		return whenOccurred;
	}
	public void setWhenOccurred(LocalDateTime whenOccurred) {
		this.whenOccurred = whenOccurred;
	}
	public long getSeedCnt() {
		return seedCnt;
	}

	public void setSeedCnt(long seedCnt) {
		this.seedCnt = seedCnt;
	}

	public int getTurnCnt() {
		return turnCnt;
	}

	public void setTurnCnt(int turnCnt) {
		this.turnCnt = turnCnt;
	}

	public String getActorName() {
		return actorName;
	}
	public void setActorName(String actorName) {
		this.actorName = actorName;
	}
	public Coordinates getActorCoords() {
		return actorCoords;
	}
	public void setActorCoords(Coordinates actorCoords) {
		this.actorCoords = actorCoords;
	}
	public AtomTypeEnum getAtomConsumedType() {
		return atomConsumedType;
	}
	public void setAtomConsumedType(AtomTypeEnum atomConsumedType) {
		this.atomConsumedType = atomConsumedType;
	}
	public AtomTypeEnum getAtomProducedType() {
		return atomProducedType;
	}
	public void setAtomProducedType(AtomTypeEnum atomProducedType) {
		this.atomProducedType = atomProducedType;
	}
	public Coordinates getAtomCoords() {
		return atomCoords;
	}
	public void setAtomCoords(Coordinates atomCoords) {
		this.atomCoords = atomCoords;
	}

	public boolean isMoved() {
		return moved;
	}

	public void setMoved(boolean moved) {
		this.moved = moved;
	}
	
}

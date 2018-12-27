package com.compmodel.sim.trsfr.core;

import java.awt.Color;
import java.io.Serializable;

public class Atom implements Serializable{
	private static final long serialVersionUID = -6664530532126869588L;
	private AtomTypeEnum type;
	private Coordinates coords;
	private Transformer actor;
	
	Atom(Coordinates coords, AtomTypeEnum type){
		this.coords = coords;
		this.type = type;
		setActor(null);
	}

	public void transform(AtomTypeEnum newType, Transformer actor) {
		this.type = newType;
		this.setActor(actor);		
	}

	public AtomTypeEnum getType() {
		return type;
	}

	public void setType(AtomTypeEnum type) {
		this.type = type;
	}

	public Coordinates getCoords() {
		return coords;
	}

	public void setCoords(Coordinates coords) {
		this.coords = coords;
	}

	public Color getColor() {
		return type.getColor();
	}

	public Transformer getActor() {
		return actor;
	}

	public void setActor(Transformer actor) {
		this.actor = actor;
	}

}

package com.compmodel.sim.trsfr.core;
import java.awt.Color;
import java.io.Serializable;
public enum AtomTypeEnum implements Serializable{
	A(Color.RED),
	B(Color.BLUE),
	C(Color.YELLOW),
	D(Color.GREEN),
	E(Color.MAGENTA),
	F(Color.GRAY),
	G(Color.BLACK);// H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z;
	
	private Color color;
	private AtomTypeEnum(Color color) {
		this.color = color;
	}
	public Color getColor() {
		return color;
	}
	public static int getIndex(AtomTypeEnum value) {
		for(int i=0;i<values().length;i++) {
			if(value == values()[i]) {
				return i;
			}
		}
		return 0;
	}
}

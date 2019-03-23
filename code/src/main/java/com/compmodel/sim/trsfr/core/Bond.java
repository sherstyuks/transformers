package com.compmodel.sim.trsfr.core;

import java.io.Serializable;

/**
 * Represents the unidirectional bond from transformer to its neighbor.
 * 
 * @author Sergey Sherstyuk
 *
 */
public class Bond implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2427048054204742250L;
	private Transformer neighbor;
	private long actionCnt;
	private long createdSeedCnt;
	
	public Bond(Transformer neighbor, long actionCnt, long createdSeedCnt) {
		this.neighbor = neighbor;
		this.actionCnt = actionCnt;
		this.createdSeedCnt = createdSeedCnt;
	}
	
	public double getStrength(long curSeedCnt) {
		long diff = curSeedCnt - createdSeedCnt;
		if (diff <= 0) {
			diff =1;
		}
		return 0.5*(double)actionCnt/diff;
	}
	
	public Transformer getNeighbor() {
		return neighbor;
	}
	public void setNeighbor(Transformer neighbor) {
		this.neighbor = neighbor;
	}
	public long getActionCnt() {
		return actionCnt;
	}
	public void setActionCnt(long actionCnt) {
		this.actionCnt = actionCnt;
	}

	public long getCreatedSeedCnt() {
		return createdSeedCnt;
	}

	public void setCreatedSeedCnt(long createdSeedCnt) {
		this.createdSeedCnt = createdSeedCnt;
	}
}

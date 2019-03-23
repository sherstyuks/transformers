package com.compmodel.sim.trsfr.core;

public class ChainStatsSummary {
	private boolean isCircular;
	private double avgStrength;
	private double maxStrength;
	private double minStrength;
	private double avgAge;
	private long maxAge;
	private long minAge;
	private long createdSeedCnt;
	private double matchTypePct;
	private String typeList;
	public boolean isCircular() {
		return isCircular;
	}
	public void setCircular(boolean isCircular) {
		this.isCircular = isCircular;
	}
	public double getAvgStrength() {
		return avgStrength;
	}
	public void setAvgStrength(double avgStrength) {
		this.avgStrength = avgStrength;
	}
	public double getMaxStrength() {
		return maxStrength;
	}
	public void setMaxStrength(double maxStrength) {
		this.maxStrength = maxStrength;
	}
	public double getMinStrength() {
		return minStrength;
	}
	public void setMinStrength(double minStrength) {
		this.minStrength = minStrength;
	}
	public double getAvgAge() {
		return avgAge;
	}
	public void setAvgAge(double avgAge) {
		this.avgAge = avgAge;
	}
	public long getMaxAge() {
		return maxAge;
	}
	public void setMaxAge(long maxAge) {
		this.maxAge = maxAge;
	}
	public long getMinAge() {
		return minAge;
	}
	public void setMinAge(long minAge) {
		this.minAge = minAge;
	}
	public long getCreatedSeedCnt() {
		return createdSeedCnt;
	}
	public void setCreatedSeedCnt(long createdSeedCnt) {
		this.createdSeedCnt = createdSeedCnt;
	}
	public String getTypeList() {
		return typeList;
	}
	public void setTypeList(String typeList) {
		this.typeList = typeList;
	}
	public double getMatchTypePct() {
		return matchTypePct;
	}
	public void setMatchTypePct(double matchTypePct) {
		this.matchTypePct = matchTypePct;
	}
}

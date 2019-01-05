package com.compmodel.sim.trsfr.core;

import java.io.Serializable;

public class WorldStatsSummary implements Serializable{
	private static final long serialVersionUID = -1326089825065394003L;
	private int count;
	private double avgLength;
	private int maxLength;
	private double avgStrength;
	private double maxStrength;
	private double minStrength;
	private double avgAge;
	private long maxAge;
	private long minAge;
	private int countNonCircular;
	private double avgLengthNonCircular;
	private int maxLengthNonCircular;
	private double avgStrengthNonCircular;
	private double maxStrengthNonCircular;
	private double minStrengthNonCircular;
	private double avgAgeNonCircular;
	private long maxAgeNonCircular;
	private long minAgeNonCircular;
	private long createdSeedCnt;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("seedCnt:").append(String.format("%8d",createdSeedCnt))
		.append(", chainsCnt:").append(String.format("%4d",count))
		.append(", avgLength:").append(String.format("%04.1f",avgLength))
		.append(", maxLength:").append(String.format("%4d",maxLength))
		.append(", avgStrength:").append(String.format("%05.2f",avgStrength))
		.append(", maxStrength:").append(String.format("%05.2f",maxStrength))
		.append(", minStrength:").append(String.format("%04.2f",minStrength))
		.append(", avgAge:").append(String.format("%08.0f",avgAge))
		.append(", maxAge:").append(String.format("%8d",maxAge))
		.append(", minAge:").append(String.format("%6d",minAge));
		return sb.toString();
	}
	
	public String toCsv() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("%d",createdSeedCnt))
		.append(",").append(String.format("%d",count))
		.append(",").append(String.format("%4.1f",avgLength))
		.append(",").append(String.format("%d",maxLength))
		.append(",").append(String.format("%5.2f",avgStrength))
		.append(",").append(String.format("%5.2f",maxStrength))
		.append(",").append(String.format("%4.2f",minStrength))
		.append(",").append(String.format("%8.0f",avgAge))
		.append(",").append(String.format("%d",maxAge))
		.append(",").append(String.format("%d",minAge))
		.append(",").append(String.format("%d",countNonCircular))
		.append(",").append(String.format("%4.1f",avgLengthNonCircular))
		.append(",").append(String.format("%d",maxLengthNonCircular))
		.append(",").append(String.format("%5.2f",avgStrengthNonCircular))
		.append(",").append(String.format("%5.2f",maxStrengthNonCircular))
		.append(",").append(String.format("%4.2f",minStrengthNonCircular))
		.append(",").append(String.format("%8.0f",avgAgeNonCircular))
		.append(",").append(String.format("%d",maxAgeNonCircular))
		.append(",").append(String.format("%d",minAgeNonCircular));
		return sb.toString();
	}
	
	public static String getCsvHeader() {
		StringBuilder sb = new StringBuilder();
		sb.append("createdSeedCnt")
		.append(",").append("count")
		.append(",").append("avgLength")
		.append(",").append("maxLength")
		.append(",").append("avgStrength")
		.append(",").append("maxStrength")
		.append(",").append("minStrength")
		.append(",").append("avgAge")
		.append(",").append("maxAge")
		.append(",").append("minAge")
		.append(",").append("countNonCircular")
		.append(",").append("avgLengthNonCircular")
		.append(",").append("maxLengthNonCircular")
		.append(",").append("avgStrengthNonCircular")
		.append(",").append("maxStrengthNonCircular")
		.append(",").append("minStrengthNonCircular")
		.append(",").append("avgAgeNonCircular")
		.append(",").append("maxAgeNonCircular")
		.append(",").append("minAgeNonCircular");
		return sb.toString();
	}

	public double getAvgLength() {
		return avgLength;
	}
	public void setAvgLength(double avgLength) {
		this.avgLength = avgLength;
	}
	public int getMaxLength() {
		return maxLength;
	}
	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
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
	public double getAvgStrengthNonCircular() {
		return avgStrengthNonCircular;
	}
	public void setAvgStrengthNonCircular(double avgStrengthNonCircular) {
		this.avgStrengthNonCircular = avgStrengthNonCircular;
	}
	public double getMaxStrengthNonCircular() {
		return maxStrengthNonCircular;
	}
	public void setMaxStrengthNonCircular(double maxStrengthNonCircular) {
		this.maxStrengthNonCircular = maxStrengthNonCircular;
	}
	public double getMinStrengthNonCircular() {
		return minStrengthNonCircular;
	}
	public void setMinStrengthNonCircular(double minStrengthNonCircular) {
		this.minStrengthNonCircular = minStrengthNonCircular;
	}
	public double getAvgAgeNonCircular() {
		return avgAgeNonCircular;
	}
	public void setAvgAgeNonCircular(double avgAgeNonCircular) {
		this.avgAgeNonCircular = avgAgeNonCircular;
	}
	public long getMaxAgeNonCircular() {
		return maxAgeNonCircular;
	}
	public void setMaxAgeNonCircular(long maxAgeNonCircular) {
		this.maxAgeNonCircular = maxAgeNonCircular;
	}
	public long getMinAgeNonCircular() {
		return minAgeNonCircular;
	}
	public void setMinAgeNonCircular(long minAgeNonCircular) {
		this.minAgeNonCircular = minAgeNonCircular;
	}
	public long getCreatedSeedCnt() {
		return createdSeedCnt;
	}
	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void setCreatedSeedCnt(long createdSeedCnt) {
		this.createdSeedCnt = createdSeedCnt;
	}

	public int getCountNonCircular() {
		return countNonCircular;
	}

	public void setCountNonCircular(int countNonCircular) {
		this.countNonCircular = countNonCircular;
	}

	public double getAvgLengthNonCircular() {
		return avgLengthNonCircular;
	}

	public void setAvgLengthNonCircular(double avgLengthNonCircular) {
		this.avgLengthNonCircular = avgLengthNonCircular;
	}

	public int getMaxLengthNonCircular() {
		return maxLengthNonCircular;
	}

	public void setMaxLengthNonCircular(int maxLengthNonCircular) {
		this.maxLengthNonCircular = maxLengthNonCircular;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}

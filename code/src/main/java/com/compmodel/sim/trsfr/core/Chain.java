package com.compmodel.sim.trsfr.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.LongSummaryStatistics;

public class Chain {
	private boolean isCircular;
	private ArrayList<Transformer> links;
	private ArrayList<Double> strengths;
	private ArrayList<Long> ages;
	
	public Chain() {
		isCircular = false;
		links = new ArrayList<Transformer>();
		strengths = new ArrayList<Double>();
		ages = new ArrayList<Long>();
	}
	
	public void addLink(Transformer trsf, double strength, long age) {
		links.add(trsf);
		strengths.add(strength);
		ages.add(age);	
	}

	public String getSummary() {
		DoubleSummaryStatistics statsS = strengths.stream().mapToDouble(Double::valueOf).summaryStatistics();
		LongSummaryStatistics statsA = ages.stream().mapToLong(Long::valueOf).summaryStatistics();
		StringBuilder sb = new StringBuilder();
		sb.append("Size:").append(String.format("%3d",links.size())).append(",")
		.append(isCircular)
		.append(", Strength:").append(String.format("%04.1f",statsS.getAverage())).append("(")
			.append(String.format("%05.1f",statsS.getMax())).append(",")
			.append(String.format("%03.1f",statsS.getMin()))
		.append("), Age:").append(String.format("%07.1f",statsA.getAverage())).append("(")
			.append(String.format("%6d",statsA.getMax())).append(",")
			.append(String.format("%6d",statsA.getMin())).append(")");
		return sb.toString();
	}
	
	public int getLength() {
		return links.size();
	}
	public boolean isCircular() {
		return isCircular;
	}
	public void setCircular(boolean isCircular) {
		this.isCircular = isCircular;
	}
	public ArrayList<Transformer> getLinks() {
		return links;
	}
	public void setLinks(ArrayList<Transformer> links) {
		this.links = links;
	}
	public ArrayList<Double> getStrengths() {
		return strengths;
	}
	public void setStrengths(ArrayList<Double> strengths) {
		this.strengths = strengths;
	}

	public ArrayList<Long> getAges() {
		return ages;
	}

	public void setAges(ArrayList<Long> ages) {
		this.ages = ages;
	}

	public int size() {
		return links.size();
	}



}

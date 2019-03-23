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
	
	public String getTrsfTypeList() {
		StringBuilder sb = new StringBuilder();
		for(int i =0;i<links.size();i++) {
			Transformer t = links.get(i);
			sb.append(t.getInputType().toString()+t.getOutputType().toString());
			if(i < links.size() -1) {
				sb.append(".");
			}
		}
		return sb.toString();
	}

	public double getMatchTypePct() {
		int matchCnt = 0;
		if(links.size() <=1) {
			return 0.;
		}
		AtomTypeEnum prevType = links.get(0).getOutputType();
		for(int i =1;i<links.size();i++) {
			Transformer t = links.get(i);
			if(t.getInputType() == prevType) {
				matchCnt++;
			}
			prevType = t.getOutputType();
		}
		return matchCnt*100.0/(links.size()-1);
	}

	public void addLink(Transformer trsf, double strength, long age) {
		links.add(trsf);
		strengths.add(strength);
		ages.add(age);	
	}

	public ChainStatsSummary getSummaryStats(long seedCnt) {
		DoubleSummaryStatistics statsS = strengths.stream().mapToDouble(Double::valueOf).summaryStatistics();
		LongSummaryStatistics statsA = ages.stream().mapToLong(Long::valueOf).summaryStatistics();
		ChainStatsSummary stats = new ChainStatsSummary();
		stats.setAvgAge(statsA.getAverage());
		stats.setMaxAge(statsA.getMax());
		stats.setMinAge(statsA.getMin());
		stats.setAvgStrength(statsS.getAverage());
		stats.setMaxStrength(statsS.getMax());
		stats.setMinStrength(statsS.getMin());
		stats.setCircular(isCircular);
		stats.setCreatedSeedCnt(seedCnt);
		stats.setMatchTypePct(getMatchTypePct());
		stats.setTypeList(getTrsfTypeList());
		return stats;
	}
	
	public String getSummaryStr(long seedCnt) {
		ChainStatsSummary stats = getSummaryStats(seedCnt);
		StringBuilder sb = new StringBuilder();
		sb.append("Size:").append(String.format("%3d",links.size())).append(",")
		.append(isCircular)
		.append(", Strength:").append(String.format("%04.1f",stats.getAvgStrength())).append("(")
			.append(String.format("%04.1f",stats.getMaxStrength())).append(",")
			.append(String.format("%03.1f",stats.getMinStrength()))
		.append("), Age:").append(String.format("%8d",(long)stats.getAvgAge())).append("(")
			.append(String.format("%8d",stats.getMaxAge())).append(",")
			.append(String.format("%8d",stats.getMinAge())).append(")")
		.append(". matchPct:").append(String.format("%04.1f",stats.getMatchTypePct()));
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

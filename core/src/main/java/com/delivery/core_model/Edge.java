package com.delivery.core.model;

public class Edge {
	private	int from;
	private	int to;
	private double length;
		
	public Edge(int from, int to, double length)
	{
		this.from = from;
		this.to = to;
		this.length = length;
	}
	
	public int getSourceNodeId() { return from; }
		
	public int getTargetNodeId() {return to; }
		
	public double getLength() {return length; }
}


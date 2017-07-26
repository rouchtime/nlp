package com.rouchtime.nlp.common;

import java.util.Set;

public class NewsSig extends News{
	private int[] hash;
	private Set<Integer> vector;
	public Set<Integer> getVector() {
		return vector;
	}

	public void setVector(Set<Integer> vector) {
		this.vector = vector;
	}

	public int[] getHash() {
		return hash;
	}

	public void setHash(int[] hash) {
		this.hash = hash;
	}
}

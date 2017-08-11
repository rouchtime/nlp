package com.rouchtime.nlp.common;

public class Term {
	@Override
	public String toString() {
		return word+"/"+nature;
	}
	private String word;
	private String nature;
	public Term(String word, String nature) {
		this.word = word;
		this.nature = nature;
	}
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public String getNature() {
		return nature;
	}
	public void setNature(String nature) {
		this.nature = nature;
	}
	
	
}

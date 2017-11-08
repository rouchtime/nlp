package com.rouchtime.nlp.keyword.similarity;

public interface WordSimiarity {
	public double calTowWordSimiarity(String w1,String w2);
	public boolean isExistWord(String word);
}

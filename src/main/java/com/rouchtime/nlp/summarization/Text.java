package com.rouchtime.nlp.summarization;

import java.util.List;
import java.util.Set;

import com.aliasi.classify.TfIdfClassifierTrainer;
import com.aliasi.symbol.MapSymbolTable;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.ObjectToSet;

public class Text {
    private List<Sentence> sentences;
    private String name;
    private Set<String> totalWords;
    private MapSymbolTable symbolTable;
    public MapSymbolTable getSymbolTable() {
		return symbolTable;
	}

	public void setSymbolTable(MapSymbolTable symbolTable) {
		this.symbolTable = symbolTable;
	}

	public Set<String> getTotalWords() {
		return totalWords;
	}

	public void setTotalWords(Set<String> totalWords) {
		this.totalWords = totalWords;
	}

	public Text(String name) {
        this.name = name;
    }

    public List<Sentence> getSentences() {
        return sentences;
    }

    public void setSentences(List<Sentence> sentences) {
        this.sentences = sentences;
    }

    @Override
    public String toString() {
        return sentences.toString();
    }

    public int numSentences() {
        return sentences.size();
    }

    public String getName() {
        return name;
    }
}

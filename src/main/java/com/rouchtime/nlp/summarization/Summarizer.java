package com.rouchtime.nlp.summarization;


import java.util.List;

public interface Summarizer {

    List<Sentence> summarize(Text text, Integer part);

}

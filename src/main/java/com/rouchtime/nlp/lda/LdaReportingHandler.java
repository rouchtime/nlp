package com.rouchtime.nlp.lda;

import com.aliasi.cluster.LatentDirichletAllocation;

import com.aliasi.corpus.ObjectHandler;

import com.aliasi.symbol.SymbolTable;

import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.Strings;
import com.rouchtime.nlp.keyword.extraction.AbstractKeyWordExtraction;

import java.util.List;

import org.apache.log4j.Logger;

public class LdaReportingHandler implements ObjectHandler<LatentDirichletAllocation.GibbsSample> {
	private Logger logger = Logger.getLogger(LdaReportingHandler.class);
	private final SymbolTable mSymbolTable;
	private final long mStartTime;

	LdaReportingHandler(SymbolTable symbolTable) {
		mSymbolTable = symbolTable;
		mStartTime = System.currentTimeMillis();
	}

	public void handle(LatentDirichletAllocation.GibbsSample sample) {
		logger.debug(String.format("Epoch=%3d   elapsed time=%s\n", sample.epoch(),
				Strings.msToString(System.currentTimeMillis() - mStartTime)));

		if ((sample.epoch() % 10) == 0) {
			double corpusLog2Prob = sample.corpusLog2Probability();
			logger.debug(String.format("\t\t\tlog2 p(corpus|phi,theta)=%f\t\t\ttoken cross-entropy rate=%f",
					corpusLog2Prob, (-corpusLog2Prob / sample.numTokens())));
		}
	}

	void fullReport(LatentDirichletAllocation.GibbsSample sample, int maxWordsPerTopic, int maxTopicsPerDoc,
			boolean reportTokens) {
		logger.debug("\nFull Report\n");

		int numTopics = sample.numTopics();
		int numWords = sample.numWords();
		int numDocs = sample.numDocuments();
		int numTokens = sample.numTokens();
		logger.debug("epoch=" + sample.epoch()+"\n");
		logger.debug("numDocs=" + numDocs+"\n");
		logger.debug("numTokens=" + numTokens+"\n");
		logger.debug("numWords=" + numWords+"\n");
		logger.debug("numTopics=" + numTopics+"\n");

		for (int topic = 0; topic < numTopics; ++topic) {
			int topicCount = sample.topicCount(topic);
			ObjectToCounterMap<Integer> counter = new ObjectToCounterMap<Integer>();
			for (int word = 0; word < numWords; ++word)
				counter.set(Integer.valueOf(word), sample.topicWordCount(topic, word));
			List<Integer> topWords = counter.keysOrderedByCountList();
			logger.debug("\nTOPIC " + topic + "  (total count=" + topicCount + ")\n");
			logger.debug("SYMBOL             WORD    COUNT   PROB          Z\n");
			logger.debug("--------------------------------------------------\n");
			for (int rank = 0; rank < maxWordsPerTopic && rank < topWords.size(); ++rank) {
				int wordId = topWords.get(rank);
				String word = mSymbolTable.idToSymbol(wordId);
				int wordCount = sample.wordCount(wordId);
				int topicWordCount = sample.topicWordCount(topic, wordId);
				double topicWordProb = sample.topicWordProb(topic, wordId);
				double z = binomialZ(topicWordCount, topicCount, wordCount, numTokens);
				logger.debug(String.format("%6d  %15s  %7d   %4.3f  %8.1f\n", wordId, word, topicWordCount, topicWordProb, z));
			}
		}

		for (int doc = 0; doc < numDocs; ++doc) {
			int docCount = 0;
			for (int topic = 0; topic < numTopics; ++topic)
				docCount += sample.documentTopicCount(doc, topic);
			ObjectToCounterMap<Integer> counter = new ObjectToCounterMap<Integer>();
			for (int topic = 0; topic < numTopics; ++topic)
				counter.set(Integer.valueOf(topic), sample.documentTopicCount(doc, topic));
			List<Integer> topTopics = counter.keysOrderedByCountList();
			logger.debug("\nDOC " + doc + "\n");
			logger.debug("TOPIC    COUNT    PROB\n");
			logger.debug("----------------------\n");
			for (int rank = 0; rank < topTopics.size() && rank < maxTopicsPerDoc; ++rank) {
				int topic = topTopics.get(rank);
				int docTopicCount = sample.documentTopicCount(doc, topic);
				double docTopicPrior = sample.documentTopicPrior();
				double docTopicProb = (sample.documentTopicCount(doc, topic) + docTopicPrior)
						/ (docCount + numTopics * docTopicPrior);
				logger.debug(String.format("%5d  %7d   %4.3f\n", topic, docTopicCount, docTopicProb));
			}
			logger.debug("\n");
			if (!reportTokens)
				continue;
			int numDocTokens = sample.documentLength(doc);
			for (int tok = 0; tok < numDocTokens; ++tok) {
				int symbol = sample.word(doc, tok);
				short topic = sample.topicSample(doc, tok);
				String word = mSymbolTable.idToSymbol(symbol);
				logger.debug(word + "(" + topic + ") \n");
			}
			logger.debug("\n");
		}
	}

	static double binomialZ(double wordCountInDoc, double wordsInDoc, double wordCountinCorpus, double wordsInCorpus) {
		double pCorpus = wordCountinCorpus / wordsInCorpus;
		double var = wordsInCorpus * pCorpus * (1 - pCorpus);
		double dev = Math.sqrt(var);
		double expected = wordsInDoc * pCorpus;
		double z = (wordCountInDoc - expected) / dev;
		return z;
	}

}
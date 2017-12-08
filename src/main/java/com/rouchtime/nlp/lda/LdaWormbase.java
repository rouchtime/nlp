package com.rouchtime.nlp.lda;


import com.aliasi.corpus.ObjectHandler;

import com.aliasi.cluster.LatentDirichletAllocation;

import com.aliasi.tokenizer.*;
import com.aliasi.symbol.*;
import com.aliasi.util.ObjectToCounterMap;

import tokenizer.AnsjNlpSelfDicTokenzierFactory;
import tokenizer.RegexStopTokenzierFactory;
import tokenizer.StopNatureTokenizerFactory;
import tokenizer.StopWordTokenierFactory;

import java.io.*;
import java.util.*;



// ftp://ftp.wormbase.org/pub/wormbase/misc/literature/2007-12-01-wormbase-literature.endnote.gz

public class LdaWormbase {

    public static void main(String[] args) throws Exception {
        File corpusFile = new File("D:\\corpus\\keyword\\tb_raw20171130\\tb_raw20171130.txt");
        int minTokenCount = 5;
        short numTopics = 50;
        double topicPrior = 0.1;
        double wordPrior = 0.01;
        int burninEpochs = 0;
        int sampleLag = 1;
        int numSamples = 2000;
        long randomSeed = 6474835;

        System.out.println("Citation file=" + corpusFile);
        System.out.println("Minimum token count=" + minTokenCount);
        System.out.println("Number of topics=" + numTopics);
        System.out.println("Topic prior in docs=" + topicPrior);
        System.out.println("Word prior in topics=" + wordPrior);
        System.out.println("Burnin epochs=" + burninEpochs);
        System.out.println("Sample lag=" + sampleLag);
        System.out.println("Number of samples=" + numSamples);

        CharSequence[] articleTexts = readCorpus(corpusFile);
        
        // reportCorpus(articleTexts);

        SymbolTable symbolTable = new MapSymbolTable();
        int[][] docTokens
            = LatentDirichletAllocation
            .tokenizeDocuments(articleTexts,ANSJTOKENZIERFACTORY,symbolTable,minTokenCount);
        
        System.out.println("Number of unique words above count threshold=" + symbolTable.numSymbols());

        int numTokens = 0;
        for (int[] tokens : docTokens)
            numTokens += tokens.length;
        System.out.println("Tokenized.  #Tokens After Pruning=" + numTokens);

        LdaReportingHandler handler
            = new LdaReportingHandler(symbolTable);
        
        LatentDirichletAllocation.GibbsSample sample
            = LatentDirichletAllocation
            .gibbsSampler(docTokens,

                          numTopics,
                          topicPrior,
                          wordPrior,

                          burninEpochs,
                          sampleLag,
                          numSamples,

                          new Random(randomSeed),
                          handler);

        int maxWordsPerTopic = 200;
        int maxTopicsPerDoc = 10;
        boolean reportTokens = true;
        handler.fullReport(sample,maxWordsPerTopic,maxTopicsPerDoc,reportTokens);
    }

    static void reportCorpus(CharSequence[] cSeqs) {
        ObjectToCounterMap<String> tokenCounter = new ObjectToCounterMap<String>();
        for (CharSequence cSeq : cSeqs) {
            char[] cs = cSeq.toString().toCharArray();
            for (String token : ANSJTOKENZIERFACTORY.tokenizer(cs,0,cs.length))
                tokenCounter.increment(token);
        }
        System.out.println("TOKEN COUNTS");
        for (String token : tokenCounter.keysOrderedByCountList())
            System.out.printf("%9d %s\n",tokenCounter.getCount(token),token);
    }

    static CharSequence[] readCorpus(File file) throws IOException {
        FileInputStream fileIn = new FileInputStream(file);
        InputStreamReader isReader = new InputStreamReader(fileIn,"utf-8");
        BufferedReader bufReader = new BufferedReader(isReader);

        List<CharSequence> articleTextList = new ArrayList<CharSequence>(15000);
        StringBuilder docBuf = new StringBuilder();
        String line;
        while ((line = bufReader.readLine()) != null) {
        	String splits[] = line.split("\t");
        	if(splits.length != 4) {
        		continue;
        	}
        	if(splits[3].equals("caijing")) {
        		articleTextList.add(splits[1] + "," + splits[2]);
        	}
        }
        bufReader.close();
        CharSequence[] articleTexts
            = articleTextList
            .<CharSequence>toArray(new CharSequence[articleTextList.size()]);
        return articleTexts;
    }

    static TokenizerFactory ANSJTOKENZIERFACTORY = getTokenizerFactory();

	private static TokenizerFactory getTokenizerFactory() {
		TokenizerFactory factory = AnsjNlpSelfDicTokenzierFactory.getIstance();
		factory = new StopWordTokenierFactory(factory);
		factory = new RegexStopTokenzierFactory(factory);
		factory = new StopNatureTokenizerFactory(factory);
		return factory;
	}

}
package com.rouchtime.nlp.keyword.extraction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.ObjectToDoubleMap;


/**
 * textRank多窗口关键词提取算法
 * @author 龚帅宾
 *
 */
public class TextRankWithMultiWinExtraction extends AbstractKeyWordExtraction{
	private int minWindow = 2;
	private int maxWindow = 10;
	public TextRankWithMultiWinExtraction(int minWindows,int maxWindows,TokenizerFactory tokenizerFactory){
		super(tokenizerFactory);
		this.minWindow = minWindows;
		this.maxWindow = maxWindows;
	}

	
	private  ObjectToDoubleMap<String> integrateMultiWindow(List<String> tokens) {
		Map<String, Float> tempKeywordScore = new HashMap<String, Float>();
		ObjectToDoubleMap<String> keywordMap = new ObjectToDoubleMap<>();
		String key = null;
		Float value = null;
		for (int i = minWindow; i <= maxWindow; i++) {
			TextRank.setWindowSize(i); // set the size of co-occurance window
			tempKeywordScore = TextRank.getWordScore(tokens);
			Iterator<Map.Entry<String, Float>> it = tempKeywordScore.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, Float> entry = it.next();
				key = entry.getKey();
				value = entry.getValue();
				if (keywordMap.containsKey(key))
					keywordMap.increment(key, value);
				else
					keywordMap.put(key, value.doubleValue());
			}
		}

		return keywordMap;
	}


	@Override
	ObjectToDoubleMap<String> modifyKeywordsSort(List<String> titleTokens, List<String> bodyTokens) {
		List<String> tokens = new ArrayList<String>();
		for(String token : titleTokens) {
			tokens.add(token.split("/")[0]);
		}
		for(String token : bodyTokens) {
			tokens.add(token.split("/")[0]);
		}
		return integrateMultiWindow(tokens);
	}

}

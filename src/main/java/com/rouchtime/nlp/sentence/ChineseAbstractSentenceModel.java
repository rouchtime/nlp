package com.rouchtime.nlp.sentence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 中文句子检测
 * 
 * @author 龚帅宾
 *
 */
public abstract class ChineseAbstractSentenceModel implements ChineseSentenceModel {

	protected ChineseAbstractSentenceModel() {
		/* do nothing */
	}

    public int[] boundaryIndices(String[] tokens) {
    	int i = 0;
    	for(String token : tokens) {
    		tokens[i] = token.split("/")[0];
    		i++;
    	}
        return boundaryIndices(tokens,0,tokens.length);
    }
	
	public int[] boundaryIndices(String[] tokens, int start, int length) {
		List<Integer> boundaries = new ArrayList<Integer>();
		boundaryIndices(tokens, start, length, boundaries);
		int[] result = new int[boundaries.size()];
		for (int i = 0; i < result.length; ++i)
			result[i] = boundaries.get(i).intValue();
		return result;
	}

	public abstract void boundaryIndices(String[] tokens, int start, int length,
			Collection<Integer> indices);
}

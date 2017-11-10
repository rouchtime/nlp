package com.rouchtime.nlp.keyword.hanlp;

import com.hankcs.hanlp.dictionary.stopword.CoreStopWordDictionary;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;

public class KeywordExtractor {

    /**
     * 默认分词器
     */
    Segment defaultSegment = StandardTokenizer.SEGMENT;

    /**
     * 是否应当将这个term纳入计算，词性属于名词、动词、副词、形容词
     *
     * @param term
     * @return 是否应当
     */
    public boolean shouldInclude(Term term)
    {
        // 除掉停用词
        if (term.nature == null) return false;
        String nature = term.nature.toString();
        char firstChar = nature.charAt(0);
        switch (firstChar)
        {
        	case 'n':
        	{
                if (term.word.trim().length() > 1 && !CoreStopWordDictionary.contains(term.word))
                {
                    return true;
                }
        	}
        	break;
            default:
            {
               return false;
            }
        }
        return false;
    }

    /**
     * 设置关键词提取器使用的分词器
     * @param segment 任何开启了词性标注的分词器
     * @return 自己
     */
    public KeywordExtractor setSegment(Segment segment)
    {
        defaultSegment = segment;
        return this;
    }

}

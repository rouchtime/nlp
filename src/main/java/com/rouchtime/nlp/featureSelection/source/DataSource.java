package com.rouchtime.nlp.featureSelection.source;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.aliasi.tokenizer.TokenizerFactory;
import com.google.common.collect.Table;
import com.rouchtime.nlp.corpus.ICorpus;
import com.rouchtime.nlp.featureSelection.bean.FeatureSelectionBean;


public abstract class DataSource {
	
    public DataSource() throws IOException {

    }

    public boolean reset(List<FeatureSelectionBean> corpus,TokenizerFactory factory) throws IOException {
        resetImpl(corpus,factory);
        return load();
    }
    abstract protected boolean resetImpl(List<FeatureSelectionBean> corpus,TokenizerFactory factory) throws IOException;
    abstract public boolean load() throws IOException;
    // 得到词典
    abstract public Set<String> getDictionary();
    //得到所有类别标识
    abstract public Set<String> getLabels();
    //类别数
    abstract public int getLabelCn();
    // 得到词典的大小
    abstract public int getDicSize();
    //文档数
    abstract public double getDocCn();

    // 工具方法，将map中对应键的值加x
    public static void addToMap(Map<String, Double> map, String key, double num){
        Double v = map.get(key);
        if(v == null)
            map.put(key, num);
        else map.put(key, v + num);
    }
    public static void addToMap(Table<String, String, Double> map, String key1, String key2, double num){
        if(map.contains(key1, key2))
            map.put(key1, key2, map.get(key1, key2) + num);
        else map.put(key1, key2, num);
    }
    public static void addToMap(Table<Integer, String, Double> map, Integer key1, String key2, double num){
        if(map.contains(key1, key2))
            map.put(key1, key2, map.get(key1, key2) + num);
        else map.put(key1, key2, num);
    }

}

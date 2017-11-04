package com.rouchtime.nlp.featureSelection.source;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import com.aliasi.tokenizer.TokenizerFactory;
import com.rouchtime.nlp.featureSelection.bean.FeatureSelectionBean;

/**
 * Created by py on 16-9-21.
 * 简单的数据源缓存，保证每个数据集，每种数据源只会创建一次
 */
public class SimpleDataSourcePool {
    static private Map<String, DataSource> cache = new HashMap<>();
    public static DataSource create(List<FeatureSelectionBean> corpus, Class clazz,TokenizerFactory factory)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException {
        String key = corpus.getClass().toString() + '_' + clazz.toString();
        if(cache.containsKey(key))
            return cache.get(key);
        else {
            Constructor con = clazz.getDeclaredConstructor();
            con.setAccessible(true);
            DataSource ds = (DataSource) con.newInstance();
            ds.reset(corpus,factory);
            cache.put(key, ds);
            return ds;
        }
    }
}

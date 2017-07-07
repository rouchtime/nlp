package featureselection.dataoperation;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import corpus.ICorpus;

/**
 * Created by py on 16-9-21.
 * 简单的数据源缓存，保证每个数据集，每种数据源只会创建一次
 */
public class SimpleDataSourcePool {
    static private Map<String, DataSource> cache = new HashMap<>();

    public static DataSource create(ICorpus corpus, Class clazz)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException {
        String key = corpus.getClass().toString() + '_' + clazz.toString();
//        System.out.println(cache + "  "+ key);
        if(cache.containsKey(key))
            return cache.get(key);
        else {
            Constructor con = clazz.getDeclaredConstructor();
            con.setAccessible(true);
            DataSource ds = (DataSource) con.newInstance();
            ds.reset(corpus);
            cache.put(key, ds);
//            System.out.println(cache + "  ====>");
            return ds;
        }

    }
}

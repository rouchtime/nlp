package task.lexrank;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HashBag<T> {

    private Map<T, Integer> bag;

    public HashBag() {
        bag = new HashMap<T, Integer>();
    }

    public HashBag(List<T> list) {
        this();
        addAll(list);
    }

    public void put(T value) {
        if (bag.containsKey(value)) {
            Integer num = bag.get(value);
            bag.put(value, num + 1);
        } else {
            bag.put(value, 1);
        }
    }

    public int get(T value) {
        Integer num = bag.get(value);
        return num == null ? 0 : num;
    }

    public Set<Map.Entry<T, Integer>> getAll() {
        return bag.entrySet();
    }

    public Set<T> getKeySet() {
    	return bag.keySet();
    }
    
    public void addAll(List<T> list) {
        for (T e : list) {
            put(e);
        }
    }
}

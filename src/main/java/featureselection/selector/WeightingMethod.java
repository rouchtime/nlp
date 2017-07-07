package featureselection.selector;

import java.io.IOException;

import com.aliasi.util.ObjectToDoubleMap;

/**
 * 
 * @author rouchtime
 *
 */
public interface WeightingMethod<E> {
	/**
	 * 将提取的特征输出到指定路径下
	 * @param output_dic
	 * @return
	 * @throws IOException
	 */
    public boolean computeAndPrint(String output_dic) throws IOException;
    
    public boolean compute();
    
    public boolean print(String output_dic);
    
    public ObjectToDoubleMap<E> result();
}

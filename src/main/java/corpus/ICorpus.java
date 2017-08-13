package corpus;

import java.util.List;
import java.util.Set;

import com.rouchtime.nlp.common.News;

/**
 * 
 * @author 龚帅宾
 */
public interface ICorpus {
	
	/**
	 * 获得语料的所有fileids(可以理解为标题)
	 * @return
	 * @throws Exception
	 */
	public List<String> fileids() throws Exception;
	/**
	 * 根据fileid获得分词
	 * @param fileid 标题
	 * @return
	 * @throws Exception
	 */
	public List<String> words(String fileid) ;
	
	/**
	 * 根据fileid获得句子
	 * @param fileid 标题
	 * @return
	 */
	public List<String> sents(String fileid);
	
	/**
	 * 根据fileid获得原始文本，未分词
	 * @param fileids
	 * @return
	 */
	public String raws(String fileids);
	
	/**
	 * 获得语料的存放路径
	 * @return
	 */
	public String path();
	
	public Set<String> labels();

	String url(String fileid);

	int picCount(String fileid);

	int paraCount(String fileid);

	String label(String fileid);

	List<News> newsFromLabel(String label);
}

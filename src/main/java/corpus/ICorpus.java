package corpus;

import java.util.List;

public interface ICorpus {
	public List<String> fileids() throws Exception;
	public List<String> words(String fileid) throws Exception;
	public List<String> sents(String fileid);
	
}

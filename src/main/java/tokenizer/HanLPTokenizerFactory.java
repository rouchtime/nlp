package tokenizer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.NLPTokenizer;

public class HanLPTokenizerFactory implements Serializable, TokenizerFactory {

	private static final long serialVersionUID = 2293162031266034831L;
	Map<String,String> natureConfig;
	private HanLPTokenizerFactory() {
		InputStream is = getClass().getResourceAsStream("/nlpdic/hanlp_nature_config");
		natureConfig = readFromFileNames(is);
	}

	private Map<String, String> readFromFileNames(InputStream is) {
		BufferedReader br = null;
		Map<String,String> map = new HashMap<String,String>();
		try {
			br = new BufferedReader(new InputStreamReader(is, "utf-8"));
			String s = null;
			while ((s = br.readLine()) != null) {
				map.put(s.split("\t")[0], s.split("\t")[1]);
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
			}
		}
		return map;
	}

	private static volatile HanLPTokenizerFactory instance;

	@Override
	public Tokenizer tokenizer(char[] ch, int start, int length) {
		return new HanLPTokenizer(ch, start, length);
	}

	public static HanLPTokenizerFactory getIstance() {
		if (instance == null) {
			synchronized (HanLPTokenizerFactory.class) {
				if (instance == null) {
					instance = new HanLPTokenizerFactory();
				}
			}
		}
		return instance;
	}

	class HanLPTokenizer extends Tokenizer {

		private List<Term> parse = new ArrayList<Term>();
		private int currentPos = -1;

		public HanLPTokenizer(char[] ch, int start, int length) {
			String text = String.valueOf(ch);
			parse = NLPTokenizer.segment(text);

		}
		public boolean hasNext() {
			if (parse == null || currentPos >= parse.size() - 1)
				return false;
			return true;
		}
		
		@Override
		public String nextToken() {
			if (parse == null || currentPos >= parse.size() - 1)
				return null;
			else {
				currentPos++;
				Term term = parse.get(currentPos);
				String natrue = natureConfig.get(term.nature.toString());
				if(natrue == null) {
					natrue = "un";
				}
				return term.word + "/" + natrue;
			}
		}

	}
}

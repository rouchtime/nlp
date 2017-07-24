package tokenizer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.aliasi.classify.TfIdfClassifierTrainer;
import com.aliasi.tokenizer.ModifyTokenTokenizerFactory;
import com.aliasi.tokenizer.TokenFeatureExtractor;
import com.aliasi.tokenizer.TokenizerFactory;

/**
 * 自定义停用词分词器
 * @author 龚帅宾
 *
 */
public class StopWordTokenierFactory extends ModifyTokenTokenizerFactory implements Serializable {
	private static final long serialVersionUID = -1312129063609071054L;

	private final Set<String> mStopSet;

	public StopWordTokenierFactory(TokenizerFactory factory, Set<String> stopSet) {
		super(factory);
		mStopSet = new HashSet<String>(stopSet);
	}

	/**
	 * 默认用系统自带的停用词
	 * @param factory
	 */
	public StopWordTokenierFactory(TokenizerFactory factory) {
		super(factory);
		InputStream is = getClass().getResourceAsStream("/stopwords.txt");
		mStopSet = readFromFileNames(is);
	}

	public Set<String> stopSet() {
		return Collections.unmodifiableSet(mStopSet);
	}

	@Override
	public String modifyToken(String token) {
		String[] term = token.split("/");
		if (term.length != 2) {
			return null;
		}
		return mStopSet.contains(term[0]) ? null : token;
	}

	@Override
	public String toString() {
		return this.getClass().getName() + "\n  stop set=" + mStopSet + "\n  base factory=\n    "
				+ baseTokenizerFactory().toString().replace("\n", "\n    ");
	}

	public Set<String> readFromFileNames(InputStream is) {
		BufferedReader br = null;
		Set<String> set = new HashSet<String>();
		try {
			br = new BufferedReader(new InputStreamReader(is, "utf-8"));
			String s = null;
			while ((s = br.readLine()) != null) {
				set.add(s.trim());
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
		return set;
	}
	public static void main(String[] args) {
		FudanNLPTokenzierFactory factory = FudanNLPTokenzierFactory.getIstance();
		@SuppressWarnings("unused")
		StopWordTokenierFactory stopFactory = new StopWordTokenierFactory(factory);
	}
}

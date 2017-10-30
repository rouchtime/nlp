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

import com.aliasi.tokenizer.ModifyTokenTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

public class StopNatureTokenizerFactory extends ModifyTokenTokenizerFactory implements Serializable {

	private static final long serialVersionUID = 764079574422299397L;

	private final Set<String> mStopSet;

	public StopNatureTokenizerFactory(TokenizerFactory factory) {
		super(factory);
		InputStream is = getClass().getResourceAsStream("/nlpdic/stopnature_jieba.txt");
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
		return mStopSet.contains(term[1]) ? null : token;
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
				set.add(s);
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
}

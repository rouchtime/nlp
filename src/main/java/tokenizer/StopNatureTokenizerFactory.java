package tokenizer;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.aliasi.tokenizer.ModifyTokenTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

public class StopNatureTokenizerFactory extends ModifyTokenTokenizerFactory implements Serializable {

	private static final long serialVersionUID = 764079574422299397L;

	private final Set<String> mStopSet;

	public StopNatureTokenizerFactory(TokenizerFactory factory, Set<String> stopSet) {
		super(factory);
		mStopSet = new HashSet<String>(stopSet);
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
}

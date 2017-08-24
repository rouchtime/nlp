package tokenizer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

public class NGramTokenizerBasedOtherTokenizerFactory implements Serializable, TokenizerFactory {

	private static final long serialVersionUID = 5304377133830271098L;
	private TokenizerFactory mfactory;
	private final int mMinNGram;
	private final int mMaxNGram;

	public NGramTokenizerBasedOtherTokenizerFactory(TokenizerFactory factory, int minNGram, int maxNGram) {
		this.mfactory = factory;
		mMinNGram = minNGram;
		mMaxNGram = maxNGram;
	}

	@Override
	public Tokenizer tokenizer(char[] ch, int start, int length) {
		return new NGramTokenizerBasedOtherTokenizer(ch, start, length);
	}

	class NGramTokenizerBasedOtherTokenizer extends Tokenizer {
		private List<String> parse = new ArrayList<String>();
		private final int mOffset;
		private final int mLength;
		private int mCurrentSize;
		private int mNextStart;

		public NGramTokenizerBasedOtherTokenizer(char[] ch, int offset, int length) {
			Iterator<String> iterator = mfactory.tokenizer(ch, offset, length).iterator();
			while (iterator.hasNext()) {
				parse.add(iterator.next().split("/")[0]);
			}
			mOffset = offset;
			mLength = parse.size();
			mCurrentSize = mMinNGram; // don't need to store min n-gram with this
			mNextStart = mOffset;
		}

		@Override
		public String nextToken() {
			while (mCurrentSize <= mMaxNGram && mNextStart + mCurrentSize > mOffset + mLength) {
				++mCurrentSize;
				mNextStart = mOffset;
			}
			if (mCurrentSize > mMaxNGram)
				return null;
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < mCurrentSize; i++) {
				sb.append(parse.get(mNextStart + i));
			}
			mNextStart++;
			return sb.toString();
		}
	}

	public static void main(String[] args) {
		String raw = "美国：示威者“勇闯”国会山抗议共和党医改法案遭逮捕";
		// NGramTokenizerFactory f = new
		// NGramTokenizerFactory(HanLPTokenizerFactory.getIstance(), 2);
		// f.tokenizer(raw.toCharArray(), 0, raw.length());
		StopWordTokenierFactory stopWordFactory = new StopWordTokenierFactory(HanLPTokenizerFactory.getIstance());
		NGramTokenizerBasedOtherTokenizerFactory factory = new NGramTokenizerBasedOtherTokenizerFactory(
				stopWordFactory, 1, 2);
		for (String word : factory.tokenizer(raw.toCharArray(), 0, raw.length())) {
			System.out.println(word);
		}
	}
}

package tokenizer;

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.WordDictionary;

public class JiebaTokenizerFactory implements Serializable, TokenizerFactory {

	private String modelPath;
	private WordDictionary wordDictionary;
	private JiebaTokenizerFactory() {
//		String pPath = JiebaTokenizerFactory.class.getProtectionDomain().getCodeSource().getLocation().getPath();
//		modelPath = new File(pPath).getParent() + "/conf/jieba.dict";
		modelPath = getClass().getClassLoader().getResource("conf/jieba.dict").getPath();
		wordDictionary = WordDictionary.getInstance();
		wordDictionary.loadUserDict(new File(modelPath).toPath(), Charset.forName("UTF-8"));
	}

	private static volatile JiebaTokenizerFactory instance;

	@Override
	public Tokenizer tokenizer(char[] ch, int start, int length) {
		return new JiebaTokenizer(ch, start, length);
	}

	public static JiebaTokenizerFactory getIstance() {
		if (instance == null) {
			synchronized (JiebaTokenizerFactory.class) {
				if (instance == null) {
					instance = new JiebaTokenizerFactory();
				}
			}
		}
		return instance;
	}

	class JiebaTokenizer extends Tokenizer {

		private List<String> parse = new ArrayList<String>();
		private int currentPos = -1;

		public JiebaTokenizer(char[] ch, int start, int length) {
			String text = String.valueOf(ch);
			JiebaSegmenter segmenter = new JiebaSegmenter();

			for (String word : segmenter.sentenceProcess(text)) {
				parse.add(word + "/" + "jieba");
			}
		}

		@Override
		public String nextToken() {
			if (parse == null || currentPos >= parse.size() - 1)
				return null;
			else {
				currentPos++;
				return parse.get(currentPos);
			}
		}

	}

	public static void main(String[] args) {
		String text = "本文是这个系列的第三篇文章，介绍了通过Builder模式应对参数过多的问题。如果你也希望参与类似的系列文章翻译，可以加入我们的Java开发 和 技术翻译 小组。";
		for(String word : JiebaTokenizerFactory.getIstance().tokenizer(text.toCharArray(), 0, text.length())) {
			System.out.println(word);
		}
	}
}

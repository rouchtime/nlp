package task.comment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.aliasi.classify.TradNaiveBayesClassifier;
import com.aliasi.spell.JaccardDistance;
import com.aliasi.tokenizer.NGramTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.Factory;
import com.aliasi.util.ObjectToCounterMap;
import com.rouchtime.nlp.duplicate.bean.DuplicateBean;
import com.rouchtime.nlp.duplicate.bean.Result;
import com.rouchtime.util.Contants;
import com.rouchtime.util.DuplicateUtils;
import com.rouchtime.util.RegexUtils;
import com.rouchtime.util.WekaUtils;

import tokenizer.HanLPTokenizerFactory;
import tokenizer.NGramTokenizerBasedOtherTokenizerFactory;
import tokenizer.StopNatureTokenizerFactory;
import tokenizer.StopWordTokenierFactory;

public class CommentFilter {

	/**
	 * 生成训练集和测试集
	 * 
	 * @param trainNum
	 * @throws IOException
	 */
	public static void task1() throws IOException {
		List<String> normals = FileUtils.readLines(new File("D:\\comment\\task\\normal.txt"), "utf-8");
		Random random = new Random(System.currentTimeMillis());
		for (int i = 0; i < 1000; i++) {
			int index = random.nextInt(normals.size());
			FileUtils.write(new File("D://comment//task//train"), normals.get(index) + "\n", "utf-8", true);
			normals.remove(index);
		}
		for (String normal : normals) {
			FileUtils.write(new File("D://comment//task//test"), normal + "\n", "utf-8", true);
		}

		List<String> spams = FileUtils.readLines(new File("D:\\comment\\task\\dup_spam.txt"), "utf-8");
		Random random1 = new Random(System.currentTimeMillis());
		for (int i = 0; i < 50; i++) {
			int index = random1.nextInt(spams.size());
			FileUtils.write(new File("D://comment//task//train"), spams.get(index) + "\n", "utf-8", true);
			spams.remove(index);
		}
		for (String spam : spams) {
			FileUtils.write(new File("D://comment//task//test"), spam + "\n", "utf-8", true);
		}
	}

	public static void task2() throws IOException {
		String dir = "D://comment//spam";
		List<String> lines = FileUtils.readLines(new File(dir, "spam"), "utf-8");
		for (String line : lines) {
			String normal = RegexUtils.cleanSpecialWord(line);
			if (normal.length() > 20 && !RegexUtils.isQQorWeiXinNum(normal) && !RegexUtils.isSpecialNumberSignal(normal)
					&& !RegexUtils.isSpamWord(normal)) {
				FileUtils.write(new File(dir, "normal_filter"), String.format("%s\n", line), "utf-8", true);
			} else {
				FileUtils.write(new File(dir, "normal_spam"), String.format("%s\n", line), "utf-8", true);
			}
		}
	}

	public static void task3() throws IOException {
		String dir = "D:\\comment\\spam";
		List<String> lines = FileUtils.readLines(new File(dir, "normal.txt"), "utf-8");
		StopWordTokenierFactory stopWordTokenierFactory = new StopWordTokenierFactory(
				HanLPTokenizerFactory.getIstance());
		StopNatureTokenizerFactory factory = new StopNatureTokenizerFactory(stopWordTokenierFactory);
		NGramTokenizerBasedOtherTokenizerFactory ngramFactory = new NGramTokenizerBasedOtherTokenizerFactory(factory, 1,
				3);
		for (String line : lines) {
			if (line.length() > 20) {
				String comment = RegexUtils.toSemiangle(RegexUtils.replacePunct(RegexUtils.cleanSpecialWord(line)));
				String text = WekaUtils.formWekaArffTextFromRaw(RegexUtils.cleanSpecialWord(comment), ngramFactory,
						"normal");
				FileUtils.write(new File(dir, "weka_spam_ngram.arff"), text, "utf-8", true);
			} else {
				FileUtils.write(new File(dir, "short_normal"), String.format("%s\n", line), "utf-8", true);
			}
		}

		List<String> spams = FileUtils.readLines(new File(dir, "spam.txt"), "utf-8");
		for (String line : spams) {
			if (line.length() > 20) {
				String comment = RegexUtils.toSemiangle(RegexUtils.cleanSpecialWord(line));
				String text = WekaUtils.formWekaArffTextFromRaw(RegexUtils.cleanSpecialWord(comment), ngramFactory,
						"spam");
				FileUtils.write(new File(dir, "weka_spam_ngram.arff"), text, "utf-8", true);
			} else {
				FileUtils.write(new File(dir, "short_spam.txt"), String.format("%s\n", line), "utf-8", true);
			}
		}
	}

	public static void calSimWithCommentAndArticle() throws IOException {
		NGramTokenizerBasedOtherTokenizerFactory ngramFactory = new NGramTokenizerBasedOtherTokenizerFactory(
				HanLPTokenizerFactory.getIstance(), 1, 2);
		StopWordTokenierFactory factory = new StopWordTokenierFactory(HanLPTokenizerFactory.getIstance());
		for (String line : FileUtils.readLines(new File("D://comment//spamfilter_article.txt"), "utf-8")) {
			String raw = RegexUtils.cleanSpecialWord(
					RegexUtils.cleanParaAndImgLabel(line.substring(line.indexOf("!@#!@"), line.length())));
			String comment = line.substring(line.indexOf("\t") + 1, line.indexOf("!@#!@"));
			// if (comment.length() < 20) {
			// continue;
			// }
			// JaccardDistance jaccardDistance = new JaccardDistance(factory);
			// double sim = jaccardDistance.proximity(comment, raw);
			FileUtils.write(new File("D://comment//spam.txt"), comment + "\n", "utf-8", true);
		}
	}

	public static void regexFilterWeiXinAndQQ() throws IOException {
		for (String line : FileUtils.readLines(new File("D://comment//unlabeled"), "utf-8")) {
			if (RegexUtils.isQQorWeiXinNum(line)) {
				FileUtils.write(new File("D://comment//regex//weinxin.txt"), line + "\n", "utf-8", true);
			} else {
				FileUtils.write(new File("D://comment//regex//normal.txt"), line + "\n", "utf-8", true);
			}
		}
	}

	public static void calSpamwords() throws IOException {
		String dir = "D:\\comment\\spam";
		StopWordTokenierFactory factory = new StopWordTokenierFactory(HanLPTokenizerFactory.getIstance());
		NGramTokenizerBasedOtherTokenizerFactory ngramFactory = new NGramTokenizerBasedOtherTokenizerFactory(factory, 1,
				3);
		ObjectToCounterMap<String> map = new ObjectToCounterMap<>();
		for (String line : FileUtils.readLines(new File(dir, "spam.txt"), "utf-8")) {
			String cleanLine = RegexUtils
					.cleanSequenceLetterOrNum(RegexUtils.cleanSpecialWord(RegexUtils.toSemiangle(line)));
			for (String term : ngramFactory.tokenizer(cleanLine.toCharArray(), 0, cleanLine.length())) {
				map.increment(term.split(Contants.SLASH)[0]);
			}
		}
		for (String word : map.keysOrderedByCountList()) {
			if (map.get(word).intValue() > 10) {
				FileUtils.write(new File(dir, "tf.txt"), String.format("%s\n", word), "utf-8", true);
			}
		}
	}

	public static void outSequenceLetterOrNum() throws IOException {
		String dir = "D:\\comment\\spam";
		for (String line : FileUtils.readLines(new File(dir, "spam.txt"), "utf-8")) {
			String cleanLine = RegexUtils.cleanSpecialWord(RegexUtils.toSemiangle(line));
			String seq = RegexUtils.findSpecialNumberSignal(cleanLine);
			if (seq != null) {

				FileUtils.write(new File(dir, "weixin.txt"), String.format("%s\n", seq), "utf-8", true);
			}

		}
	}

	public static void lengthFilter() throws IOException {
		for (String line : FileUtils.readLines(new File("D:\\comment\\comment_20170802_31//spam.txt"), "utf-8")) {
			if (line.length() > 20) {
				FileUtils.write(new File("D:\\comment\\comment_20170802_31//long.txt"), line + "\n", "utf-8", true);
			} else {
				FileUtils.write(new File("D:\\comment\\comment_20170802_31//short.txt"), line + "\n", "utf-8", true);
			}
		}
	}

	public static void duplicate(String dir) throws IOException {
		for (int i = 100; i < 200; i++) {
			List<String> spams = FileUtils.readLines(new File(dir, "spam" + i), "utf-8");

			List<DuplicateBean> dupicates = new ArrayList<DuplicateBean>();
			for (String spam : spams) {
				DuplicateBean db = new DuplicateBean();
				Long timestamp = System.currentTimeMillis();
				db.setId(String.valueOf(timestamp));
				db.setRaw(spam);
				db.setTimestamp(timestamp);
				dupicates.add(db);
			}
			DuplicateUtils dupUtils = DuplicateUtils.getIstance(new NGramTokenizerFactory(1, 3), Integer.MAX_VALUE,
					true);
			for (DuplicateBean bean : dupicates) {
				List<Result> results = dupUtils.duplicateLong(bean, 0.7);
				if (results.size() <= 0) {
					FileUtils.write(new File("D:\\comment\\spam\\spam" + (i + 1)), bean.getRaw() + "\n", "utf-8", true);
				} else {
					FileUtils.write(new File("D:\\comment\\spam\\spam_dup" + (i + 1)), bean.getRaw() + "\n", "utf-8",
							true);
				}
			}
		}
		// JaccardDistance jd = new JaccardDistance(HanLPTokenizerFactory.getIstance());
		// for (int i = 0; i < spams.size(); i++) {
		// String content = spams.get(i);
		// int dupcount = 0;
		// for (int j = 0; j < spams.size(); j++) {
		// if (jd.proximity(content, spams.get(j)) > 0.7) {
		// dupcount++;
		// }
		// }
		// if (dupcount == 1) {
		// FileUtils.write(new File("D:\\comment\\comment_20170802_31\\spam_dup.txt"),
		// content + "\n", "utf-8",
		// true);
		// }
		// }
	}

	public static void calPunctRatio(String dir) throws IOException {
		List<String> spams = FileUtils.readLines(new File(dir, "spam.txt"));
		List<String> normals = FileUtils.readLines(new File(dir, "normal.txt"));
		for (String spam : spams) {
			double spam_Count = RegexUtils.countpPunct(spam);
			FileUtils.write(new File(dir, "punctRatio.txt"),
					String.format("%.2f\t%s\n", spam_Count / (spam.length() - spam_Count), "spam"), "utf-8", true);
		}
		for (String normal : normals) {
			double normal_Count = RegexUtils.countpPunct(normal);
			FileUtils.write(new File(dir, "punctRatio.txt"),
					String.format("%.2f\t%s\n", normal_Count / (normal.length() - normal_Count), "normal"), "utf-8",
					true);
		}
	}

	public static void calSpecialWordCount(String dir) throws IOException {
		List<String> spams = FileUtils.readLines(new File(dir, "spam.txt"));
		List<String> normals = FileUtils.readLines(new File(dir, "normal.txt"));
		for (String spam : spams) {
			double spam_Count = RegexUtils.countNoChineseNoNumberNoEnglish(spam);
			FileUtils.write(new File(dir, "SpecialRatio.txt"), String.format("%.2f\t%s\n", spam_Count, "spam"), "utf-8",
					true);
		}
		for (String normal : normals) {
			double normal_Count = RegexUtils.countNoChineseNoNumberNoEnglish(normal);
			FileUtils.write(new File(dir, "SpecialRatio.txt"), String.format("%.2f\t%s\n", normal_Count, "normal"),
					"utf-8", true);
		}
	}

	public static void weixinNumer(String dir) throws IOException {
		List<String> spams = FileUtils.readLines(new File(dir, "spam.txt"));
		for (String spam : spams) {

		}
	}

	public static void task4() throws IOException {
		String dir = "D:\\comment\\spam";
		List<String> normals = FileUtils.readLines(new File(dir, "normal.txt"), "utf-8");
		StopWordTokenierFactory stopWordTokenierFactory = new StopWordTokenierFactory(
				HanLPTokenizerFactory.getIstance());
		NGramTokenizerBasedOtherTokenizerFactory ngramFactory = new NGramTokenizerBasedOtherTokenizerFactory(
				stopWordTokenierFactory, 1, 3);
		for (String normal : normals) {
			String comment = RegexUtils.toSemiangle(RegexUtils.cleanSpecialWord(normal));
			Set<String> commentWordSet = new HashSet<String>();
			for (String word : ngramFactory.tokenizer(comment.toCharArray(), 0, comment.length() - 1)) {
				commentWordSet.add(word);
			}
			double spamRatio = RegexUtils.spamWordRatio(commentWordSet);
			String seq = RegexUtils.findQQorWeiXinNum(comment);
			int sequenceLetterOrNumCount = (seq == null)?0:seq.length();
			boolean hasSpecialSignal = RegexUtils.isSpecialNumberSignal(comment);
			boolean hasSpecialSplit = RegexUtils.isContantsSpecialSignalWithQQOrWeiXin(comment);
			int length = normal.length();
			StringBuffer sb = new StringBuffer();
			sb.append(String.valueOf(spamRatio)).append(",").append(sequenceLetterOrNumCount).append(",")
					.append(hasSpecialSignal).append(",").append(hasSpecialSplit).append(",").append(length).append(",").append("normal");
			FileUtils.write(new File(dir,"spam_normal.txt"), String.format("%s\n", sb.toString()),"utf-8",true);
		}

		List<String> spams = FileUtils.readLines(new File(dir, "spam.txt"), "utf-8");
		for (String spam : spams) {
			String comment = RegexUtils.toSemiangle(RegexUtils.cleanSpecialWord(spam));
			Set<String> commentWordSet = new HashSet<String>();
			for (String word : ngramFactory.tokenizer(comment.toCharArray(), 0, comment.length() - 1)) {
				commentWordSet.add(word);
			}
			double spamRatio = RegexUtils.spamWordRatio(commentWordSet);
			String seq = RegexUtils.findQQorWeiXinNum(comment);
			int sequenceLetterOrNumCount = (seq == null)?0:seq.length();
			boolean hasSpecialSignal = RegexUtils.isSpecialNumberSignal(comment);
			boolean hasSpecialSplit = RegexUtils.isContantsSpecialSignalWithQQOrWeiXin(comment);
			int length = spam.length();
			StringBuffer sb = new StringBuffer();
			sb.append(String.valueOf(spamRatio)).append(",").append(sequenceLetterOrNumCount).append(",")
			.append(hasSpecialSignal).append(",").append(hasSpecialSplit).append(",").append(length).append(",").append("spam");
			FileUtils.write(new File(dir,"spam_normal.txt"), String.format("%s\n", sb.toString()),"utf-8",true);
		}
	}

	public static void shortLong() throws IOException {
		String dir = "C:\\Users\\Admin\\Downloads\\comment_article_09";
		List<String> normals = FileUtils.readLines(new File(dir, "comment_article_09.txt"), "utf-8");
		for(String normal : normals) {
			String[] splits = normal.split("\t");
			if(splits[1].length() > 20) {
				FileUtils.write(new File(dir,"long.txt"), String.format("%s\n", normal),"utf-8",true);
			} else {
				FileUtils.write(new File(dir,"short.txt"), String.format("%s\n", normal),"utf-8",true);
			}
		}
	}
	
	public static void filter() throws IOException {
		String dir = "D:\\comment\\spam";
		List<String> normals = FileUtils.readLines(new File(dir, "normal_remain.txt"), "utf-8");
		for(String normal : normals) {
			if(RegexUtils.isQQorWeiXinNum(normal)) {
				FileUtils.write(new File(dir,"normal_filter_pure.txt"), String.format("%s\n", normal),"utf-8",true);
			} else {
				FileUtils.write(new File(dir,"normal_remain2.txt"), String.format("%s\n", normal),"utf-8",true);
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		shortLong();
		// Set<String> categorySet = new HashSet<String>();
		// categorySet.add("spam");
		// categorySet.add("normal");
		// TradNaiveBayesClassifier initBayesClassifier = new
		// TradNaiveBayesClassifier(categorySet,
		// JiebaTokenizerFactory.getIstance());
		// for (String line : FileUtils.readLines(new
		// File("D:\\corpus\\comment\\siem_comment\\train.txt"), "utf-8")) {
		// String category = line.split("\t")[0];
		// String raw = line.split("\t")[1];
		// Classification classification = new Classification(category);
		// Classified<CharSequence> classified = new Classified<CharSequence>(raw,
		// classification);
		// initBayesClassifier.handle(classified);
		// }
		// ObjectOutputStream os;
		// os = new ObjectOutputStream(new FileOutputStream("D://test.model"));
		// initBayesClassifier.compileTo(os);
		// os.close();
		// String category = null;
		// String text = "特朗姆是想翻天了女友说我的性··生活就像玩一玩似得，根本就不能让她爽，在也是看了（ 高轩的经历）后，女友说她开心多了";
		// try {
		// ObjectInputStream oi = new ObjectInputStream(new
		// FileInputStream("D://test.model"));
		// ScoredClassifier<CharSequence> compiledClassifier =
		// (ScoredClassifier<CharSequence>) oi.readObject();
		// oi.close();
		// ScoredClassification channels =
		// compiledClassifier.classify(text.subSequence(0, text.length()));
		// category = channels.bestCategory();
		// System.out.println(category);
		// } catch (ClassNotFoundException | IOException e) {
		// e.printStackTrace();
		// }

		// File[] files = new File("D:\\corpus\\comment").listFiles();
		// for (File filedir : files) {
		// for (File file : filedir.listFiles()) {
		// List<String> lines = FileUtils.readLines(file);
		// for (String line : lines) {
		// FileUtils.write(new File("D://corpus//comment//total.txt"), line + "\n",
		// "utf-8", true);
		// }
		// }
		//
		// }
	}

	class TradNaiveBayesClassifierFactory implements Factory<TradNaiveBayesClassifier> {
		Set<String> m_categorySet;
		TokenizerFactory m_factory;

		public TradNaiveBayesClassifierFactory() {

		}

		public TradNaiveBayesClassifierFactory(Set<String> set, TokenizerFactory factory) {
			m_categorySet = set;
			m_factory = factory;
		}

		@Override
		public TradNaiveBayesClassifier create() {
			return new TradNaiveBayesClassifier(m_categorySet, m_factory);
		}

	}
}

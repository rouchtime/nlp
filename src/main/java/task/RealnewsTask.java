package task;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import corpus.FinanceNewsOrNonCorpus;
import corpus.ICorpus;
import corpus.RealOrNotRealNewsCorpus;
import tokenizer.FudanNLPTokenzierFactory;

public class RealnewsTask {

	public static int calPunctSize(String raw) {
		int count = 0;
		Pattern pattern = Pattern.compile("！|？|“|”|【|】");
		Matcher m = pattern.matcher(raw);
		while (m.find()) {
			if (m.group() != "") {
				count++;
			}
		}
		return count;
	}

	public static void main(String[] args) throws IOException {
		FudanNLPTokenzierFactory fudanNLP = FudanNLPTokenzierFactory.getIstance();
//		FinanceNewsOrNonCorpus corpus = new FinanceNewsOrNonCorpus(FudanNLP);
//		RealOrNotRealNewsCorpus corpus = new RealOrNotRealNewsCorpus();
		ICorpus corpus = new FinanceNewsOrNonCorpus(fudanNLP,"D://corpus//isnews_caijing.json");
		for (String label : corpus.labels()) {
			for (String title : corpus.fileidsFromLabel(label)) {
				StringBuffer print = new StringBuffer();
				double first_sents_date_count = 0;
				double pic_count = 0;
				double punct_count = 0;
				double raws_size = 0;
				double numeral_count = 0;
				double numeral_count_ratio = 0;
				double para_count = 0;
				double front_time_word_count = 0;
				double xun_word_count = 0;
				double person_name_count = 0;
				/* 记录第一句中的具有时间短语的数量，如2016年7月1日等 */
				String first_sents = corpus.sents(title).get(0);
				for (String term : fudanNLP.tokenizer(first_sents.toCharArray(), 0, first_sents.length())) {
					if (term.split("/")[1].equals("时间短语")) {
						first_sents_date_count++;
					}
				}
				
				
				/* 记录图片数量 */
				pic_count = corpus.picCount(title);
				
				
				/* 记录特殊标点数量 */
				punct_count = calPunctSize(corpus.raws(title));
				
				
				/* 记录原文长度 */
				raws_size = corpus.raws(title).length();
				
				
				/* 记录数词数量与文章长度比 */
				for (String term : fudanNLP.tokenizer(corpus.raws(title).toCharArray(), 0, (int) raws_size)) {
					if (term.split("/")[1].equals("数词")) {
						numeral_count++;
					}
				}
				numeral_count_ratio = numeral_count / raws_size;
				
				
				/* 记录段落数量 */
				para_count = corpus.paraCount(title);
				
				
				/* 记录文章前三句中包含今日，今天，今，昨日，昨，最近，近日， */
				StringBuffer sb = new StringBuffer();
				sb.append(corpus.sents(title).get(0));
				try {
					String secondSent = corpus.sents(title).get(1);
					sb.append(secondSent);
					String thirdSent = corpus.sents(title).get(2);
					sb.append(thirdSent);
					front_time_word_count = calDateWordSize(sb.toString());
				} catch (Exception e) {
					front_time_word_count = calDateWordSize(sb.toString());
				}
				
				
				/* 记录某某讯字眼 */
				String firstSents = corpus.sents(title).get(0);
				xun_word_count = calWordXunSize(firstSents);
				
				
				for (String ner : fudanNLP.ner(corpus.raws(title).toCharArray(), 0, (int) raws_size)) {
					if (ner.split("/")[1].equals("人名")) {
						person_name_count++;
					}
				}
				print.append(nonZero(first_sents_date_count)).append("\t");
				print.append(nonZero(pic_count)).append("\t");
				print.append(nonZero(punct_count)).append("\t");
				print.append(nonZero(raws_size)).append("\t");
				print.append(nonZero(numeral_count_ratio)).append("\t");
				print.append(nonZero(para_count)).append("\t");
				print.append(nonZero(front_time_word_count)).append("\t");
				print.append(nonZero(xun_word_count)).append("\t");
				print.append(nonZero(person_name_count)).append("\t");
				
				FileUtils.write(new File("D://corpus//realTimeOrNotNews//attr_normal_corpus_data"), print.append(label).append("\n"),true);
			}
		}
	}

	public static double nonZero(double num) {
		if(num-0<Double.MIN_NORMAL) {
			return 0.0001;
		}
		return num;
	}
	
	public static int calDateWordSize(String raw) {
		int count = 0;
		Pattern pattern = Pattern.compile("今天|今|今日|昨天|昨|上午|下午|近日");
		Matcher m = pattern.matcher(raw);
		while (m.find()) {
			if (m.group() != "") {
				count++;
			}
		}
		return count;
	}

	public static int calWordXunSize(String raw) {
		int count = 0;
		Pattern pattern = Pattern.compile("(\\w*)讯");
		Matcher m = pattern.matcher(raw);
		while (m.find()) {
			if (m.group() != "") {
				count++;
			}
		}
		return count;
	}

}

package task;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.rouchtime.util.RegexUtils;

import corpus.NonLabelRawCorpus;

public class RuleFinanceNews {
	public static void main(String[] args) throws Exception {
		NonLabelRawCorpus nonLabelRawCorpus = new NonLabelRawCorpus("D://corpus//corpus//total_new_caijing.json");
		for (String title : nonLabelRawCorpus.fileids()) {
			List<String> sents = nonLabelRawCorpus.sents(title);
			if (sents == null) {
				continue;
			}
			StringBuffer sb = new StringBuffer();
			if (sents.size() > 2) {
				sb.append(sents.get(0)).append("\t").append(sents.get(1));
			} else if (sents.size() > 1) {
				sb.append(sents.get(0));
			} else {
				continue;
			}
			if (title.indexOf("快讯") != -1) {
				FileUtils.write(new File("D://corpus//fenews//kuaixun//" + RegexUtils.filterPunct(title)),
						nonLabelRawCorpus.raws(title));
			}
			if (RegexUtils.isExistsDateWord(sb.toString())) {
				Long xinwenTime = RegexUtils.findDateConvertToTime(sb.toString());
				if (xinwenTime != null) {
					Date today = new Date();
					if ((today.getTime() - xinwenTime) < 7 * 24 * 60 * 60 * 1000
							&& (today.getTime() - xinwenTime) > -7 * 24 * 60 * 60 * 1000) {
						FileUtils.write(new File("D://corpus//fenews//date//inweek//" + RegexUtils.filterPunct(title)),
								nonLabelRawCorpus.raws(title));
						continue;
					} else {
						FileUtils.write(new File("D://corpus//fenews//date//outweek//" + RegexUtils.filterPunct(title)),
								nonLabelRawCorpus.raws(title));
						continue;
					}
				}
				FileUtils.write(new File("D://corpus//fenews//date//other//" + RegexUtils.filterPunct(title)),
						nonLabelRawCorpus.raws(title));
				continue;
			}
			if (RegexUtils.isExistsDateWord(title)) {
				FileUtils.write(new File("D://corpus//fenews//titleDate//" + RegexUtils.filterPunct(title)),
						nonLabelRawCorpus.raws(title));
				continue;
			}
			Long titleTime = RegexUtils.findDateContainsDotConvertToTime(title);
			if (titleTime != null) {
				FileUtils.write(new File("D://corpus//fenews//titleDate//" + RegexUtils.filterPunct(title)),
						nonLabelRawCorpus.raws(title));
				continue;

			}
			if (title.indexOf("今日") != -1) {
				FileUtils.write(new File("D://corpus//fenews//title_today//" + RegexUtils.filterPunct(title)),
						nonLabelRawCorpus.raws(title));
				continue;
			}
			if (title.indexOf("昨日") != -1) {
				FileUtils.write(new File("D://corpus//fenews//title_today//" + RegexUtils.filterPunct(title)),
						nonLabelRawCorpus.raws(title));
				continue;
			}

			if (RegexUtils.isExistsNewsReportWords(sb.toString())) {
				FileUtils.write(new File("D://corpus//fenews//newsReport//" + RegexUtils.filterPunct(title)),
						nonLabelRawCorpus.raws(title));
				continue;
			}
			if (RegexUtils.isExistsTimeWord(sb.toString())) {
				FileUtils.write(new File("D://corpus//fenews//time//" + RegexUtils.filterPunct(title)),
						nonLabelRawCorpus.raws(title));
				continue;
			}

			FileUtils.write(new File("D://corpus//fenews//unconfirm//" + RegexUtils.filterPunct(title)),
					nonLabelRawCorpus.raws(title));
		}
	}
}

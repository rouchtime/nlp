package task;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import com.aliasi.classify.TradNaiveBayesClassifier;
import com.alibaba.fastjson.JSONObject;
import com.rouchtime.nlp.common.NewsSig;
import com.rouchtime.util.RegexUtils;

public class OperateRawNews {
	
	public static String cleanText(String text) {
		text = text.replaceAll("(?:!@#!@)", "").replaceAll("(?:\\$#imgidx=\\d{4}#\\$)", "").replaceAll("&nbsp;", "");
		return text;
	}

	
	
	public static int calImgSize(String raw) {
		int count = 0;
		Pattern pattern = Pattern.compile("(\\$)?(#imgidx=)\\d{4}#\\$(。。)?");
		Matcher m = pattern.matcher(raw);
		while (m.find()) {
			if (m.group() != "") {
				count++;
			}
		}
		return count;
	}
	public static void readTotalText() throws IOException {
		File[] files = new File("D://corpus//duplicate//duplicate_raws_version1").listFiles();
		for(File file : files) {
			if(!file.getName().matches("xa.")) {
				continue;
			}
			List<String> rawnews = FileUtils.readLines(file, "utf-8");
			for (String rawnew : rawnews) {
				String[] splits = rawnew.split("\t+");
				if(splits == null || splits.length != 4) {
					continue;
				}
				try {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("url", splits[1]);
					String newsKey = RegexUtils.convertURLToNewsKey(splits[1]);
					if(newsKey == null) {
						continue;
					}
					String content = cleanText(splits[3]);
					if(content.matches("\\s+")) {
						continue;
					}
					jsonObject.put("newsKey", newsKey);
					jsonObject.put("title", splits[2]);
					jsonObject.put("content", content);
					FileUtils.write(new File("D:\\corpus\\duplicate\\duplicate_clean_json_version2"),
							jsonObject.toJSONString() + "\n", "utf-8", true);
				} catch (Exception e) {
					continue;
				}
			}
		}
	}
	
	public static boolean check() {
		System.out.println("aaaaaa");
		return false;
	}
	public static void main(String[] args) throws IOException {
//		readTotalText();
//		System.out.println("　　".matches("\\s+"));
//		TreeMap<String, Integer> bOWMap = new TreeMap<String,Integer>();
//		System.out.println(bOWMap.put("1", 1));
//		boolean flag = false;
//		if(flag || check()) {
//			System.out.println("bbbb");
//		}
	}
}

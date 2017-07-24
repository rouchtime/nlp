package corpus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.aliasi.tokenizer.TokenizerFactory;
import com.alibaba.fastjson.JSONObject;

import pojo.News;
import tokenizer.HanLPTokenizerFactory;

public class FinanceNewsOrNonCorpus extends BaseCorpus {

	public FinanceNewsOrNonCorpus(TokenizerFactory tokenizerFactory, String path, double trainRate) throws IOException {
		super(path, tokenizerFactory, trainRate);
	}

	/*
	 * 默认为HanLP分词器
	 */
	public FinanceNewsOrNonCorpus(String path) throws IOException {
		super(path, HanLPTokenizerFactory.getIstance(), 0.9);
	}

	@Override
	protected void createCorpusToRAM() {
		try {
			BufferedReader reader = new BufferedReader(
					(new InputStreamReader(new FileInputStream(new File(path)), Charsets.toCharset("utf-8"))));
			String line = reader.readLine();
			while (line != null) {
				String title = JSONObject.parseObject(line).getString("title");
				String label = JSONObject.parseObject(line).getString("label");
				String url = JSONObject.parseObject(line).getString("url");
				String article = JSONObject.parseObject(line).getString("article");
				News news = new News();
				news.setArticle(article);
				news.setLabel(label);
				news.setTitle(title);
				news.setUrl(url);
				newsMap_key_title.put(title, news);
				labels.add(label);
				if (labelToNewsMap.get(label) != null) {
					List<News> titles = labelToNewsMap.get(label);
					titles.add(news);
				} else {
					List<News> newses = new ArrayList<News>();
					labelToNewsMap.put(label, newses);
				}
				line = reader.readLine();
			}
		} catch (Exception e) {
			ExceptionUtils.getRootCauseMessage(e);
		}

	}
}

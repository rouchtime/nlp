package corpus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.DiskCorpus;
import com.aliasi.corpus.StringParser;
import com.aliasi.corpus.XValidatingObjectCorpus;
import com.aliasi.tokenizer.TokenizerFactory;
import com.alibaba.fastjson.JSONObject;

import pojo.News;
import tokenizer.HanLPTokenizerFactory;

public class FeNews2ClassCorpus extends BaseCorpus {

	public FeNews2ClassCorpus(String path, TokenizerFactory factory,double trainRate) throws IOException {
		super(path, factory,trainRate);
	}

	public FeNews2ClassCorpus(String path) throws IOException {
		super(path, HanLPTokenizerFactory.getIstance(),0.9);
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

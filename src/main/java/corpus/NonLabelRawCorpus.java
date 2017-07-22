package corpus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.aliasi.tokenizer.TokenizerFactory;
import com.alibaba.fastjson.JSONObject;

import pojo.News;
import tokenizer.HanLPTokenizerFactory;

public class NonLabelRawCorpus extends BaseCorpus{

	public NonLabelRawCorpus(TokenizerFactory factory,String path,double trainRate) throws IOException {
		super(path, factory,trainRate);
		this.tokenizerFactory = factory;
	}
	public NonLabelRawCorpus(String path) throws IOException {
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
				String url = JSONObject.parseObject(line).getString("url");
				String article = JSONObject.parseObject(line).getString("article");
				News news = new News();
				news.setArticle(article);
				news.setTitle(title);
				news.setUrl(url);
				newsMap_key_title.put(title, news);
				line = reader.readLine();
			}
		} catch (Exception e) {
			ExceptionUtils.getRootCauseMessage(e);
		}
	}
}

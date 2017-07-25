package corpus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.aliasi.tokenizer.TokenizerFactory;
import com.alibaba.fastjson.JSONObject;

import duplicate.pojo.News;
import tokenizer.HanLPTokenizerFactory;

public class DuplicateCorpus extends BaseCorpus {

	public DuplicateCorpus(String path, TokenizerFactory factory, double trainRate) throws IOException {
		super(path, factory, trainRate);
	}

	public DuplicateCorpus(String path) throws IOException {
		super(path, HanLPTokenizerFactory.getIstance(), 0.9);
	}

	@Override
	protected void createCorpusToRAM() {
		try {
			BufferedReader reader = new BufferedReader(
					(new InputStreamReader(new FileInputStream(new File(path)), Charsets.toCharset("utf-8"))));
			String line = reader.readLine();
			Integer idCount = 0;
			while (line != null) {
				JSONObject jsonObject = JSONObject.parseObject(line);
				String title = jsonObject.getString("title");
				String article = jsonObject.getString("article");
				String url = jsonObject.getString("url");
				String id = jsonObject.getString("id");
				if (id == null) {
					id = idCount.toString();
					idCount++;
				}
				News news = new News();
				news.setId(id);
				news.setArticle(article);
				news.setUrl(url);
				news.setTitle(title);
				idNewsMap.put(id, news);
				line = reader.readLine();

			}
		} catch (Exception e) {
			ExceptionUtils.getRootCauseMessage(e);
		}
	}

	public Map<String, News> getAllNewsIdMap() {
		return idNewsMap;
	}

	@Override
	protected void createTrainAndTestCorpus(double trainRate) {
		LinkedList<News> list = new LinkedList<News>(idNewsMap.values());
		int trainNum = (int) (idNewsMap.keySet().size() * trainRate);
		int randomSize = idNewsMap.keySet().size();
		for (int i = 0; i < trainNum; i++) {
			Random r = new Random(System.currentTimeMillis());
			int rIndex = r.nextInt(randomSize);
			trainSet.add(list.get(rIndex));
			list.remove(rIndex);
			randomSize--;
		}
		for(News news : list) {
			testSet.add(news);
		}
	}
}

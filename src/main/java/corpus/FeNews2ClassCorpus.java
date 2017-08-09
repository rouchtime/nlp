package corpus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.aliasi.tokenizer.TokenizerFactory;
import com.alibaba.fastjson.JSONObject;
import com.rouchtime.nlp.common.News;

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
					(new InputStreamReader(new FileInputStream(new File(path)), "utf-8")));
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
				titleNewsMap.put(title, news);
				labels.add(label);
				if (labelNewsMap.get(label) != null) {
					List<News> titles = labelNewsMap.get(label);
					titles.add(news);
				} else {
					List<News> newses = new ArrayList<News>();
					labelNewsMap.put(label, newses);
				}
				line = reader.readLine();
			}
		} catch (Exception e) {
			ExceptionUtils.getRootCauseMessage(e);
		}
	}
	@Override
	protected void createTrainAndTestCorpus(double trainRate) {
		for (String label : labelNewsMap.keySet()) {
			List<News> linkedList = new LinkedList<News>(labelNewsMap.get(label));
			int trainSize = (int) (linkedList.size() * trainRate);
			int randomSize = linkedList.size();
			for (int i = 0; i < trainSize; i++) {
				Random r = new Random(System.currentTimeMillis());
				int rIndex = r.nextInt(randomSize);
				trainSet.add(linkedList.get(rIndex));
				linkedList.remove(rIndex);
				randomSize--;
			}
			for(News news : linkedList) {
				testSet.add(news);
			}
		}

		
	}

}

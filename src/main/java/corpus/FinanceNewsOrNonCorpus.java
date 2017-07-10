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
import com.aliasi.tokenizer.TokenizerFactory;
import com.alibaba.fastjson.JSONObject;

import tokenizer.HanLPTokenizerFactory;

public class FinanceNewsOrNonCorpus extends BaseCorpus {

	public FinanceNewsOrNonCorpus(TokenizerFactory tokenizerFactory, String path) throws IOException {
		super(path, tokenizerFactory);
	}

	/*
	 * 默认为HanLP分词器
	 */
	public FinanceNewsOrNonCorpus(String path) throws IOException {
		super(path, HanLPTokenizerFactory.getIstance());
	}

	@Override
	protected void createCorpusToRAM() {

		try {
			BufferedReader reader = new BufferedReader(
					(new InputStreamReader(new FileInputStream(new File(path)), Charsets.toCharset("utf-8"))));
			String line = reader.readLine();
			while (line != null) {
				String title = JSONObject.parseObject(line).getString("title");
				String lable = JSONObject.parseObject(line).getString("label");
				String url = JSONObject.parseObject(line).getString("url");
				titleToRawNewsMap.put(title, line);
				titleToLableMap.put(title, lable);
				titleToURLMap.put(title, url);
				labels.add(lable);
				if (labelToTitleMap.get(lable) != null) {
					List<String> titles = labelToTitleMap.get(lable);
					titles.add(title);
				} else {
					List<String> titles = new ArrayList<String>();
					labelToTitleMap.put(lable, titles);
				}
				line = reader.readLine();
			}
		} catch (Exception e) {
			ExceptionUtils.getRootCauseMessage(e);
		}

	}
}

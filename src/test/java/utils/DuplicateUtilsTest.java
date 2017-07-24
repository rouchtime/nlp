package utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.alibaba.fastjson.JSONObject;

import junit.framework.TestCase;
import pojo.News;
import pojo.Result;
import tokenizer.AnsjTokenizerFactory;
import tokenizer.StopWordTokenierFactory;

public class DuplicateUtilsTest extends TestCase {
	public void testDuplicate() throws IOException {
		String outputPath = "D://corpus//dupResult.txt";
		StopWordTokenierFactory stopFactory = new StopWordTokenierFactory(AnsjTokenizerFactory.getIstance());
		News A = new News();
		A.setId("0");
		A.setArticle("李连杰战鹰犬，杨紫琼战公公，拳脚刀剑之战堪称经典,龚帅宾");
		News B = new News();
		B.setId("1");
		B.setArticle("李连杰战鹰犬，杨紫琼战公公，拳脚刀剑之战堪称经典，八点");
		News C = new News();
		C.setId("2");
		C.setArticle("拳脚刀剑之战堪称经典，李连杰战鹰犬，杨紫琼战公公，");
		News D = new News();
		D.setId("3");
		D.setArticle("杨紫琼战公公，拳脚刀剑之战堪称经典，李连杰战鹰");
		News E = new News();
		E.setId("4");
		E.setArticle("李连杰战鹰犬，杨紫琼战公公，拳脚刀剑之战堪称经典，太好了");
		News F = new News();
		F.setId("5");
		F.setArticle("李连杰战鹰犬，杨紫琼战公公，拳脚刀剑之战堪称经典，耶耶耶");

		List<News> listNews = new ArrayList<News>();
		listNews.add(A);
		listNews.add(B);
		listNews.add(C);

		List<News> underList = new ArrayList<News>();
		underList.add(D);
		underList.add(E);
		underList.add(F);
		int newsIndex = 0;

		DuplicateUtils duplicateUtils = new DuplicateUtils(listNews, stopFactory, 90000);
		for (News news : underList) {
			List<Result> dupIds = duplicateUtils.duplicateShort(news.getId(), news.getArticle(), 0.5);
			JSONObject originNewsJson = new JSONObject();
			originNewsJson.put("article", news.getArticle());
			originNewsJson.put("id", news.getId());
			FileUtils.write(new File(outputPath),
					String.format("视频%d:\n%s \n", newsIndex, originNewsJson.toJSONString()), "utf-8", true);
			for (Result result : dupIds) {
				JSONObject dupJsonObject = new JSONObject();
				dupJsonObject.put("id", result.getId());
				dupJsonObject.put("simlarity", result.getSimilariy());
				FileUtils.write(new File(outputPath), "重复列表：" + dupJsonObject.toJSONString() + "\n", "utf-8", true);
			}
			newsIndex++;
			FileUtils.write(new File(outputPath), "===============================================\n", "utf-8", true);
		}
	}

}

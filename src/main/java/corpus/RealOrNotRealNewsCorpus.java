package corpus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;


public final class RealOrNotRealNewsCorpus implements ICorpus{

	private static String url = "D://corpus//realTimeOrNotNews//news_nonnews_json";
	private static HashMap<String,String> titleToRawNewsMap;
	private static HashMap<String,String> labelToTitleMap;
	private static HashMap<String,String> categoryToTitleMap;
	static {
		try {
			List<String> newsList = FileUtils.readLines(new File(url), "utf-8");
			for(String news:newsList) {
				titleToRawNewsMap.put(JSONObject.parseObject(news).getString("title"),news);
				labelToTitleMap.put(JSONObject.parseObject(news).getString("label"), JSONObject.parseObject(news).getString("title"));
				categoryToTitleMap.put(JSONObject.parseObject(news).getString("category"), JSONObject.parseObject(news).getString("title"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public List<String> fileids() throws Exception {
		List<String> fileids = new ArrayList<String>(titleToRawNewsMap.keySet());
		return fileids;
		
	}

	public List<String> words(String fileid) throws IOException {
		String absolutePath = url + "//" + fileid;
		String raws = FileUtils.readFileToString(new File(absolutePath));
		
		return null;
	}

	public List<String> sents(String fileid) {
		// TODO Auto-generated method stub
		return null;
	}
	public static void main(String[] args) {
		RealOrNotRealNewsCorpus realorNotreal = new RealOrNotRealNewsCorpus();
		try {
			System.out.println(realorNotreal.fileids());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

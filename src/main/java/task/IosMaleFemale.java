package task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.rouchtime.util.WekaUtils;

import tokenizer.SpecialWordSplitTokenizerFactory;

public class IosMaleFemale {
	public static void oper() throws IOException {
		List<String> lines = FileUtils.readLines(new File("D:\\corpus\\sex_sample_gt_20170804_ios.txt"), "utf-8");
		Map<String,String> map = new HashMap<String,String>();
		for(String line : lines) {
			String id = line.split("\t+")[1];
			String label = line.split("\t+")[2];
			map.put(id, label);
		}
		
		BufferedReader reader = new BufferedReader(
				(new InputStreamReader(new FileInputStream(new File("D:\\corpus\\sex_keywords_ios")), Charsets.toCharset("utf-8"))));
		String line = reader.readLine();
		Integer idCount = 0;
		Map<String,Pair<String,String>> usrMap = new HashMap<String,Pair<String,String>>();
		while (line != null) {
			String id = line.split("\001")[0];
			String cate = line.split("\001")[1];
			String keyword = line.split("\001")[2];
			String sex = map.get(id);
			if(sex==null) {
				line = reader.readLine();
				continue;
			}
			if(null == usrMap.get(id)) {
				MutablePair<String, String> pair = new MutablePair<String, String>(keyword, sex);
				usrMap.put(id, pair);
			} else {
				String text = usrMap.get(id).getLeft() + "\t" + keyword;
				MutablePair<String, String> pair = new MutablePair<String, String>(text, sex);
				usrMap.put(id, pair);
			}
			line = reader.readLine();
		}
		
		SpecialWordSplitTokenizerFactory factory = SpecialWordSplitTokenizerFactory.getIstance("\t");
		for(String id : usrMap.keySet()) {
			String wekaText = WekaUtils.formWekaArffTextFromRaw(usrMap.get(id).getLeft(), factory, usrMap.get(id).getRight());
			FileUtils.write(new File("D://corpus//male_or_female"), wekaText,"utf-8",true);
		}
		
	}
	public static void main(String[] args) throws IOException {
		oper();
	}
}

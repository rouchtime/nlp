package task;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.rouchtime.nlp.duplicate.bean.DuplicateBean;
import com.rouchtime.nlp.duplicate.bean.Result;
import com.rouchtime.util.DuplicateUtils;
import com.rouchtime.util.RegexUtils;

import tokenizer.HanLPTokenizerFactory;
import tokenizer.StopWordTokenierFactory;

public class GuojiClear {
	public static void duplicate() throws IOException {
		String path = "D:\\corpus\\category\\guoji\\guoji170803\\guoji170803";
		File[] files = new File(path).listFiles();
		StopWordTokenierFactory stopTokenizerFactory = new StopWordTokenierFactory(HanLPTokenizerFactory.getIstance());
		Map<String,String> mapURL = new HashMap<String,String>();
		DuplicateUtils duplicateUtils = DuplicateUtils.getIstance(stopTokenizerFactory, 100000, true);
		for (File file : files) {
			List<String> lines = FileUtils.readLines(file);
			for (String line : lines) {
				String[] splits = line.split("\t+");
				if (line.split("\t+").length != 3) {
					System.err.println("ErrorLine:" + line);
					continue;
				}
				String id = RegexUtils.convertURLToNewsKey(splits[0]);
				mapURL.put(id, splits[0]);
				if (id == null) {
					System.err.println("ErrorNewsKey:" + splits[0]);
					continue;
				}
				String raw = RegexUtils.cleanParaAndImgLabel(splits[2]);
				DuplicateBean duplicateBean = new DuplicateBean();
				duplicateBean.setId(id);
				duplicateBean.setRaw(raw);
				duplicateBean.setTimestamp(System.currentTimeMillis());
				List<Result> results = duplicateUtils.duplicateLong(duplicateBean, 0.8);
				FileUtils.write(new File("D://corpus//category//guoji//duplicate"),
						line + "\n", "utf-8", true);
				if (results.size() > 1) {
					for(Result result : results) {
						FileUtils.write(new File("D://corpus//category//guoji//duplicate"),
								mapURL.get(result.getDuplicateBean().getId()) + "\n", "utf-8", true);
					}
				} else {
					FileUtils.write(new File("D://corpus//category//guoji//nonduplicate//" + file.getName()),
							line  + "\n", "utf-8", true);
				}
			}
		}
	}
	public static void main(String[] args) throws IOException {
		duplicate();
	}
}

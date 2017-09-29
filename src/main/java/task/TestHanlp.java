package task;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.dictionary.CustomDictionary;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;

import tokenizer.HanLPKeyWordTTokenizerFactory;
import tokenizer.HanLPTokenizerFactory;

public class TestHanlp {
	public static void main(String[] args) throws IOException {
		Nature.create("表情符号");
		for (String express : FileUtils.readLines(new File("D://face_expression.txt"), "utf-8")) {
			CustomDictionary.insert(express.trim(), "表情符号 1024");
		}
		StandardTokenizer.SEGMENT.enablePartOfSpeechTagging(true); // 依然支持隐马词性标注
		String text = "\\(0^◇^0)/苹果电脑可\\(0^◇^0)/以运行开源阿尔法狗代码吗，<。)#)))≤";
		System.out.println(HanLP.segment(text));
//		for (String token : HanLPTokenizerFactory.getIstance().tokenizer(text.toCharArray(), 0, text.length())) {
//			System.out.println(token);
//		}

		// List<String> lines = FileUtils.readLines(new File("D://title.txt"), "utf-8");
		// for (String line : lines) {
		// if(line.split("\001").length!=2) {
		// continue;
		// }
		// String url = line.split("\001")[0];
		// String text = line.split("\001")[1];
		// for (String token :
		// HanLPKeyWordTTokenizerFactory.getIstance(5).tokenizer(text.toCharArray(), 0,
		// text.length())) {
		//// if(token.split("/").length!=2) {
		//// continue;
		//// }
		//// if(token.split("/")[1].equals("nx")) {
		//// continue;
		//// }
		//// if (token.split("/")[1].matches("n.*")) {
		// FileUtils.write(new File("D://title_result_keyword.txt"),
		// String.format("%s\t%s\n", url, token),
		// "utf-8", true);
		//// }
		// }
		//
		// }
	}
}

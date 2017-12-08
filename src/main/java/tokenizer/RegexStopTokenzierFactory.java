package tokenizer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aliasi.tokenizer.ModifyTokenTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

public class RegexStopTokenzierFactory extends ModifyTokenTokenizerFactory implements Serializable {

	public RegexStopTokenzierFactory(TokenizerFactory factory) {
		super(factory);
		InputStream is = getClass().getResourceAsStream("/nlpdic/token_regex.txt");
		regexList = readFromFileNames(is);
	}

	List<String> regexList = new ArrayList<String>();

	@Override
	public String modifyToken(String token) {
		String[] term = token.split("/");
		if (term.length != 2) {
			return null;
		}
		for (String regex : regexList) {
			Pattern pattern = Pattern.compile(regex, Pattern.CANON_EQ);
			Matcher m = pattern.matcher(term[0]);
			if (m.find()) {
				return null;
			}
		}
		return token;
	}

	public List<String> readFromFileNames(InputStream is) {
		BufferedReader br = null;
		List<String> list = new ArrayList<String>();
		try {
			br = new BufferedReader(new InputStreamReader(is, "utf-8"));
			String s = null;
			while ((s = br.readLine()) != null) {
				list.add(s.trim());
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
			}
		}
		return list;
	}

	public static void main(String[] args) {
		AnsjNlpSelfDicTokenzierFactory factory = AnsjNlpSelfDicTokenzierFactory.getIstance();
		@SuppressWarnings("unused")
		StopWordTokenierFactory stopFactory = new StopWordTokenierFactory(factory);
		RegexStopTokenzierFactory regexFactory = new RegexStopTokenzierFactory(stopFactory);
		StopNatureTokenizerFactory stopNatrue = new StopNatureTokenizerFactory(regexFactory);
		String text = "2011年9月11日客户杨女士为丈夫刘先生投保了吉祥三宝A款两全保险，保费5733元。天有不测风云，人有旦夕祸福，2017年8月18日被保险人刘先生不慎溺水身故。";
		for(String word : stopNatrue.tokenizer(text.toCharArray(), 0, text.length())) {
			System.out.println(word);
		}
	}
}

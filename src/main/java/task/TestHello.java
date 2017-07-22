package task;

import tokenizer.FudanNLPTokenzierFactory;
import tokenizer.StopNatureTokenizerFactory;
import tokenizer.StopWordTokenierFactory;

public class TestHello {
	public static void main(String[] args) {
		String text = "，下半年三四线将成主战场。 文／克而瑞研究中心 2017年上半年，房地产行业政策整体延续分类调控、因城施策的主基调，一方面继续支持高库存的二线和三四线城";
		StopWordTokenierFactory stopTokenizerFactory = new StopWordTokenierFactory(
				FudanNLPTokenzierFactory.getIstance());
		StopNatureTokenizerFactory stopNatureFactory = new StopNatureTokenizerFactory(stopTokenizerFactory);
		for(String term : stopNatureFactory.tokenizer(text.toCharArray(), 0, text.length())) 
		{
			System.out.println(term);
		}
	}
}

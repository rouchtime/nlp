package task.yule;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.util.ObjectToSet;

public class YuLeCorpus extends Corpus<ObjectHandler<Classified<CharSequence>>> {

	final Map<String, String[]> labeledCatToTexts;
	final String[] unLabeledCatToTexts;
	int mMaxSupervisedInstancesPerCategory = 1;

	public YuLeCorpus(File path) throws IOException {
		File trainDir = new File(path, "train");
		File unlabeledDir = new File(path, "unlabeled");
		labeledCatToTexts = read(trainDir);
		unLabeledCatToTexts = FileUtils.readLines(unlabeledDir, "utf-8").toArray(new String[0]);

	}

	public Set<String> categorySet() {
		return labeledCatToTexts.keySet();
	}

	public void setMaxSupervisedInstancesPerCategory(int max) {
		mMaxSupervisedInstancesPerCategory = max;
	}

	public void visitTrain(ObjectHandler<Classified<CharSequence>> handler) {
		visit(labeledCatToTexts, handler, Integer.MAX_VALUE);
	}

	public Corpus<ObjectHandler<CharSequence>> unlabeledCorpus() {
		return new Corpus<ObjectHandler<CharSequence>>() {
			public void visitTest(ObjectHandler<CharSequence> handler) {
				throw new UnsupportedOperationException();
			}

			public void visitTrain(ObjectHandler<CharSequence> handler) {
				Random random = new Random(System.currentTimeMillis());
				Set<Integer> set = new HashSet<Integer>();
				while (set.size() < 10) {
					int r = random.nextInt(unLabeledCatToTexts.length);
					set.add(r);
				}
				for (Integer index : set) {
					try {
						String raw = unLabeledCatToTexts[index].split("\t")[2];
						handler.handle(raw);
					} catch (Exception e) {
						continue;
					}
				}
			}
		};
	}

	private static Map<String, String[]> read(File dir) throws IOException {
		ObjectToSet<String, String> catToTexts = new ObjectToSet<String, String>();
		File[] files = dir.listFiles();
		for (File file : files) {
			String cat = file.getName().replaceAll(".txt", "");
			for (String line : FileUtils.readLines(file)) {
				if (line.equals("")) {
					continue;
				}

				try {
					String[] raws = line.split("\t+");
					catToTexts.addMember(cat, raws[2].trim());
				} catch (Exception e) {
					System.out.println(String.format("Error:\t%s", line));
					continue;
				}
			}
		}
		Map<String, String[]> map = new HashMap<String, String[]>();
		for (Map.Entry<String, Set<String>> entry : catToTexts.entrySet())
			map.put(entry.getKey(), entry.getValue().toArray(new String[0]));
		return map;
	}

	private static void visit(Map<String, String[]> catToItems, ObjectHandler<Classified<CharSequence>> handler,
			int maxItems) {
		for (Map.Entry<String, String[]> entry : catToItems.entrySet()) {
			String cat = entry.getKey();
			Classification c = new Classification(cat);
			String[] texts = entry.getValue();
			for (int i = 0; i < maxItems && i < texts.length; ++i) {
				Classified<CharSequence> classifiedText = new Classified<CharSequence>(texts[i], c);
				handler.handle(classifiedText);
			}
		}
	}

}

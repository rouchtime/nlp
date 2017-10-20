package task.comment;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.util.ObjectToSet;

public class CommentCorpus extends Corpus<ObjectHandler<Classified<CharSequence>>> {

	final Map<String, String[]> labeledCatToTexts;
	final String[] unLabeledCatToTexts;
	final Map<String, String[]> mTestCatToTexts;
	int mMaxSupervisedInstancesPerCategory = 1;

	public CommentCorpus(File path) throws IOException {
		File trainDir = new File(path, "train");
		File testDir = new File(path, "test");
		File unlabeledDir = new File(path, "unlabeled");
		labeledCatToTexts = read(trainDir);
		mTestCatToTexts = read(testDir);
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

	public void visitTest(ObjectHandler<Classified<CharSequence>> handler) {
		visit(mTestCatToTexts, handler, Integer.MAX_VALUE);
	}

	public Corpus<ObjectHandler<CharSequence>> unlabeledCorpus() {
		return new Corpus<ObjectHandler<CharSequence>>() {
			public void visitTest(ObjectHandler<CharSequence> handler) {
				throw new UnsupportedOperationException();
			}

			public void visitTrain(ObjectHandler<CharSequence> handler) {
				for (String text : unLabeledCatToTexts) {
					handler.handle(text);
				}
			}
		};
	}

	private static Map<String, String[]> read(File dir) throws IOException {
		ObjectToSet<String, String> catToTexts = new ObjectToSet<String, String>();
		for (String line : FileUtils.readLines(dir)) {
			String cat = line.split("\t")[0];
			String raw = line.split("\t")[1];
			catToTexts.addMember(cat, raw);
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

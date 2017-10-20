package task.lexrank;

import java.util.ArrayList;
import java.util.List;
public class Sentence {

    private static final String[] specWords = new String[]{"-lrb-", "-rrb-"};

    private String sentence;
    private List<String> words;
    private Integer locationIndex;

	public Integer getLocationIndex() {
		return locationIndex;
	}

	public void setLocationIndex(Integer locationIndex) {
		this.locationIndex = locationIndex;
	}

	public Sentence(String sentence) {
        this.sentence = sentence;
    }

    public String getSentence() {
        return sentence;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    public List<String> getWords() {
        return words;
    }

    public void setWords(List<String> words) {
        for (int i = 0; i < words.size(); i++) {
            String word = words.get(i);
            words.set(i, word);       //todo remove to lowercase
        }
        this.words = filterSpecWords(words);
    }

    private List<String> filterSpecWords(List<String> words) {
        for (int i = 0; i < specWords.length; i++) {
            String specWord = specWords[i];
            while (words.contains(specWord)) {
                int index = words.indexOf(specWord);
                words.remove(index);
            }

        }
        return words;
    }

    @Override
    public String toString() {
        return sentence;
    }

    public boolean contains(String word) {
        return words.contains(word);
    }
    
}

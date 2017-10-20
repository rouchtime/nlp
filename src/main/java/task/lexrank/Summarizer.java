package task.lexrank;


import java.util.List;

public interface Summarizer {

    List<Sentence> summarize(Text text, Integer part);

}

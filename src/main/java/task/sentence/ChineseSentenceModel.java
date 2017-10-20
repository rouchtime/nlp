package task.sentence;

import java.util.Collection;

public interface ChineseSentenceModel {
	public int[] boundaryIndices(String[] tokens);
    public void boundaryIndices(String[] tokens,
            int start, int end,
            Collection<Integer> indices);
}

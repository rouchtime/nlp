package task.lexrank;

import java.util.*;
import java.io.*;
public class MMR123 {
	public static void main(String[] args) throws FileNotFoundException 
	{
		//Load stopwords list
		stopwords = new HashSet<String>();
		Scanner in = new Scanner(new File("stopwords.txt"));
		while(in.hasNext())
			stopwords.add(in.next());
		in.close();
		int myVar=0;
		String tempor;
		String name = "C:/Users/Anvay/Downloads/Final Hai Bhai/Final_Project/Documents";
		File mainDir = new File(name);
		String[] allDirs = mainDir.list();
		System.out.println(Arrays.toString(allDirs));
		int numDirs = allDirs.length;
		System.out.println(numDirs);
		for(int z = 0; z < numDirs; z++)
		{
			File dir = new File(name + "/"+allDirs[z]);
			
			File[] directoryListing = dir.listFiles();
			//System.out.println(Arrays.toString(directoryListing));
			int n = directoryListing.length;
			String allConcat = "";
			for(int i = 0; i < n; i ++)
			{
				in = new Scanner(directoryListing[i]);
				while(in.hasNext())
				{
					String next = in.nextLine();
					if(next.charAt(0) == '<')
						continue;
					allConcat +=  next;
				}
				in.close();
				allConcat += ". ";
				allConcat += '\n';
			}
			//preprocess data
			String[] processedSentences = processSentences(allConcat);
			String[] sentences = tokenizeSentences(allConcat);
			ArrayList<String>[] data = preprocess(sentences);
			int numSentences = data.length;
			int[][] vectors = getBagOfWords(data);
			int numWords = vectors[0].length;
			
			//get sum of all vectors
			int[] documentVector = new int[numWords];
			for(int i = 0; i < numSentences; i ++)
				for(int j = 0; j < numWords; j ++)
					documentVector[j] += vectors[i][j];
			
			//cosine similarity between each sentence and the whole document. Doesn't change
			double[] docSimilarity = new double[numSentences];
			for(int i = 0; i < numSentences; i ++)
				docSimilarity[i] = getCosineSimilarity(vectors[i], documentVector);
			
			
			//actual MMR algorithm
			int summaryLength = 5;
			double lambda = 0.4;
			HashSet<Integer> summary = new HashSet<Integer>();
			for(int t = 0; t < summaryLength; t++)
			{
				int bestSentence = -1;
				double bestMMRScore = -Double.MAX_VALUE;
				for(int i = 0; i < numSentences; i ++)
				{
					if(summary.contains(i))
						continue;
					double maxSimilarityToSummary = 0.0;
					for(int e: summary)
						maxSimilarityToSummary = Math.max(getCosineSimilarity(vectors[i], vectors[e]), maxSimilarityToSummary);
					if((lambda*docSimilarity[i] - (1-lambda)*maxSimilarityToSummary) > bestMMRScore)
					{
						bestMMRScore = lambda*docSimilarity[i] - (1-lambda)*maxSimilarityToSummary;
						bestSentence = i;
					}
				}
				summary.add(bestSentence);
			}
			
			//output
			tempor=allDirs[myVar]+".txt";
			System.out.println(tempor);
			myVar++;
			PrintWriter out = new PrintWriter(new File("output0.4/"+tempor));
			for(int e: summary)
				out.println(processedSentences[e] + ".");
			out.println();
			out.close();
			//outfile++;	
		}
	}
	
	//TODO: add actual sentence tokenization
	//tokenizes the senences that will be used for summarization
	static String[] tokenizeSentences(String s)
	{
		return s.split("[.]");
	}
	
	//processes the sentences that will be used for output.
	static String[] processSentences(String s)
	{
		String[] processedSentences = s.split("[.]");
		int n = processedSentences.length;
		for(int i = 0; i < n; i ++)
		{
			String[] a = processedSentences[i].split(" ");
			for(int j = 0; j < a.length; j ++)
				a[j] = trimWord(a[j]);
			StringBuilder sb = new StringBuilder();
			for(int j = 0; j < a.length; j ++)
			{
				sb.append(a[j]);
				if(j != a.length-1)
					sb.append(" ");
			}
			processedSentences[i] = sb.toString();
		}
		return processedSentences;
	}
	
	//trims an individual word of special characters
	static String trimWord(String word)
	{
		word = word.toLowerCase();
		while(word.length() > 0 && ((word.charAt(0) < 'a' || word.charAt(0) > 'z')&&!(word.charAt(0)>='0'&&word.charAt(0)<='9')))
			word = word.substring(1, word.length());
		while(word.length() > 0 && ((word.charAt(word.length() - 1) < 'a' || word.charAt(word.length() - 1) > 'z')&&!(word.charAt(word.length() - 1)>='0'&&word.charAt(word.length() - 1)<='9')))
			word = word.substring(0, word.length() - 1);
		return word;
	}
	
	//TODO: add lemmatization
	//takes in the list of sentences, returns preprocessed data
	static HashSet<String> stopwords;
	static ArrayList<String>[] preprocess(String[] sentences)
	{
		int n = sentences.length;
		ArrayList<String>[] data = new ArrayList[n];
		for(int i = 0; i < n; i ++)
			data[i] = new ArrayList<String>();
		for(int i = 0; i < n; i ++)
		{
			String[] words = sentences[i].split(" ");
			int m = words.length;
			for(int j = 0; j < m; j ++)
			{
				words[j] = trimWord(words[j]);
				words[j] = words[j].toLowerCase().trim();
				if(stopwords.contains(words[j]) || words[j].length() <= 1)
					continue;
				data[i].add(words[j]);
			}
		}
		return data;
	}
	
	//takes in preprocessed data, returns bag of words vectors
	static int[][] getBagOfWords(ArrayList<String>[] sentences)
	{
		int cIndex = 0;
		HashMap<String, Integer> index = new HashMap<String, Integer>();
		int n = sentences.length;
		for(int i = 0; i < n; i ++)
			for(String s: sentences[i])
				if(!index.containsKey(s))
				{
					index.put(s, cIndex);
					cIndex++;
				}
		
		cIndex++;
		int[][] vectors = new int[n][cIndex];
		for(int i = 0; i < n; i ++)
			for(String s: sentences[i])
				vectors[i][index.get(s)]++;
		
		return vectors;
	}
	
	//finds cosine similarity between two vectors
	static double getCosineSimilarity(int[] vec1, int[] vec2)
	{
		double ans = 0;
		double d1 = 0;
		double d2 = 0;
		double num = 0;
		int n = vec1.length;
		for(int i = 0; i < n; i ++)
		{
			d1+=vec1[i]*vec1[i];
			d2+=vec2[i]*vec2[i];
			num+=vec1[i]*vec2[i];
		}
		d1 = Math.sqrt(d1);
		d2 = Math.sqrt(d2);
		d1*=d2;
		ans = Math.acos(1 - (num/d1));
		return ans;
	}
}

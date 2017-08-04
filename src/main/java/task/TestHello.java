package task;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

public class TestHello {
	public static void main(String[] args) throws IOException {
		List<String> lines = FileUtils.readLines(new File("D:\\stopwords1.txt"), "utf-8");
		Set<String> set1 = new HashSet<String>();
		for (String line : lines) {
			set1.add(line.replaceAll("\\s+", line));
		}

		List<String> lines2 = FileUtils.readLines(new File("D:\\stopwords.txt"), "utf-8");
		Set<String> set2 = new HashSet<String>();
		for (String line : lines2) {
			set2.add(line.trim());
		}
		set1.addAll(set2);
		for (String a : set1) {
			FileUtils.write(new File("D://stopwords3.txt"), a + "\n", "utf-8", true);
		}

		// System.out.println(new Character(' ').hashCode());
	}

	public static String stringToHexString(String s) {
		String str = "";
		for (int i = 0; i < s.length(); i++) {
			int ch = (int) s.charAt(i);
			String s4 = Integer.toHexString(ch);
			str = str + s4;
		}
		return str;
	}
}

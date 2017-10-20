package task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

public class TestHello {
	public static void main(String[] args) throws IOException {
		FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		try {
			fis = new FileInputStream(new File("D:\\corpus\\category\\yule\\yule09\\yule09"));
			isr = new InputStreamReader(fis, "UTF-8");
			br = new BufferedReader(isr);
			String line = "";
			String[] arrs = null;
			while ((line = br.readLine()) != null) {
				try {

					arrs = line.split("\t");
					String raw = arrs[2];
					System.out.println(raw);
				} catch (Exception e) {
					continue;
				}
			}
		} catch (UnsupportedEncodingException e) {
			br.close();
			isr.close();
			fis.close();
		} catch (FileNotFoundException e) {
			br.close();
			isr.close();
			fis.close();
		} catch (IOException e) {
			br.close();
			isr.close();
			fis.close();
		}
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
	
	public static void op() throws IOException {
		File[] files = new File("D:\\corpus\\comment\\blackkey\\file").listFiles();
		for(File file : files) {
			File[] subFiles = file.listFiles();
			for(File subfile : subFiles) {
				List<String> lines = FileUtils.readLines(subfile, "utf-8");
				for(String line : lines) {
					String newLine = String.format("%s\n", 
							line.split("\t")[2]);
					FileUtils.write(new File("D://black_commentid"), newLine,"utf-8",true);
				}
			}
		}
	}
}

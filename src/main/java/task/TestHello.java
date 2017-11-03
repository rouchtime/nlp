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

import com.rouchtime.util.RegexUtils;

public class TestHello {
	public static void main(String[] args) throws IOException {
		String path = "D:\\corpus\\abstract\\ele.txt";
		for(String line : FileUtils.readLines(new File(path))) {
			String raw  = line.split("\t+")[2];
			raw= RegexUtils.cleanParaAndImgLabel(raw);
			FileUtils.write(new File("D:\\corpus\\abstract\\ele_raw.txt"), raw +"\n","utf-8",true);
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

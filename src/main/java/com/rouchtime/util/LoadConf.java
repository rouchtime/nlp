package com.rouchtime.util;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.nlpcn.commons.lang.util.FileFinder;
import org.nlpcn.commons.lang.util.IOUtil;

public class LoadConf {
	private Properties prop = new Properties();
	private static ResourceBundle rb = null;
	static {
		if (rb == null) {
			try {
				rb = ResourceBundle.getBundle("keywords");
			} catch (Exception e) {
				try {
					File find = FileFinder.find("keywords.properties", 2);
					if (find != null && find.isFile()) {
						rb = new PropertyResourceBundle(IOUtil.getReader(find.getAbsolutePath(), System.getProperty("file.encoding")));
						System.out.println("load library not find in classPath ! i find it in " + find.getAbsolutePath() + " make sure it is your config!");
					}
				} catch (Exception e1) {
					System.out.println(String.format("not find library.properties. and err {} i think it is a bug!", e1));
				
				}
			}
		}
	}
	private void loadconf() throws FileNotFoundException, IOException {
//		InputStreamReader inputStream = new InputStreamReader(LoadConf.class.getResourceAsStream("/keywords.properties"),
//				"UTF-8");
//		prop.load(inputStream);
	}

	public LoadConf() throws FileNotFoundException, IOException {
		loadconf();
	}

	public boolean chkProperty(String _key) {
		return prop.containsKey(_key);
	}

	public String getProperty(String _key) {
		return prop.getProperty(_key);
	}
}

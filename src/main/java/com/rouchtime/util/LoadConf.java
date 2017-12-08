package com.rouchtime.util;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

public class LoadConf {
	private Properties prop = new Properties();

	private void loadconf() throws FileNotFoundException, IOException {
		InputStreamReader inputStream = new InputStreamReader(LoadConf.class.getResourceAsStream("/keywords.properties"),
				"UTF-8");
		prop.load(inputStream);
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

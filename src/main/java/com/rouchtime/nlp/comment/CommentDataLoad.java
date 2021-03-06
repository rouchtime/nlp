package com.rouchtime.nlp.comment;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.rouchtime.util.Contants;

public class CommentDataLoad {
	private Logger logger = Logger.getLogger(CommentDataLoad.class);
	private Map<String, String> SPECIALNUMMAP ;
	private Set<String> FACEEXPRESSION;
	public Map<String, String> getSPECIALNUMMAP() {
		return SPECIALNUMMAP;
	}

	public Set<String> getFACEEXPRESSION() {
		return FACEEXPRESSION;
	}

	private CommentDataLoad() {
		InputStream is = null;
		ObjectInputStream oi = null;

		logger.info("Comment Data Loading ...");

		try {
			logger.debug("Load Data ...");
			SPECIALNUMMAP = new HashMap<String, String>();
			is = getClass().getResourceAsStream("/nlpdic/signalmap.txt");
			for (String line : IOUtils.readLines(is, "utf-8")) {
				if (String.valueOf(line).equals("null")) {
					continue;
				}
				if (line.split(Contants.TAB).length != 2) {
					continue;
				}
				SPECIALNUMMAP.put(line.split(Contants.TAB)[0], line.split(Contants.TAB)[1]);
				
				is = getClass().getResourceAsStream("/nlpdic/face_expression.txt");
				FACEEXPRESSION = new HashSet<String>(IOUtils.readLines(is, "utf-8"));
			}
		} catch (IOException e) {
			logger.error(ExceptionUtils.getRootCauseStackTrace(e));
		} finally {
			try {
				if (is != null) {
					is.close();
				}
				if (oi != null) {
					oi.close();
				}
			} catch (IOException e) {
				logger.error(ExceptionUtils.getRootCauseStackTrace(e));
			}
		}

	}

	public static CommentDataLoad getInstance() {
		return SingletonHolder.instance;
	}

	private static class SingletonHolder {
		private static CommentDataLoad instance = new CommentDataLoad();
	}
}
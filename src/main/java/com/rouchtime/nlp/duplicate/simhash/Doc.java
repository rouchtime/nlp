package com.rouchtime.nlp.duplicate.simhash;

import java.util.List;

public class Doc {
	private long hash;
	private String rowkey;
	private String keywords;
	

	public String getKeywords() {
		return keywords;
	}
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}
	public long getHash() {
		return hash;
	}
	public void setHash(long hash) {
		this.hash = hash;
	}
	public String getRowkey() {
		return rowkey;
	}
	public void setRowkey(String rowkey) {
		this.rowkey = rowkey;
	}
	
}

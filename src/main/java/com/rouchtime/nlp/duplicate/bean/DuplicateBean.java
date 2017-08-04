package com.rouchtime.nlp.duplicate.bean;

import java.io.Serializable;

public class DuplicateBean implements Serializable {
	private static final long serialVersionUID = 6000016295978790656L;
	private String id;
	private String raw;
	private Long timestamp;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getRaw() {
		return raw;
	}
	public void setRaw(String raw) {
		this.raw = raw;
	}
	public Long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
}

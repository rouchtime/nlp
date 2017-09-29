package com.rouchtime.util;

import java.util.List;

public class RegexBean {
	private Boolean isMatch;

	public Boolean getIsMatch() {
		return isMatch;
	}

	public void setIsMatch(Boolean isMatch) {
		this.isMatch = isMatch;
	}

	private String raw;
	private List<String> matches;
	private String remainRaw;

	public String getRemainRaw() {
		return remainRaw;
	}

	public void setRemainRaw(String remainRaw) {
		this.remainRaw = remainRaw;
	}

	public RegexBean(Boolean isMatch, String raw, List<String> matches, String remainRaw) {
		super();
		this.raw = raw;
		this.matches = matches;
		this.remainRaw = remainRaw;
		this.isMatch = isMatch;
	}

	public String getRaw() {
		return raw;
	}

	public List<String> getMatches() {
		return matches;
	}

	public RegexBean() {
		super();
	}

	public void setRaw(String raw) {
		this.raw = raw;
	}

	public void setMatches(List<String> matches) {
		this.matches = matches;
	}

}

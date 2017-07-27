package com.rouchtime.nlp.common;

import java.io.Serializable;

public class News implements Serializable{
	public News() {
		super();
	}
	public News(String id, String title, String article, String url) {
		super();
		this.id = id;
		this.title = title;
		this.article = article;
		this.url = url;
	}
	private static final long serialVersionUID = -5448373695477479745L;
	private String id; 
	private String title;
	private String article;
	private String url;
	private int picSize;
	private int pageSize;
	private String label;
	private String category;
	private Long timestamp;
	public Long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getArticle() {
		return article;
	}
	public void setArticle(String article) {
		this.article = article;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public int getPicSize() {
		return picSize;
	}
	public void setPicSize(int picSize) {
		this.picSize = picSize;
	}
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
}

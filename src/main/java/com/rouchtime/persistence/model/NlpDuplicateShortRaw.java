package com.rouchtime.persistence.model;

import java.util.Date;
import javax.persistence.*;

@Table(name = "nlp_duplicate_short_raw")
public class NlpDuplicateShortRaw {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url;

    @Column(name = "news_key")
    private String newsKey;

    private String title;

    private Date datetime;

    private String content;

    @Column(name = "dup_list")
    private String dupList;

    /**
     * @return id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return news_key
     */
    public String getNewsKey() {
        return newsKey;
    }

    /**
     * @param newsKey
     */
    public void setNewsKey(String newsKey) {
        this.newsKey = newsKey;
    }

    /**
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return datetime
     */
    public Date getDatetime() {
        return datetime;
    }

    /**
     * @param datetime
     */
    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    /**
     * @return content
     */
    public String getContent() {
        return content;
    }

    /**
     * @param content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * @return dup_list
     */
    public String getDupList() {
        return dupList;
    }

    /**
     * @param dupList
     */
    public void setDupList(String dupList) {
        this.dupList = dupList;
    }
}
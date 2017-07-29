package com.rouchtime.persistence.model;

import javax.persistence.*;

@Table(name = "nlp_finance_news_non_raw")
public class NlpFinanceNewsNonRaw extends AbstractRaw{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "news_key")
    private String newsKey;

    private String title;

    private String url;

    private String label;

    private String content;

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
     * @return label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label
     */
    public void setLabel(String label) {
        this.label = label;
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
}
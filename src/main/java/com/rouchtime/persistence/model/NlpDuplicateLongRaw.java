package com.rouchtime.persistence.model;

import java.util.Date;
import javax.persistence.*;

@Table(name = "nlp_duplicate_long_raw")
public class NlpDuplicateLongRaw extends AbstractRaw{

    private Date datetime;

    @Column(name = "dup_list")
    private String dupList;
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
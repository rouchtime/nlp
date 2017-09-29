package com.rouchtime.persistence.model;

import javax.persistence.*;

@Table(name = "nlp_classification_raw")
public class NlpClassificationRaw extends AbstractRaw{

    @Column(name = "first_label")
    private String firstLabel;

    @Column(name = "second_label")
    private String secondLabel;

    @Column(name = "third_label")
    private String thirdLabel;

    private String cname;

    

    /**
     * @return first_label
     */
    public String getFirstLabel() {
        return firstLabel;
    }

    /**
     * @param firstLabel
     */
    public void setFirstLabel(String firstLabel) {
        this.firstLabel = firstLabel;
    }

    /**
     * @return second_label
     */
    public String getSecondLabel() {
        return secondLabel;
    }

    /**
     * @param secondLabel
     */
    public void setSecondLabel(String secondLabel) {
        this.secondLabel = secondLabel;
    }

    /**
     * @return third_label
     */
    public String getThirdLabel() {
        return thirdLabel;
    }

    /**
     * @param thirdLabel
     */
    public void setThirdLabel(String thirdLabel) {
        this.thirdLabel = thirdLabel;
    }

    /**
     * @return cname
     */
    public String getCname() {
        return cname;
    }

    /**
     * @param cname
     */
    public void setCname(String cname) {
        this.cname = cname;
    }
}
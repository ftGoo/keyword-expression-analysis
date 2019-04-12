package com.wb.analysis.bean;

import java.io.Serializable;
import java.util.List;

public class AnalysisTree implements Serializable {
    private boolean isLeaf = false;
    private String word;

    private List<AnalysisTree> must;
    private List<AnalysisTree> must_not;
    private List<AnalysisTree> should;

    public List<AnalysisTree> getShould() {
        return should;
    }

    public void setShould(List<AnalysisTree> should) {
        this.should = should;
    }

    public List<AnalysisTree> getMust() {
        return must;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public void setLeaf(boolean leaf) {
        isLeaf = leaf;
    }

    public void setMust(List<AnalysisTree> must) {
        this.must = must;
    }

    public List<AnalysisTree> getMust_not() {
        return must_not;
    }

    public void setMust_not(List<AnalysisTree> must_not) {
        this.must_not = must_not;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

}

package com.wb.es.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoolFilter extends BaseFilter implements Serializable{
    private Map<String, Object> bool = new HashMap<>();

    private List<BaseFilter> must;
    private Object filter;
    private List<BaseFilter> must_not;
    private List<BaseFilter> should;


    public Map<String, Object> getBool() {
        return bool;
    }

    public void setMust(List<BaseFilter> must) {
        this.bool.put("must", must);
        this.must = must;
    }

    public void addShould(BaseFilter baseFilter) {
        if (should == null) {
            should = new ArrayList<>();
            this.bool.put("should", should);
            bool.put("minimum_should_match", 1);
        }
        should.add(baseFilter);
    }

    public void addMust(BaseFilter baseFilter) {
        if (must == null) {
            must = new ArrayList<>();
            this.bool.put("must", must);
        }
        must.add(baseFilter);
    }
    public void addMustNot(BaseFilter baseFilter) {
        if (must_not == null) {
            must_not = new ArrayList<>();
            this.bool.put("must_not", must_not);
        }
        must_not.add(baseFilter);
    }

    public void setFilter(Object filter) {
        this.bool.put("filter", filter);
        this.filter = filter;
    }

    public void setMust_not(List<BaseFilter> must_not) {
        this.bool.put("must_not", must_not);
        this.must_not = must_not;
    }

    public void setShould(List<BaseFilter> should) {
        this.bool.put("should", should);
        bool.put("minimum_should_match", 1);
        this.should = should;
    }

}

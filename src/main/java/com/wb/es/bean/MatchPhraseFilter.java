package com.wb.es.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MatchPhraseFilter extends BaseFilter implements Serializable {
    private Map<String, Object> match_phrase = new HashMap<>();

    public Map<String, Object> getMatch_phrase() {
        return match_phrase;
    }

    public MatchPhraseFilter(String key, Object value) {
        match_phrase.put(key, value);
    }

    public void setMatch(String key, Object value) {
        match_phrase.put(key, value);
    }
}

package com.colaorange.dailymoney.core.context;

import com.google.gson.annotations.Expose;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Dennis
 */
public class Card {

    @Expose
    CardType type;
    @Expose
    String title;
    @Expose
    Map<String, Object> args;

    protected Card() {

    }

    public Card(CardType type, String title) {
        this.type = type;
        this.title = title;
    }

    public Card(CardType type) {
        this.type = type;
    }

    public Card withArg(String key, Object value) {
        if (args == null) {
            args = new LinkedHashMap<>();
        }
        args.put(key, value);
        return this;
    }

    public <T> T getArg(String key) {
        return args == null ? null : (T) args.get(key);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

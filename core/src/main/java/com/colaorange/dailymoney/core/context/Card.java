package com.colaorange.dailymoney.core.context;

import com.colaorange.commons.util.JsonBase;
import com.google.gson.annotations.Expose;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Dennis
 */
public class Card extends JsonBase {

    @Expose
    CardType type;
    @Expose
    String title;
    @Expose
    LinkedHashMap<String, Object> args;

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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Card card = (Card) o;

        if (type != card.type) return false;
        if (title != null ? !title.equals(card.title) : card.title != null) return false;
        return args != null ? args.equals(card.args) : card.args == null;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (args != null ? args.hashCode() : 0);
        return result;
    }
}

package com.colaorange.dailymoney.core.data;

import com.colaorange.commons.util.JsonBase;
import com.colaorange.commons.util.Strings;
import com.google.gson.annotations.Expose;

import java.util.LinkedHashMap;

/**
 * Created by Dennis
 */
public class Card extends JsonBase {

    @Expose
    String id;
    @Expose
    CardType type;
    @Expose
    String title;
    @Expose
    LinkedHashMap<String, Object> args;

    protected Card() {
        getId();
    }

    public String getId(){
        if(id==null){
            id = Strings.randomUUID();
        }
        return id;
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
    public <T> T getArg(String key, T defval) {
        T val = getArg(key);
        return val==null?defval:val;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public CardType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Card card = (Card) o;

        return getId().equals(card.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}

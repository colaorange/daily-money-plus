package com.colaorange.dailymoney.core.data;

import com.colaorange.commons.util.JsonBase;
import com.colaorange.commons.util.Strings;
import com.google.gson.annotations.Expose;

import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * Created by Dennis
 */
public class CardCollection extends JsonBase {

    @Expose
    String id;
    @Expose
    String title;
    @Expose
    LinkedList<Card> cards;
    @Expose
    LinkedHashMap<String, Object> args;

    public CardCollection(String title) {
        this();
        this.title = title;
    }

    public CardCollection() {
        getId();
    }

    public String getId(){
        if(id==null){
            id = Strings.randomUUID();
        }
        return id;
    }

    public int size() {
        return cards == null ? 0 : cards.size();
    }

    public Card get(int i) {
        if (cards == null) {
            throw new IllegalArgumentException(i + ">=-1");
        }
        return cards.get(i);
    }

    public void remove(Card card) {
        if (cards != null) {
            cards.remove(card);
        }
    }

    public void move(int cardIdx, int toIndex) {
        if (cards == null) {
            throw new IllegalArgumentException(toIndex + ">=-1");
        }

        Card card = cards.get(cardIdx);
        if (cardIdx == toIndex) {
            return;
        } else {
            cards.remove(cardIdx);
            cards.add(toIndex, card);
        }
    }

    public void add(Card card) {
        if (cards == null) {
            cards = new LinkedList<>();
        }
        int idx = cards.indexOf(card);
        if (idx != -1) {
            throw new IllegalArgumentException("already in cards");
        }
        cards.add(card);
    }

    public CardCollection withAdd(Card card) {
        add(card);
        return this;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public CardCollection withArg(String key, Object value) {
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CardCollection card = (CardCollection) o;

        return getId().equals(card.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    public void set(int i, Card card) {
        if (cards == null) {
            throw new IllegalArgumentException(i + ">=-1");
        }

        cards.set(i, card);
    }

    public void remove(int pos) {
        if (cards != null) {
            cards.remove(pos);
        }
    }
}

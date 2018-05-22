package com.colaorange.dailymoney.core.context;

import com.colaorange.commons.util.JsonObject;
import com.google.gson.annotations.Expose;

import java.util.LinkedList;

/**
 * Created by Dennis
 */
public class CardCollection extends JsonObject {

    @Expose
    String title;
    @Expose
    LinkedList<Card> cards;

    public CardCollection(String title) {
        this.title = title;
    }

    public CardCollection() {

    }

    public int size() {
        return cards == null ? 0 : cards.size();
    }

    public Card get(int i) {
        if (cards == null) {
            throw new IllegalArgumentException(i+">=-1");
        }
        return cards.get(i);
    }

    public void remove(Card card) {
        if (cards != null) {
            cards.remove(card);
        }
    }

    public void move(int i, Card card) {
        if (cards == null) {
            throw new IllegalArgumentException(i+">=-1");
        }

        int idx = cards.indexOf(card);
        if (idx == -1) {
            throw new IllegalArgumentException("not in cards");
        }
        if (i >= cards.size()) {
            throw new ArrayIndexOutOfBoundsException(i + ">=" + cards.size());
        }

        if (idx == i) {
            return;
        } else if (idx > i) {
            cards.remove(idx);
            cards.add(i, card);
        } else {
            cards.remove(idx);
            cards.add(i - 1, card);
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

    public CardCollection withAdd(Card card){
        add(card);
        return this;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


}

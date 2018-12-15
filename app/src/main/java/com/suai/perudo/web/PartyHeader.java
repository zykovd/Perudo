package com.suai.perudo.web;

/**
 * Created by dmitry on 09.12.18.
 */

public class PartyHeader {
    private long id;
    private String message;
    private int hash;

    public PartyHeader(Party party) {
        this.id = party.getId();
        this.message = party.getMessage();
        this.hash = party.hashCode();
    }

    public long getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public int getHash() {
        return hash;
    }

}

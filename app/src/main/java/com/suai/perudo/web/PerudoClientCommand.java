package com.suai.perudo.web;

import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Serializable;

/**
 * Created by dmitry on 04.11.18.
 */

//public class PerudoClientCommand implements Serializable{
//    private boolean isMaputo = false;
//    private boolean isBid = false;
//    private boolean isLeave = false;
//    private boolean isDoubt = false;
//
//    private int currentBidQuantity = 0;
//    private int currentBidValue = 0;
//
////    public PerudoClientCommand(boolean isBid, int currentBidQuantity, int currentBidValue, boolean isDoubt, boolean isLeave, boolean isMaputo) {
////        this.isMaputo = isMaputo;
////        this.isBid = isBid;
////        this.isDoubt = isDoubt;
////        this.isLeave = isLeave;
////        this.currentBidQuantity = currentBidQuantity;
////        this.currentBidValue = currentBidValue;
////    }
//
//    public PerudoClientCommand(PerudoClientCommandEnum perudoClientCommandEnum){
//        switch (perudoClientCommandEnum){
//            case BID:
//                isBid = true;
//                break;
//            case DOUBT:
//                isDoubt = true;
//                break;
//            case LEAVE:
//                isLeave = true;
//                break;
//            case MAPUTO:
//                isMaputo = true;
//                break;
//        }
//    }
//
//    public PerudoClientCommand(PerudoClientCommandEnum perudoClientCommandEnum, int currentBidQuantity, int currentBidValue){
//        switch (perudoClientCommandEnum){
//            case BID:
//                isBid = true;
//                this.currentBidQuantity = currentBidQuantity;
//                this.currentBidValue = currentBidValue;
//                break;
//            case DOUBT:
//                isDoubt = true;
//                break;
//            case LEAVE:
//                isLeave = true;
//                break;
//            case MAPUTO:
//                isMaputo = true;
//                break;
//        }
//    }
//
//    public boolean isMaputo() {
//        return isMaputo;
//    }
//
//    public boolean isBid() {
//        return isBid;
//    }
//
//    public boolean isLeave() {
//        return isLeave;
//    }
//
//    public boolean isDoubt() {
//        return isDoubt;
//    }
//
//    public Pair getBid() {
//        return Pair.create(currentBidQuantity, currentBidValue);
//    }
//
//    public String toJson() {
//        Gson gson = new GsonBuilder().create();
//        return gson.toJson(this);
//    }
//
//}

public class PerudoClientCommand implements Serializable{
    private PerudoClientCommandEnum commandEnum;

//    private boolean isMaputo = false;
//    private boolean isBid = false;
//    private boolean isLeave = false;
//    private boolean isDoubt = false;
//    private boolean isJoin = false;
//    private boolean isGetParties = false;
//    private boolean isNewParty = false;
//    private boolean isDisconnect = false;

    private int currentBidQuantity = 0;
    private int currentBidValue = 0;
    private Party party = null;
    private String login;
    private String password;

    public PerudoClientCommand(PerudoClientCommandEnum perudoClientCommandEnum){
        this.commandEnum = perudoClientCommandEnum;
    }

    public PerudoClientCommand(PerudoClientCommandEnum perudoClientCommandEnum, Party party){
        this.commandEnum = perudoClientCommandEnum;
        this.party = party;
    }

    public PerudoClientCommand(PerudoClientCommandEnum perudoClientCommandEnum, String login, String password){
        this.commandEnum = perudoClientCommandEnum;
        this.login = login;
        this.password = password;
    }

    public PerudoClientCommand(PerudoClientCommandEnum perudoClientCommandEnum, int currentBidQuantity, int currentBidValue){
        this.commandEnum = perudoClientCommandEnum;
        this.currentBidQuantity = currentBidQuantity;
        this.currentBidValue = currentBidValue;
    }

    public PerudoClientCommandEnum getCommand() {
        return commandEnum;
    }

    public boolean isGameCommand(){
        return (commandEnum == PerudoClientCommandEnum.BID) || (commandEnum == PerudoClientCommandEnum.DOUBT) ||
                (commandEnum == PerudoClientCommandEnum.LEAVE) || (commandEnum == PerudoClientCommandEnum.MAPUTO) ||
                (commandEnum == PerudoClientCommandEnum.START_GAME);
    }

    public Party getParty() {
        return party;
    }

    public Pair getBid() {
        return new Pair(currentBidQuantity, currentBidValue);
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String toJson() {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(this);
    }


}


package com.suai.perudo.view;

import android.app.Application;

import com.suai.perudo.model.Player;
import com.suai.perudo.web.PerudoClient;
import com.suai.perudo.web.PerudoServer;

/**
 * Created by dmitry on 13.09.18.
 */

public class PerudoApplication extends Application {
    private PerudoServer perudoServer;
    private PerudoClient perudoClient;
    private boolean isServer;
    private boolean isOffline;
    private Player player;

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public PerudoServer getPerudoServer() {
        return perudoServer;
    }

    public void setPerudoServer(PerudoServer perudoServer) {
        this.perudoServer = perudoServer;
    }

    public PerudoClient getPerudoClient() {
        return perudoClient;
    }

    public void setPerudoClient(PerudoClient perudoClient) {
        this.perudoClient = perudoClient;
    }

    public boolean isServer() {
        return isServer;
    }

    public void setServer(boolean server) {
        isServer = server;
    }

    public boolean isOffline() {
        return isOffline;
    }

    public void setOffline(boolean offline) {
        isOffline = offline;
    }
}

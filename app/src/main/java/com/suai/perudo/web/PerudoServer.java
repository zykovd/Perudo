package com.suai.perudo.web;

import android.content.Context;
import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.suai.perudo.model.PerudoModel;
import com.suai.perudo.model.Player;
import com.suai.perudo.view.GameActivity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by dmitry on 11.09.18.
 */

public class PerudoServer extends Thread {

    private GsonBuilder builder = new GsonBuilder();
    private Gson gson = builder.create();

    private GameActivity view = null;

    @Expose private PerudoModel model;
    @Expose private String message;
    @Expose private String loser;
    @Expose private boolean newTurn = false;

    private ServerSocket serverSocket;
    @Expose private Player onServerPlayer;
    private int port;

    private HashMap<WebUser, Player> players = new HashMap<>();

    @Expose private LinkedList<ChatMessage> chatMessages = new LinkedList<>();
    @Expose private int maxChatMessages = 30;
    @Expose private boolean newChatMessage = false;
    private boolean reloadSending = false;

    public int getNumberOfPlayers() {
        return players.size();
    }

    /*public PerudoServer(PerudoServer server) {
        this.model = server.model;
        this.message = server.message;
        this.loser = server.loser;
        this.newTurn = server.newTurn;
    }*/

    public Player getOnServerPlayer() {
        return onServerPlayer;
    }

    public PerudoServer(int port, Player onServerPlayer) throws IOException, ClassNotFoundException {
        this.onServerPlayer = onServerPlayer;
        this.port = port;
    }

    public void setView(GameActivity view) {
        this.view = view;
    }

    @Override
    public void run() {
        super.run();
        try {
            this.serverSocket = new ServerSocket(port);
            String address = serverSocket.getLocalSocketAddress().toString();
            System.out.println(address);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //while (model == null) {
        try {
            while (true) {
                System.out.println("PerudoServer.run");
                System.out.println("serverSocket = " + serverSocket);
                Socket clientSocket = serverSocket.accept();

                WebUser webUser = new WebUser(clientSocket);
                DataInputStream dataInputStream = webUser.getDataInputStream();
                Player player = gson.fromJson(dataInputStream.readUTF(), Player.class);

                if (model.getPlayers().contains(player))
                    player = model.getPlayers().get(model.getPlayers().indexOf(player));

                webUser.setLogin(player.getName());

                for (WebUser user : players.keySet()) {
                    if (user.equals(webUser) && user.isConnected()) {
                        DataOutputStream dataOutputStream = webUser.getDataOutputStream();
                        dataOutputStream.writeUTF(new PerudoServerResponse(model, PerudoServerResponseEnum.JOIN_ERROR, null).toJson());
                        return;
                    } else if (user.equals(webUser) && !user.isConnected()) {
                        DataOutputStream dataOutputStream = webUser.getDataOutputStream();
                        dataOutputStream.writeUTF(new PerudoServerResponse(model, PerudoServerResponseEnum.CONNECTED, players.get(webUser).getDices()).toJson());
                        dataOutputStream.writeUTF(new PerudoServerResponse(model, chatMessages, PerudoServerResponseEnum.JOINED_PARTY, players.get(webUser).getDices()).toJson());
                        Player p = players.remove(webUser);
                        players.put(webUser, p);
                        new PerudoServerThread(webUser).start();
                        System.out.println("player = " + p);
                        return;
                    }
                }

                DataOutputStream dataOutputStream = webUser.getDataOutputStream();
                dataOutputStream.writeUTF(new PerudoServerResponse(model, PerudoServerResponseEnum.CONNECTED, null).toJson());

                players.put(webUser, player);
                new PerudoServerThread(webUser).start();
//                if (model != null && model.isGameStarted()) {
//                    PerudoServerResponse response = new PerudoServerResponse(model, PerudoServerResponseEnum.JOINED_PARTY, players.get(webUser).getDices());
//                    try {
//                        webUser.getDataOutputStream().writeUTF(response.toJson());
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
                System.out.println("player = " + player);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public PerudoServerResponse startGame() {
        ArrayList<Player> playersList = new ArrayList<>();
        for (Map.Entry<WebUser, Player> entry : players.entrySet()) {
            playersList.add(entry.getValue());
        }
        playersList.add(onServerPlayer);
        this.model = new PerudoModel(playersList, 6);
        this.model.setGameStarted(true);
        this.model.refreshDices();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    resendChangesToClients();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        saveGame();
        return new PerudoServerResponse(model, PerudoServerResponseEnum.GAME_START, onServerPlayer.getDices());
    }

    public void load(PerudoServer perudoServer) {
        this.model = perudoServer.model;
        this.message = perudoServer.message;
        this.loser = perudoServer.loser;
        this.newTurn = perudoServer.newTurn;
        this.chatMessages = perudoServer.chatMessages;
        this.maxChatMessages = perudoServer.maxChatMessages;
        this.newChatMessage = perudoServer.newChatMessage;
    }

    public boolean isContinueSavedGame(){
        if (model == null)
            return false;
        else
            return model.isGameStarted();
    }

    public PerudoServerResponse processOnServerPlayerCommand(PerudoClientCommand perudoClientCommand) {
        PerudoServerResponse perudoServerResponse = null;
        if (model.isGameEnded()) {
            PerudoServerResponse response = new PerudoServerResponse(model, PerudoServerResponseEnum.GAME_END, onServerPlayer.getDices());
            response.setMessage(message);
            return response;
        }
        if ((model == null || !model.isPlayersTurn(onServerPlayer)) && perudoClientCommand.isTurnCommand()) {
            perudoServerResponse = new PerudoServerResponse(model, PerudoServerResponseEnum.WRONG_TURN, null);
            return perudoServerResponse;
        }
        boolean isStateChanged = tryProceedCommand(perudoClientCommand, null);
        if (isStateChanged) {
            if (reloadSending) {
                perudoServerResponse = new PerudoServerResponse(model, chatMessages, PerudoServerResponseEnum.JOINED_PARTY, onServerPlayer.getDices());
            }
            else {
                if (newTurn) {
                    if (loser.equals(onServerPlayer.getName()) && onServerPlayer.getNumberOfDices() == 1) {
                        perudoServerResponse = new PerudoServerResponse(model, PerudoServerResponseEnum.IS_MAPUTO, onServerPlayer.getDices());
                        perudoServerResponse.setMessage(message);
                    } else {
                        perudoServerResponse = new PerudoServerResponse(model, PerudoServerResponseEnum.ROUND_RESULT, onServerPlayer.getDices());
                        perudoServerResponse.setMessage(message);
                    }
                } else {
                    if (newChatMessage) {
                        perudoServerResponse = new PerudoServerResponse(PerudoServerResponseEnum.NEW_CHAT_MESSAGE, chatMessages.getLast());
                    } else {
                        perudoServerResponse = new PerudoServerResponse(model, PerudoServerResponseEnum.TURN_ACCEPTED, onServerPlayer.getDices());
                    }
                }
            }
            Thread resender = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        resendChangesToClients();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            resender.start();
            try {
                resender.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//            if (model.getDeletedPlayer() != null) {
//                model.removePlayer(model.getDeletedPlayer());
//                model.setDeletedPlayer(null);
//            }
            newTurn = false;
            reloadSending = false;
            newChatMessage = false;
            model.setDeletedPlayer(null);
            saveGame();
        } else {
            perudoServerResponse = new PerudoServerResponse(model, PerudoServerResponseEnum.INVALID_BID, null);
        }
        System.out.println("perudoServerResponse = " + perudoServerResponse);
        return perudoServerResponse;
    }

    synchronized private boolean tryProceedCommand(PerudoClientCommand perudoClientCommand, WebUser webUser) {
        Player player;
        if (webUser == null) {
            player = onServerPlayer;
        } else {
            player = players.get(webUser);
        }
        if (perudoClientCommand == null) {
            return false;
        } else {
            PerudoClientCommandEnum commandEnum = perudoClientCommand.getCommand();
            switch (commandEnum) {
                case BID:
                    Pair bid = perudoClientCommand.getBid();
                    if (model.tryMakeBid(player, (int) bid.first, (int) bid.second))
                        return true;
                    else
                        return false;
                case CHAT_MESSAGE:
                    addChatMessage(new ChatMessage(player.getName(), perudoClientCommand.getMessage()));
                    newChatMessage = true;
                    return true;
                case DOUBT:
                    if (model.doubt(player)) {
                        loser = model.getCurrentBidPlayer().getName();
                    } else {
                        loser = player.getName();
                    }
                    message = loser + " loosing one dice!\n" + model.getDoubtMessage();
                    if (model.getPlayers().size() == 1) {
                        model.setGameEnded(true);
                        message += "\n" + model.getPlayers().get(0).getName() + " is the winner!";
                    }
                    newTurn = true;
                    return true;
                case LEAVE:
                    newTurn = true;
                    message = player.getName() + " left the game!";
                    model.removePlayer(player);
                    model.setCurrentTurn(0);
                    if (model.getPlayers().size() == 1) {
                        model.setGameEnded(true);
                        message += "\n" + model.getPlayers().get(0).getName() + " is the winner!";
                    }
//                    if (model.isPlayersTurn(player)) {
//                        model.forwardTurnTransition();
//                        model.getPlayers().remove(player);
//                    }
                    return true;
                case RETURN:
                    try {
                        if (webUser != null)
                            webUser.disconnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case DISCONNECT:
                    try {
                        if (webUser != null)
                            webUser.disconnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case MAPUTO:
                    model.setMaputo(true);
                    message = "Maputo round!";
                    return true;
                case NOT_MAPUTO:
                    model.setMaputo(false);
                    message = "Ordinary round!";
                case START_GAME:
                    reloadSending = true;
                    return true;
            }
        }
        return false;
    }

    private void sendChangesToOnServerPlayer() {
        if (view != null) {
            PerudoServerResponse response;
            if (reloadSending) {
                response = new PerudoServerResponse(model, chatMessages, PerudoServerResponseEnum.JOINED_PARTY, onServerPlayer.getDices());
            } else {
                if (newTurn) {
                    if (loser != null && loser.equals(onServerPlayer.getName()) && onServerPlayer.getNumberOfDices() == 1) {
                        response = new PerudoServerResponse(model, PerudoServerResponseEnum.IS_MAPUTO, onServerPlayer.getDices());
                        response.setMessage(message);
                    } else {
                        response = new PerudoServerResponse(model, PerudoServerResponseEnum.ROUND_RESULT, onServerPlayer.getDices());
                        response.setMessage(message);
                    }
                } else {
                    if (newChatMessage) {
                        response = new PerudoServerResponse(PerudoServerResponseEnum.NEW_CHAT_MESSAGE, chatMessages.getLast());
                    } else {
                        response = new PerudoServerResponse(model, PerudoServerResponseEnum.TURN_ACCEPTED, onServerPlayer.getDices());
                    }
                }
            }
            System.out.println("response = " + response);
            view.processResponse(response);
        }
    }

    private void resendChangesToClients() throws IOException {
        for (WebUser webUser : players.keySet()) {
            DataOutputStream dataOutputStream = webUser.getDataOutputStream();
            PerudoServerResponse response;
            if (reloadSending) {
                response = new PerudoServerResponse(model, chatMessages, PerudoServerResponseEnum.JOINED_PARTY, players.get(webUser).getDices());
            }
            else {
                if (newTurn) {
                    if (loser != null && loser.equals(webUser.getLogin()) && players.get(webUser).getNumberOfDices() == 1) {
                        response = new PerudoServerResponse(model, PerudoServerResponseEnum.IS_MAPUTO, players.get(webUser).getDices());
                        response.setMessage(message);
                    } else {
                        response = new PerudoServerResponse(model, PerudoServerResponseEnum.ROUND_RESULT, players.get(webUser).getDices());
                        response.setMessage(message);
                    }
                } else {
                    if (newChatMessage) {
                        response = new PerudoServerResponse(PerudoServerResponseEnum.NEW_CHAT_MESSAGE, chatMessages.getLast());
                    } else {
                        response = new PerudoServerResponse(model, PerudoServerResponseEnum.TURN_ACCEPTED, players.get(webUser).getDices());
                    }
                }
            }
            dataOutputStream.writeUTF(response.toJson());
        }
    }

    public void finish(){
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addChatMessage(ChatMessage message) {
        if (chatMessages.size() == maxChatMessages) {
            chatMessages.removeFirst();
        }
        chatMessages.addLast(message);
    }

    private String toJson() {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
        return gson.toJson(this);
    }

    public void saveGame(){
        Context context = view;
        String filename = "temp_game.json";
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write(this.toJson().getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class PerudoServerThread extends Thread {

        private WebUser webUser;

        public PerudoServerThread(WebUser webUser) {
            this.webUser = webUser;
        }

        @Override
        public void run() {
            DataInputStream dataInputStream = webUser.getDataInputStream();
            PerudoClientCommand perudoClientCommand;
            while (true) {
                try {
                    perudoClientCommand = gson.fromJson(dataInputStream.readUTF(), PerudoClientCommand.class);
                    if (perudoClientCommand != null) {
                        if ((model == null || !model.isPlayersTurn(players.get(webUser))) && perudoClientCommand.isTurnCommand()) {
                            PerudoServerResponse response = new PerudoServerResponse(model, PerudoServerResponseEnum.WRONG_TURN, players.get(webUser).getDices());
                            webUser.getDataOutputStream().writeUTF(response.toJson());
                            continue;
                        }
                        if (model.isGameEnded()) {
                            PerudoServerResponse response = new PerudoServerResponse(model, PerudoServerResponseEnum.GAME_END, onServerPlayer.getDices());
                            response.setMessage(message);
                            webUser.getDataOutputStream().writeUTF(response.toJson());
                            continue;
                        }
                        boolean stateChanged = tryProceedCommand(perudoClientCommand, webUser);
                        if (stateChanged) {
                            resendChangesToClients();
                            sendChangesToOnServerPlayer();
                            if (model.isGameEnded()) {
                                serverSocket.close();
                                return;
                            }
                            newTurn = false;
                            newChatMessage = false;
                            model.setDeletedPlayer(null);
                            saveGame();
                        } else {
                            PerudoServerResponse response = new PerudoServerResponse(model, PerudoServerResponseEnum.INVALID_BID, players.get(webUser).getDices());
                            webUser.getDataOutputStream().writeUTF(response.toJson());
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    try {
                        webUser.disconnect();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    break;
                }
            }
        }
    }
}

package com.suai.perudo.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.suai.perudo.model.Player;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.regex.Pattern;

/**
 * Created by dmitry on 11.09.18.
 */

public class PerudoClient extends Thread {

    private GsonBuilder builder = new GsonBuilder();
    private Gson gson = builder.create();

    private Socket socket;
    private Player player;

    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;

    private String inetAddress;
    private int port;
    private boolean isOffline;
    private boolean localConnect;

    public PerudoClient(String inetAddress, int port, Player player, boolean isOffline) {
        this.inetAddress = inetAddress;
        this.port = port;
        this.player = player;
        this.isOffline = isOffline;
    }

    @Override
    public void run() {
        super.run();
        try {
            int i = 0;
            byte[] bytes = new byte[4];
            String[] b = inetAddress.split(Pattern.quote("."));
            for (String s : b) {
                bytes[i] = (byte) Integer.parseInt(s);
                ++i;
            }
            InetAddress address = InetAddress.getByAddress(bytes);
            this.socket = new Socket(address, port);

            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
            if (isOffline) {
                dataOutputStream.writeUTF(player.toJson());
            }
            this.dataInputStream = new DataInputStream(socket.getInputStream());
            if (isOffline) {
                PerudoServerResponse perudoServerResponse = gson.fromJson(dataInputStream.readUTF(), PerudoServerResponse.class);
                if (perudoServerResponse.getResponseEnum().equals(PerudoServerResponseEnum.CONNECTED)) {
                    localConnect = true;
                }
                else {
                    localConnect = false;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() throws IOException {
//        thread.stop(); //TODO ExecutorService
        dataInputStream.close();
        dataOutputStream.close();
        socket.close();
    }

    public void sendCommand(PerudoClientCommand perudoClientCommand) throws IOException {
        dataOutputStream.writeUTF(perudoClientCommand.toJson());
    }

    public PerudoServerResponse getResponse() throws IOException {
        PerudoServerResponse perudoServerResponse = null;
        perudoServerResponse = gson.fromJson(dataInputStream.readUTF(), PerudoServerResponse.class);
        return perudoServerResponse;
    }

    public boolean isConnected() {
        if (socket == null)
            return false;
        else
            return socket.isConnected();
    }

    public boolean isLocalConnect() {
        return localConnect;
    }
}

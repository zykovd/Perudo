package com.suai.perudo.web;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Created by dmitry on 13.09.18.
 */

public class WebUser {

    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

//    private ObjectOutputStream objectOutputStream;
//    private ObjectInputStream objectInputStream;

    public WebUser(Socket socket) throws IOException {
        this.socket = socket;
        this.dataInputStream = new DataInputStream(socket.getInputStream());
        this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
//        this.objectInputStream = new ObjectInputStream(socket.getInputStream());
//        this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
    }

//    public ObjectOutputStream getObjectOutputStream() {
//        if (objectOutputStream == null) {
//            try {
//                this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return objectOutputStream;
//    }
//
//    public ObjectInputStream getObjectInputStream() {
//        if (objectInputStream == null) {
//            try {
//                this.objectInputStream = new ObjectInputStream(socket.getInputStream());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return objectInputStream;
//    }


    public DataInputStream getDataInputStream() {
        return dataInputStream;
    }

    public DataOutputStream getDataOutputStream() {
        return dataOutputStream;
    }


    public void disconnect() throws IOException {
//        objectOutputStream.close();
//        objectInputStream.close();
        dataInputStream.close();
        dataOutputStream.close();
        socket.close();
    }

    public boolean isConnected() {
        if (socket == null)
            return false;
        else
            return socket.isConnected();
    }
}

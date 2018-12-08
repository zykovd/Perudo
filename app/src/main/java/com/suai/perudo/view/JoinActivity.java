package com.suai.perudo.view;

import android.content.Intent;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.suai.perudo.R;
import com.suai.perudo.model.Player;
import com.suai.perudo.web.PerudoClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class JoinActivity extends AppCompatActivity implements View.OnClickListener {

    PerudoApplication perudoApplication;
    PerudoClient perudoClient;
    Player player;

    Button btnGameJoin;
    EditText editAddress;
    EditText editPort;

    TextView test;
    Toast serverWarning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        perudoApplication = (PerudoApplication) this.getApplication();

        player = perudoApplication.getPlayer();
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        editAddress = (EditText) findViewById(R.id.editJoinServer);
        editPort = (EditText) findViewById(R.id.editJoinPort);

        btnGameJoin = (Button) findViewById(R.id.btnGameJoin);
        btnGameJoin.setOnClickListener(this);

        test = (TextView) findViewById(R.id.test);
        test.setText(perudoApplication.getPlayer().getName());

        serverWarning = Toast.makeText(getApplicationContext(), "Connection error!", Toast.LENGTH_SHORT);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnGameJoin:
                if (!String.valueOf(editAddress.getText()).equals("") && !String.valueOf(editPort.getText()).equals("")) {
                    String address = editAddress.getText().toString();
                    int port = Integer.parseInt(editPort.getText().toString());
                    if (connectToServer(address, port)) {
                        Intent intent = new Intent(this, GameActivity.class);
                        intent.putExtra("isGameStarted", false);
                        startActivity(intent);
                    }
                    else {
                        serverWarning.show();
                    }
                }
                break;
        }
    }

    private boolean connectToServer(String address, int port) {
        boolean isConnected = false;
        if (perudoApplication.isOffline()) {
            try {
                //            InetAddress ipAddress;
                //            if (address.equals("192.168.1.1"))
                //                ipAddress = InetAddress.getLocalHost();
                //            else {
                //                String[] s = address.split(".");
                //                byte[] bytes = new byte[4];
                //                for (int i =0; i < 4; ++i) {
                //                    bytes[i] = Byte.parseByte(s[i]);
                //                }
                //                ipAddress = InetAddress.getByAddress(bytes);
                //            }
                perudoClient = new PerudoClient(address, port, player, true);
                perudoClient.start();
                perudoClient.join();
                perudoApplication.setPerudoClient(perudoClient);
                isConnected = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            //TODO idk what to do
        }
        return isConnected;
    }

}

package com.suai.perudo.view;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.suai.perudo.R;
import com.suai.perudo.model.Player;
import com.suai.perudo.web.PerudoClient;
import com.suai.perudo.web.PerudoServer;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    PerudoApplication perudoApplication;
    PerudoServer perudoServer;
    Player player;

    String name;
    int timeout;

    Button btnGameStart;
    Button btnRefresh;
    Button btnStartServer;
    ProgressBar progressBar;
    TextView connections;
    TextView address;
    TextView port;
    EditText editTimeout;
    EditText editPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        perudoApplication = (PerudoApplication) this.getApplication();

        player = perudoApplication.getPlayer();
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        int a = 0;
        String ip;
        if (wm != null) {
            a = wm.getConnectionInfo().getIpAddress();
            if (a == 0) {
                a = wm.getDhcpInfo().serverAddress;
            }
            int[] bytes = new int[4];
            bytes[0] = (a & 0xFF);
            bytes[1] = ((a >> 8) & 0xFF);
            bytes[2] = ((a >> 16) & 0xFF);
            bytes[3] = ((a >> 24) & 0xFF);
            ip = bytes[0] + "." + bytes[1] + "." + bytes[2] + "." + bytes[3];
        }
        else {
            ip = wifiIpAddress(this);
        }

        name = getIntent().getStringExtra("name");
        connections = (TextView) findViewById(R.id.textView3);
        btnGameStart = (Button) findViewById(R.id.btnGameStart);
        btnGameStart.setOnClickListener(this);
        btnRefresh = (Button) findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(this);
        btnStartServer = (Button) findViewById(R.id.btnStartServer);
        btnStartServer.setOnClickListener(this);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(60);
        editTimeout = (EditText) findViewById(R.id.editTimeout);
        editPort = (EditText) findViewById(R.id.editPort);
        address = (TextView) findViewById(R.id.textView4);
        address.setText(address.getText() + ip);
        port = (TextView) findViewById(R.id.textView5);

        btnStartServer.setEnabled(true);
        btnRefresh.setEnabled(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnGameStart:
                if (!String.valueOf(editTimeout.getText()).equals(""))
                    timeout = Integer.parseInt(String.valueOf(editTimeout.getText()));
                Intent intent = new Intent(this, GameActivity.class);
                //perudoServer.startGame();
                //intent.putExtra("name", name);
                intent.putExtra("isGameStarted", true);
                startActivity(intent);
                break;
            case R.id.btnStartServer:
                if (String.valueOf(editPort.getText()).equals("")) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Please choose port!", Toast.LENGTH_SHORT);
                    toast.show();
                    break;
                }
                btnStartServer.setEnabled(false);
                editPort.setEnabled(false);

                startServer(Integer.parseInt(editPort.getText().toString()));

                btnRefresh.setEnabled(true);
                break;
            case R.id.btnRefresh:
                progressBar.setProgress(perudoServer.getNumberOfPlayers() * 10);
                connections.setText(String.valueOf(perudoServer.getNumberOfPlayers()));
                break;

        }
    }

    private String wifiIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);

        int ipAddress;
        assert wifiManager != null;
        if (wifiManager.isWifiEnabled()) {
            ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        } else {
            ipAddress = wifiManager.getDhcpInfo().serverAddress;
        }

        // Convert little-endian to big-endian if needed
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

        String ipAddressString;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            ipAddressString = null;
        }

        return ipAddressString;
    }

    private void startServer(int port) {
        try {
            perudoServer = new PerudoServer(port, player);
            perudoServer.start();
            perudoApplication.setPerudoServer(perudoServer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

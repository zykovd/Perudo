package com.suai.perudo.view;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.suai.perudo.R;
import com.suai.perudo.model.Player;
import com.suai.perudo.web.PerudoClient;
import com.suai.perudo.web.PerudoClientCommand;
import com.suai.perudo.web.PerudoClientCommandEnum;
import com.suai.perudo.web.PerudoServer;
import com.suai.perudo.web.PerudoServerResponse;
import com.suai.perudo.web.PerudoServerResponseEnum;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    PerudoApplication perudoApplication;
    PerudoServer perudoServer;
    PerudoServerResponse perudoServerResponse;
    PerudoClientCommand perudoClientCommand;
    PerudoClient perudoClient;
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

    Button btnCreateParty;
    EditText editPartyTitle;

    String jsonString;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        perudoApplication = (PerudoApplication) this.getApplication();

        if (perudoApplication.isOffline()) {
            setContentView(R.layout.activity_settings);
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
            } else {
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

            FileInputStream fis = null;
            String filename = "temp_game.json";
            jsonString = null;
            Context context = SettingsActivity.this;
            try {
                fis = context.openFileInput(filename);
                int i;
                StringBuilder stringBuilder = new StringBuilder();
                while(( i = fis.read()) != -1){
                    stringBuilder.append((char)i);
                }
                jsonString = stringBuilder.toString();
                fis.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("jsonString = " + jsonString);
        }
        else {
            perudoClient = perudoApplication.getPerudoClient();
            setContentView(R.layout.activity_settings_online);
            btnCreateParty = (Button) findViewById(R.id.btnCreateParty);
            btnCreateParty.setOnClickListener(this);
            editPartyTitle = (EditText) findViewById(R.id.editPartyTitle);
        }
    }

    public void loadSavedGameDialog(String gameJson) {
        String title = "Saved game";
        String message = "Do you want to start saved game?";
        String button1String = "Yes";
        String button2String = "No";

        AlertDialog.Builder ad = new AlertDialog.Builder(SettingsActivity.this);
        ad.setTitle(title);
        ad.setMessage(message);
        ad.setPositiveButton(button1String, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.excludeFieldsWithoutExposeAnnotation().create();
                PerudoServer savedPerudoServer = gson.fromJson(jsonString, PerudoServer.class);
                try {
                    perudoServer = new PerudoServer(Integer.parseInt(editPort.getText().toString()), savedPerudoServer.getOnServerPlayer());
                    perudoServer.load(savedPerudoServer);
                    perudoServer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                perudoApplication.setPerudoServer(perudoServer);
                Intent intent = new Intent(SettingsActivity.this, GameActivity.class);
                intent.putExtra("isGameStarted", true);
                startActivity(intent);
            }
        });
        ad.setNegativeButton(button2String, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                startServer(Integer.parseInt(editPort.getText().toString()));
            }
        });
        ad.setCancelable(true);
        ad.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnGameStart:
                if (!String.valueOf(editTimeout.getText()).equals(""))
                    timeout = Integer.parseInt(String.valueOf(editTimeout.getText()));
                Intent intent = new Intent(this, GameActivity.class);
                intent.putExtra("isGameStarted", true);
                startActivity(intent);
                break;

            case R.id.btnStartServer:
                if (String.valueOf(editPort.getText()).equals("")) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Please choose port!", Toast.LENGTH_SHORT);
                    toast.show();
                    break;
                }
                if (jsonString != null) {
                    loadSavedGameDialog(jsonString);
                }
                else {
                    startServer(Integer.parseInt(editPort.getText().toString()));
                }
                btnStartServer.setEnabled(false);
                editPort.setEnabled(false);
//                startServer(Integer.parseInt(editPort.getText().toString()));
                btnRefresh.setEnabled(true);
                break;

            case R.id.btnRefresh:
                progressBar.setProgress(perudoServer.getNumberOfPlayers() * 10);
                connections.setText(String.valueOf(perudoServer.getNumberOfPlayers()));
                break;

            case R.id.btnCreateParty:
                if (String.valueOf(editPartyTitle.getText()).equals("")) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Please enter title!", Toast.LENGTH_SHORT);
                    toast.show();
                    break;
                }
                RequestAsyncTask requestAsyncTask = new RequestAsyncTask(this, String.valueOf(editPartyTitle.getText()));
                requestAsyncTask.execute();
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

    private static class RequestAsyncTask extends AsyncTask<Void, Void, Void> {
        private WeakReference<SettingsActivity> settingsActivityWeakReference;
        private ProgressDialog dialog;
        private String partyTitle;
        private boolean socketEx = false;

        RequestAsyncTask(SettingsActivity activity, String partyTitle) {
            this.settingsActivityWeakReference = new WeakReference<SettingsActivity>(activity);
            this.dialog = new ProgressDialog(activity);
            this.dialog.setCancelable(false);
            this.partyTitle = partyTitle;
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Connecting, please wait.");
            dialog.show();
        }

        protected Void doInBackground(Void... args) {
            try {
                settingsActivityWeakReference.get().perudoClient.sendCommand(new PerudoClientCommand(PerudoClientCommandEnum.NEW_PARTY, partyTitle));
                settingsActivityWeakReference.get().perudoServerResponse = settingsActivityWeakReference.get().perudoClient.getResponse();
            } catch (IOException e) {
                e.printStackTrace();
                socketEx = true;
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            if (socketEx) {
                Toast toast = Toast.makeText(settingsActivityWeakReference.get().getApplicationContext(), "Network error!", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
            if (settingsActivityWeakReference.get().perudoServerResponse.getResponseEnum().equals(PerudoServerResponseEnum.JOINED_PARTY)) {
                Intent intent = new Intent(settingsActivityWeakReference.get(), GameActivity.class);
                settingsActivityWeakReference.get().startActivity(intent);
            }
            else {
                Toast toast = Toast.makeText(settingsActivityWeakReference.get().getApplicationContext(), "Couldn't create party!", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

}

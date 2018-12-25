package com.suai.perudo.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.suai.perudo.R;
import com.suai.perudo.model.Player;
import com.suai.perudo.web.PartyHeader;
import com.suai.perudo.web.PerudoClient;
import com.suai.perudo.web.PerudoClientCommand;
import com.suai.perudo.web.PerudoClientCommandEnum;
import com.suai.perudo.web.PerudoServerResponse;
import com.suai.perudo.web.PerudoServerResponseEnum;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

public class JoinActivity extends AppCompatActivity implements View.OnClickListener {

    PerudoApplication perudoApplication;
    PerudoClient perudoClient;
    PerudoClientCommand perudoClientCommand;
    PerudoServerResponse perudoServerResponse;
    Player player;

    Button btnGameJoin;
    Button btnGetParties;
    EditText editAddress;
    EditText editPort;
    HashMap<ImageButton, PartyHeader> buttons = new HashMap<>();
    ArrayList<PartyHeader> parties;
    ArrayList<LinearLayout> partyLayouts = new ArrayList<>();

    TextView test;
    Toast serverWarning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        perudoApplication = (PerudoApplication) this.getApplication();
        perudoClient = perudoApplication.getPerudoClient();


        if (perudoApplication.isOffline()) {
            setContentView(R.layout.activity_join);

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
        else {
            setContentView(R.layout.parties_list_layout);
            btnGetParties = (Button) findViewById(R.id.btnGetParties);
            btnGetParties.setOnClickListener(this);

            preparePartiesButtons();
        }


    }

    private void preparePartiesButtons() {
        Thread sender = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    perudoClient.sendCommand(new PerudoClientCommand(PerudoClientCommandEnum.GET_PARTIES));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                PerudoServerResponse perudoServerResponse = null;
                try {
                    perudoServerResponse = perudoClient.getResponse();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                parties = perudoServerResponse.getParties();
            }
        });
        sender.start();
        try {
            sender.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Context context = JoinActivity.this;
        LinearLayout list = (LinearLayout) findViewById(R.id.partysList);
        for (LinearLayout linearLayout: partyLayouts) {
            list.removeView(linearLayout);
        }
        for (int i = 0; i < parties.size(); ++i) {
            LayoutInflater inflater = LayoutInflater.from(context);
            LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.party_layout, null, false);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.setMargins(16,16,16,0);
            layout.setLayoutParams(layoutParams);

            TextView title = (TextView) inflater.inflate(R.layout.join_title, null,false);
            title.setText(parties.get(i).getTitle());
            title.setTextColor(getResources().getColor(R.color.whiteText));
            LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams1.weight = 5;
            layoutParams1.gravity = Gravity.CENTER;
            title.setLayoutParams(layoutParams1);
            layout.addView(title);

            TextView title2 = (TextView) inflater.inflate(R.layout.join_title, null,false);
            title2.setText(String.valueOf(parties.get(i).getHash()));
            title2.setTextColor(getResources().getColor(R.color.whiteText));
            title2.setLayoutParams(layoutParams1);
            layout.addView(title2);

            ImageButton button = (ImageButton) inflater.inflate(R.layout.join_button, null,false);
            buttons.put(button, parties.get(i));
            button.setOnClickListener(this);
            layout.addView(button);

            partyLayouts.add(layout);
            list.addView(layout);
        }
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
                return;
            case R.id.btnGetParties:
                preparePartiesButtons();
                return;
        }
        PartyHeader partyHeader = buttons.get((ImageButton)v);
        perudoClientCommand = new PerudoClientCommand(PerudoClientCommandEnum.JOIN, partyHeader);
        JoinAsyncTask joinAsyncTask = new JoinAsyncTask(this);
        joinAsyncTask.execute();
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
                isConnected = perudoClient.isLocalConnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
        }
        return isConnected;
    }

    private static class JoinAsyncTask extends AsyncTask<Void, Void, Void> {
        private WeakReference<JoinActivity> joinActivityWeakReference;
        private ProgressDialog dialog;
        private boolean socketEx = false;

        JoinAsyncTask(JoinActivity activity) {
            joinActivityWeakReference = new WeakReference<JoinActivity>(activity);
            this.dialog = new ProgressDialog(activity);
            dialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Connecting, please wait.");
            dialog.show();
        }

        protected Void doInBackground(Void... args) {
            try {
                joinActivityWeakReference.get().perudoClient.sendCommand(joinActivityWeakReference.get().perudoClientCommand);
                joinActivityWeakReference.get().perudoServerResponse = joinActivityWeakReference.get().perudoClient.getResponse();
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
                Toast toast = Toast.makeText(joinActivityWeakReference.get().getApplicationContext(), "Network error!", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
            if (joinActivityWeakReference.get().perudoServerResponse.getResponseEnum().equals(PerudoServerResponseEnum.JOINED_PARTY)) {
                Intent intent = new Intent(joinActivityWeakReference.get(), GameActivity.class);
                joinActivityWeakReference.get().startActivity(intent);
            }
            else {
                Toast toast = Toast.makeText(joinActivityWeakReference.get().getApplicationContext(), "Couldn't join the party!", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
}

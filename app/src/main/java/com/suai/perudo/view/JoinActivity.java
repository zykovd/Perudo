package com.suai.perudo.view;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
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
import com.suai.perudo.web.Party;
import com.suai.perudo.web.PartyHeader;
import com.suai.perudo.web.PerudoClient;
import com.suai.perudo.web.PerudoClientCommand;
import com.suai.perudo.web.PerudoClientCommandEnum;
import com.suai.perudo.web.PerudoServerResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class JoinActivity extends AppCompatActivity implements View.OnClickListener {

    PerudoApplication perudoApplication;
    PerudoClient perudoClient;
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


        if (perudoApplication.isOffline()) { //TODO don't forget about this case
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
                PerudoServerResponse perudoServerResponse = perudoClient.getResponse();
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
            title.setText(parties.get(i).getMessage());
            LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams1.weight = 5;
            layoutParams1.gravity = Gravity.CENTER;
            title.setLayoutParams(layoutParams1);
            layout.addView(title);

            TextView title2 = (TextView) inflater.inflate(R.layout.join_title, null,false);
            title2.setText(String.valueOf(parties.get(i).getHash()));
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
        Toast.makeText(getApplicationContext(), partyHeader.getMessage(), Toast.LENGTH_SHORT).show();
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

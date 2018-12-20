package com.suai.perudo.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.suai.perudo.R;
import com.suai.perudo.web.ChatMessage;
import com.suai.perudo.web.PerudoClientCommand;
import com.suai.perudo.web.PerudoClientCommandEnum;
import com.suai.perudo.web.PerudoServerResponse;

import java.io.IOException;

/**
 * Created by dmitry on 19.12.18.
 */

public class ChatFragment extends Fragment implements View.OnClickListener {

    Button send;
    EditText messageText;
    GameActivity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.chat_fragment, container, false);
        send = (Button) v.findViewById(R.id.btnSend);
        send.setOnClickListener(this);
        messageText = (EditText) v.findViewById(R.id.editMessageText);
        activity = (GameActivity)getActivity();

        return v;
    }

    @Override
    public void onClick(View v) {
        activity.command = null;
        switch (v.getId()) {
            case R.id.btnSend:
                if (!messageText.getText().toString().equals("")) {
                    activity.command = new PerudoClientCommand(PerudoClientCommandEnum.CHAT_MESSAGE, messageText.getText().toString());
                    if (activity.onServerPlayer) {
                        PerudoServerResponse perudoServerResponse = activity.perudoServer.processOnServerPlayerCommand(activity.command);
                        activity.processResponse(perudoServerResponse);
                    } else {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (activity.onServerPlayer) {
                                    activity.perudoServer.processOnServerPlayerCommand(activity.command);
                                }
                                else {
                                    try {
                                        activity.perudoClient.sendCommand(activity.command);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }).start();
                    }
                }
                break;
        }
        messageText.setText("");
    }

    public void addChatMessageView(ChatMessage message) {
        LinearLayout list = (LinearLayout) getView().findViewById(R.id.chatLayout);

        LayoutInflater inflater2 = LayoutInflater.from(getContext());
        LinearLayout layout = (LinearLayout) inflater2.inflate(R.layout.chat_message_layout, null, false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(16,16,16,0);
        layout.setLayoutParams(layoutParams);

        Button title = (Button) inflater2.inflate(R.layout.chat_text_layout, null,false);
        title.setText(message.getSenderName());
        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams1.setMargins(16,16,16,16);
        layoutParams1.weight = 2;
//        title.setTextColor(getResources().getColor(R.color.whiteText));
        layoutParams1.gravity = Gravity.CENTER;
        title.setLayoutParams(layoutParams1);
        layout.addView(title);

        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        TextView title2 = (TextView) inflater2.inflate(R.layout.join_title, null,false);
        title2.setText(message.getMessage());
        layoutParams2.weight = 1;
        title2.setTextColor(getResources().getColor(R.color.whiteText));
        title2.setLayoutParams(layoutParams2);
        layout.addView(title2);

        list.addView(layout);
    }
}

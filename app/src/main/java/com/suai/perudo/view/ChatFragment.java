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

/**
 * Created by dmitry on 19.12.18.
 */

public class ChatFragment extends Fragment implements View.OnClickListener {

    Button send;
    EditText messageText;

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


        LinearLayout list = (LinearLayout) v.findViewById(R.id.chatLayout);
        for (int i = 0; i < 30; ++i) {
            LayoutInflater inflater2 = LayoutInflater.from(getContext());
            LinearLayout layout = (LinearLayout) inflater2.inflate(R.layout.party_layout, null, false);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.setMargins(16,16,16,0);
            layout.setLayoutParams(layoutParams);

            TextView title = (TextView) inflater2.inflate(R.layout.join_title, null,false);
            title.setText("From: " + String.valueOf(i));
            LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams1.weight = 5;
            layoutParams1.gravity = Gravity.CENTER;
            title.setLayoutParams(layoutParams1);
            layout.addView(title);

            TextView title2 = (TextView) inflater2.inflate(R.layout.join_title, null,false);
            title2.setText("Message" + String.valueOf(i));
            title2.setLayoutParams(layoutParams1);
            layout.addView(title2);

            list.addView(layout);
        }

        return v;
    }

    @Override
    public void onClick(View v) {

    }
}

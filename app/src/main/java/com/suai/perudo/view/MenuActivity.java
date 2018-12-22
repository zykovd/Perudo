package com.suai.perudo.view;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.suai.perudo.R;

public class MenuActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnStart;
    Button btnJoin;
    Button btnExit;

    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        btnStart = (Button)findViewById(R.id.btnStart);
        btnStart.setOnClickListener(this);
        btnJoin = (Button)findViewById(R.id.btnJoin);
        btnJoin.setOnClickListener(this);
        btnExit = (Button)findViewById(R.id.btnExit);
        btnExit.setOnClickListener(this);
        name = getIntent().getStringExtra("name");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStart:
                Intent intentSettings = new Intent(this, SettingsActivity.class);
                intentSettings.putExtra("name", name);
                startActivity(intentSettings);
                break;
            case R.id.btnJoin:
                Intent intentJoin = new Intent(this, JoinActivity.class);
                startActivity(intentJoin);
                break;
            case R.id.btnExit:
                finishAffinity();
                break;
        }
    }
}

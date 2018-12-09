package com.suai.perudo.view;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.suai.perudo.R;
import com.suai.perudo.model.Player;
import com.suai.perudo.web.PerudoClient;
import com.suai.perudo.web.PerudoClientCommand;
import com.suai.perudo.web.PerudoClientCommandEnum;
import com.suai.perudo.web.PerudoServerResponse;
import com.suai.perudo.web.PerudoServerResponseEnum;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    Button btnStayOffline;
    Button btnLogin;
    Button btnRegister;
    EditText editLogin;
    EditText editPassword;

    Button test1;
    Button test2;

    PerudoClientCommand perudoClientCommand;
    PerudoServerResponse perudoServerResponse;
    PerudoApplication perudoApplication;
    PerudoClient perudoClient;

    String address = "192.168.1.39";
    int port = 8080;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        perudoApplication = (PerudoApplication) this.getApplication();

        btnStayOffline = (Button)findViewById(R.id.btnOffline);
        btnStayOffline.setOnClickListener(this);
        btnLogin = (Button)findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);
        btnRegister = (Button)findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(this);
        editLogin = (EditText)findViewById(R.id.editLogin);
        editPassword = (EditText)findViewById(R.id.editPassword);

        test1 = (Button)findViewById(R.id.btnTest1);
        test1.setOnClickListener(this);
        test2 = (Button)findViewById(R.id.btnTest2);
        test2.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String login = String.valueOf(editLogin.getText());
        String password = String.valueOf(editPassword.getText());
        switch (v.getId()) {
            case R.id.btnOffline:
                if (login.equals("")) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Please, enter your nickname!", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }
                ((PerudoApplication) this.getApplication()).setPlayer(new Player(login));
                perudoApplication.setOffline(true);
                Intent intent = new Intent(this, MenuActivity.class);
                startActivity(intent);
                break;
            case R.id.btnLogin:
                if (login.equals("")) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Please, enter your login!", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }
                if (password.equals("")) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Please, enter your password!", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }
                if (perudoClient == null) {
                    perudoClient = new PerudoClient(address, port, new Player(login), false);
                    perudoApplication.setPerudoClient(perudoClient);
                    perudoClient.start();
                    try {
                        perudoClient.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                perudoClientCommand = new PerudoClientCommand(PerudoClientCommandEnum.LOGIN, login, password);
                Thread sender = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            perudoClient.sendCommand(perudoClientCommand);
                            perudoServerResponse = perudoClient.getResponse();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                sender.start();
                try {
                    sender.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (perudoServerResponse.getResponseEnum() == PerudoServerResponseEnum.AUTH_ERROR) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Auth error!", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }
                else if (perudoServerResponse.getResponseEnum() == PerudoServerResponseEnum.AUTH_SUCCESS) {
                    Intent intent2 = new Intent(this, MenuActivity.class);
                    startActivity(intent2);
                    return;
                }
                break;

            case R.id.btnRegister:
                if (login.equals("")) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Please, enter your login!", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }
                if (password.equals("")) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Please, enter your password!", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }
                if (perudoClient == null) {
                    perudoClient = new PerudoClient(address, port, new Player(login), false);
                    perudoApplication.setPerudoClient(perudoClient);
                    perudoClient.start();
                    try {
                        perudoClient.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                perudoClientCommand = new PerudoClientCommand(PerudoClientCommandEnum.REGISTER, login, password);
                Thread sender2 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            perudoClient.sendCommand(perudoClientCommand);
                            perudoServerResponse = perudoClient.getResponse();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                sender2.start();
                try {
                    sender2.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (perudoServerResponse.getResponseEnum() == PerudoServerResponseEnum.REG_ERROR) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Reg error!", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }
                else if (perudoServerResponse.getResponseEnum() == PerudoServerResponseEnum.REG_SUCCESS) {
                    Intent intent2 = new Intent(this, MenuActivity.class);
                    startActivity(intent2);
                    return;
                }
                break;

            case R.id.btnTest1:
                editLogin.setText("Dima");
                editPassword.setText("1234");
                break;

            case R.id.btnTest2:
                editLogin.setText("Vova");
                editPassword.setText("4321");
                break;
        }
    }

}

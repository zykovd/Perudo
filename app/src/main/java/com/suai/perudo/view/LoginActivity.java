package com.suai.perudo.view;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
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
import java.lang.ref.WeakReference;

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
                LoginAsyncTask loginAsyncTask = new LoginAsyncTask(this, login, password, false);
                loginAsyncTask.execute();
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
                LoginAsyncTask regAsyncTask = new LoginAsyncTask(this, login, password, true);
                regAsyncTask.execute();
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

    private static class LoginAsyncTask extends AsyncTask<Void, Void, Void> {
        private WeakReference<LoginActivity> loginActivityWeakReference;
        private ProgressDialog dialog;
        private String login;
        private String password;
        private boolean isRegistration;

        private boolean socketEx = false;

        LoginAsyncTask(LoginActivity activity, String login, String password, boolean isRegistration) {
            loginActivityWeakReference = new WeakReference<LoginActivity>(activity);
            this.login = login;
            this.password = password;
            this.isRegistration = isRegistration;
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
                if (loginActivityWeakReference.get().perudoClient == null) {
                    loginActivityWeakReference.get().perudoClient = new PerudoClient(loginActivityWeakReference.get().address, loginActivityWeakReference.get().port, new Player(login), false);
                    loginActivityWeakReference.get().perudoApplication.setPerudoClient(loginActivityWeakReference.get().perudoClient);
                    loginActivityWeakReference.get().perudoClient.start();
                    try {
                        loginActivityWeakReference.get().perudoClient.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (!loginActivityWeakReference.get().perudoClient.isConnected()) {
                    socketEx = true;
                    return null;
                }
                if (!isRegistration) {
                    loginActivityWeakReference.get().perudoClientCommand = new PerudoClientCommand(PerudoClientCommandEnum.LOGIN, login, password);
                }
                else {
                    loginActivityWeakReference.get().perudoClientCommand = new PerudoClientCommand(PerudoClientCommandEnum.REGISTER, login, password);
                }
                loginActivityWeakReference.get().perudoClient.sendCommand(loginActivityWeakReference.get().perudoClientCommand);
                loginActivityWeakReference.get().perudoServerResponse = loginActivityWeakReference.get().perudoClient.getResponse();
            }
            catch (IOException ex) {
                socketEx = true;
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            if (socketEx) {
                Toast toast = Toast.makeText(loginActivityWeakReference.get().getApplicationContext(), "Network error!", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
            if (!isRegistration) {
                if (loginActivityWeakReference.get().perudoServerResponse.getResponseEnum() == PerudoServerResponseEnum.AUTH_ERROR) {
                    Toast toast = Toast.makeText(loginActivityWeakReference.get().getApplicationContext(), "Auth error!", Toast.LENGTH_SHORT);
                    toast.show();
                } else if (loginActivityWeakReference.get().perudoServerResponse.getResponseEnum() == PerudoServerResponseEnum.AUTH_SUCCESS) {
                    Intent intent = new Intent(loginActivityWeakReference.get(), MenuActivity.class);
                    loginActivityWeakReference.get().startActivity(intent);
                }
            }
            else {
                if (loginActivityWeakReference.get().perudoServerResponse.getResponseEnum() == PerudoServerResponseEnum.REG_ERROR) {
                    Toast toast = Toast.makeText(loginActivityWeakReference.get().getApplicationContext(), "Reg error!", Toast.LENGTH_SHORT);
                    toast.show();
                }
                else if (loginActivityWeakReference.get().perudoServerResponse.getResponseEnum() == PerudoServerResponseEnum.REG_SUCCESS) {
                    Intent intent2 = new Intent(loginActivityWeakReference.get(), MenuActivity.class);
                    loginActivityWeakReference.get().startActivity(intent2);
                }
            }
        }
    }

}

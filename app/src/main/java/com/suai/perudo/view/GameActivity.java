package com.suai.perudo.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.suai.perudo.R;
import com.suai.perudo.web.PerudoClient;
import com.suai.perudo.web.PerudoClientCommand;
import com.suai.perudo.web.PerudoClientCommandEnum;
import com.suai.perudo.web.PerudoServer;
import com.suai.perudo.web.PerudoServerResponse;
import com.suai.perudo.web.PerudoServerResponseEnum;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class GameActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    Activity activity = this;
    Context context;

    ImageButton buttonBid[] = new ImageButton[6];
    int[] dices;

    Button buttonMakeBid;
    Button buttonDoubt;
    Button buttonShowDices;

    PerudoServer perudoServer;
    PerudoClient perudoClient;
    boolean onServerPlayer = false;
    boolean isGameStarted = false;

    TextView textCurrentTurn;
    TextView textPlayerName;
    TextView textPlayerNumber;
    TextView textQuantity;
    ImageButton imageButtonBid;

    TextView seekBarValue;
    SeekBar seekQuantity;

    Toast quantityWarning;
    Toast diceWarning;

    private int chosenDice;

    private PerudoClientCommand command;
    private PerudoServerResponse serverResponse;
    private ClientHandlerThread clientHandlerThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        isGameStarted = getIntent().getBooleanExtra("isGameStarted", false);
        prepareWidgets();
        prepareNet();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        context = GameActivity.this;
        String title = "Warning";
        String message = "Are you sure you want to ";
        String button1String = "Yes";
        String button2String = "No";

        switch (id) {
            case R.id.itemLeave:
                message += "leave this game?";
                command = new PerudoClientCommand(PerudoClientCommandEnum.LEAVE);
                break;
            case R.id.itemExit:
                message += "exit application?";
                command = new PerudoClientCommand(PerudoClientCommandEnum.DISCONNECT);
                break;
            default:
                command = null;
                break;
        }

        AlertDialog.Builder ad = new AlertDialog.Builder(context);
        ad.setTitle(title);
        ad.setMessage(message);
        ad.setPositiveButton(button1String, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                Thread sender = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            perudoClient.sendCommand(command);
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (command.getCommand().equals(PerudoClientCommandEnum.DISCONNECT)) {
                            finishAffinity();
                        }
                        else {
                            finish();
                        }
                    }
                });
            }
        });
        ad.setNegativeButton(button2String, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {

            }
        });
        ad.setCancelable(true);
        ad.show();

        return true;
    }

    @Override
    public void onBackPressed() {
//        String title = "Warning";
//        String message = "Are you sure you want to exit game?";
//        String button1String = "Yes";
//        String button2String = "No";
//
//        AlertDialog.Builder ad = new AlertDialog.Builder(context);
//        ad.setTitle(title);
//        ad.setMessage(message);
//        ad.setPositiveButton(button1String, new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int arg1) {
//                Thread sender = new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            perudoClient.sendCommand(command);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//                sender.start();
//                try {
//                    sender.join();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        finish();
//                    }
//                });
//            }
//        });
//        ad.setNegativeButton(button2String, new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int arg1) {
//
//            }
//        });
//        ad.setCancelable(true);
//        ad.show();
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    perudoClient.sendCommand(new PerudoClientCommand(PerudoClientCommandEnum.DISCONNECT));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        thread.start();
//        try {
//            thread.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        //todo disconnects etc
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    perudoClient.sendCommand(new PerudoClientCommand(PerudoClientCommandEnum.DISCONNECT));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        thread.start();
//        try {
//            thread.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        //todo disconnects etc
//    }

    public AlertDialog prepareGameEndAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Game over!")
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intentMenu = new Intent(GameActivity.this, MenuActivity.class);
                        startActivity(intentMenu);
                    }
                });
        return builder.create();
    }

    public void prepareNet(){
        PerudoApplication perudoApplication = (PerudoApplication)this.getApplication();
        perudoClient = perudoApplication.getPerudoClient();
        perudoServer = perudoApplication.getPerudoServer();
        if (perudoClient != null && perudoServer == null) {
            buttonMakeBid.setEnabled(false);
            buttonDoubt.setEnabled(false);
            clientHandlerThread = new ClientHandlerThread(this);
            clientHandlerThread.execute();
        }
        else if (perudoClient == null && perudoServer != null) {
            onServerPlayer = true;
            perudoServer.setView(this);
            PerudoServerResponse perudoServerResponse = perudoServer.startGame();
            processResponse(perudoServerResponse);
        }
    }

    public void prepareWidgets(){
        buttonBid[0] = (ImageButton) findViewById(R.id.buttonMakeBid1);
        buttonBid[0].setOnClickListener(this);
        buttonBid[1] = (ImageButton) findViewById(R.id.buttonMakeBid2);
        buttonBid[1].setOnClickListener(this);
        buttonBid[2] = (ImageButton) findViewById(R.id.buttonMakeBid3);
        buttonBid[2].setOnClickListener(this);
        buttonBid[3] = (ImageButton) findViewById(R.id.buttonMakeBid4);
        buttonBid[3].setOnClickListener(this);
        buttonBid[4] = (ImageButton) findViewById(R.id.buttonMakeBid5);
        buttonBid[4].setOnClickListener(this);
        buttonBid[5] = (ImageButton) findViewById(R.id.buttonMakeBid6);
        buttonBid[5].setOnClickListener(this);

        buttonMakeBid = (Button) findViewById(R.id.buttonMakeBid);
        buttonMakeBid.setOnClickListener(this);
        buttonDoubt = (Button) findViewById(R.id.buttonDoubt);
        buttonDoubt.setOnClickListener(this);
        buttonShowDices = (Button) findViewById(R.id.buttonShowDices);
        buttonShowDices.setOnClickListener(this);
        imageButtonBid = (ImageButton) findViewById(R.id.imageButtonBid);

        seekBarValue = (TextView) findViewById(R.id.seekBarValue);
        seekQuantity = (SeekBar) findViewById(R.id.seekQuantity);
        seekQuantity.setOnSeekBarChangeListener(this);
        seekQuantity.setMax(30);

        textCurrentTurn = (TextView) findViewById(R.id.textCurrentTurn);
        textPlayerName = (TextView) findViewById(R.id.textPlayerName);
        textPlayerNumber = (TextView) findViewById(R.id.textPlayerNumber);
        textQuantity = (TextView) findViewById(R.id.textQuantity);

        quantityWarning = Toast.makeText(getApplicationContext(), "Please, choose quantity!", Toast.LENGTH_SHORT);
        diceWarning = Toast.makeText(getApplicationContext(), "Please, choose dice!", Toast.LENGTH_SHORT);

//        if (!isGameStarted) {
//            Toast.makeText(getApplicationContext(), "Game is not started!", Toast.LENGTH_LONG).show();
//        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonMakeBid1:
                if (chosenDice != 1) {
                    buttonBid[0].getBackground().setColorFilter(getResources().getColor(R.color.choosenDice), PorterDuff.Mode.SRC_ATOP);
                    if (chosenDice != 0)
                        buttonBid[chosenDice - 1].getBackground().clearColorFilter();
                    chosenDice = 1;
                } else {
                    buttonBid[0].getBackground().clearColorFilter();
                    chosenDice = 0;
                }
                break;

            case R.id.buttonMakeBid2:
                if (chosenDice != 2) {
                    buttonBid[1].getBackground().setColorFilter(getResources().getColor(R.color.choosenDice), PorterDuff.Mode.SRC_ATOP);
                    if (chosenDice != 0)
                        buttonBid[chosenDice - 1].getBackground().clearColorFilter();
                    chosenDice = 2;
                } else {
                    buttonBid[1].getBackground().clearColorFilter();
                    chosenDice = 0;
                }
                break;

            case R.id.buttonMakeBid3:
                if (chosenDice != 3) {
                    buttonBid[2].getBackground().setColorFilter(getResources().getColor(R.color.choosenDice), PorterDuff.Mode.SRC_ATOP);
                    if (chosenDice != 0)
                        buttonBid[chosenDice - 1].getBackground().clearColorFilter();
                    chosenDice = 3;
                } else {
                    buttonBid[2].getBackground().clearColorFilter();
                    chosenDice = 0;
                }
                break;

            case R.id.buttonMakeBid4:
                if (chosenDice != 4) {
                    buttonBid[3].getBackground().setColorFilter(getResources().getColor(R.color.choosenDice), PorterDuff.Mode.SRC_ATOP);
                    if (chosenDice != 0)
                        buttonBid[chosenDice - 1].getBackground().clearColorFilter();
                    chosenDice = 4;
                } else {
                    buttonBid[3].getBackground().clearColorFilter();
                    chosenDice = 0;
                }
                break;

            case R.id.buttonMakeBid5:
                if (chosenDice != 5) {
                    buttonBid[4].getBackground().setColorFilter(getResources().getColor(R.color.choosenDice), PorterDuff.Mode.SRC_ATOP);
                    if (chosenDice != 0)
                        buttonBid[chosenDice - 1].getBackground().clearColorFilter();
                    chosenDice = 5;
                } else {
                    buttonBid[4].getBackground().clearColorFilter();
                    chosenDice = 0;
                }
                chosenDice = 5;
                break;

            case R.id.buttonMakeBid6:
                if (chosenDice != 6) {
                    buttonBid[5].getBackground().setColorFilter(getResources().getColor(R.color.choosenDice), PorterDuff.Mode.SRC_ATOP);
                    if (chosenDice != 0)
                        buttonBid[chosenDice - 1].getBackground().clearColorFilter();
                    chosenDice = 6;
                } else {
                    buttonBid[5].getBackground().clearColorFilter();
                    chosenDice = 0;
                }
                break;

            case R.id.buttonMakeBid:
                if (chosenDice == 0) {
                    diceWarning.show();
                    return;
                }
                if (seekQuantity.getProgress() == 0) {
                    quantityWarning.show();
                    return;
                }
                int quantity = seekQuantity.getProgress();
                int value = chosenDice;
                command = new PerudoClientCommand(PerudoClientCommandEnum.BID, quantity, value);
                if (onServerPlayer) {
                    PerudoServerResponse perudoServerResponse = perudoServer.processOnServerPlayerCommand(command);
                    processResponse(perudoServerResponse);
                }
                else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                perudoClient.sendCommand(command);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
                break;
            case R.id.buttonDoubt:
                command = new PerudoClientCommand(PerudoClientCommandEnum.DOUBT);
                if (onServerPlayer) {
                    PerudoServerResponse perudoServerResponse = perudoServer.processOnServerPlayerCommand(command);
                    processResponse(perudoServerResponse);
                }
                else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                perudoClient.sendCommand(command);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
                break;
            case R.id.buttonShowDices:
                if (dices == null)
                    break;
                ShowDicesDialog showDicesDialog = new ShowDicesDialog();
                showDicesDialog.showDialog(this, dices);
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        seekBarValue.setText(String.valueOf(seekQuantity.getProgress()));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public void processResponse(PerudoServerResponse response) {
        if (response != null) {
            serverResponse = response;
            if (response.getDices() != null)
                dices = response.getDices();
            PerudoServerResponseEnum responseEnum = response.getResponseEnum();
            switch (responseEnum) {
                case INVALID_BID:
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Invalid bid", Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                case WRONG_TURN:
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), "It's not your turn", Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                case ROUND_RESULT:
                    runOnUiThread(new Runnable() {
                        public void run() {
                            ShowDoubtDialog showDoubtDialog = new ShowDoubtDialog();
                            showDoubtDialog.showDialog(activity, serverResponse.getPlayers(), serverResponse.getMessage());
                            if (serverResponse.getPlayers().size() == 1) {
//                                AlertDialog gameEnd = prepareGameEndAlert();
//                                gameEnd.show(); //TODO Not tested
                                buttonMakeBid.setEnabled(false);
                                buttonDoubt.setEnabled(false);
                            }
                        }
                    });
                    break;
                case IS_MAPUTO:
                    AlertDialog.Builder ad = new AlertDialog.Builder(context);
                    ad.setTitle("Maputo");
                    ad.setMessage("Maputo round?");
                    ad.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
                            Thread sender = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        command = new PerudoClientCommand(PerudoClientCommandEnum.MAPUTO);
                                        perudoClient.sendCommand(command);
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
                        }
                    });
                    ad.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
                            Thread sender = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        command = new PerudoClientCommand(PerudoClientCommandEnum.NOT_MAPUTO);
                                        perudoClient.sendCommand(command);
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
                        }
                    });
                    ad.setCancelable(false);
                    ad.show();
                    break;
            }
            isGameStarted = response.isGameStarted();
            if (isGameStarted) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        buttonMakeBid.setEnabled(true);
                        buttonDoubt.setEnabled(true);
                    }
                });
            }
            textCurrentTurn.setText(response.getCurrentTurnPlayerName());
            textPlayerName.setText(response.getCurrentBidPlayerName());
            textPlayerNumber.setText(String.valueOf(response.getTotalDicesCount()));
            seekQuantity.setMax(response.getTotalDicesCount());
            textQuantity.setText(String.valueOf(response.getCurrentBidQuantity()));
            if (response.getCurrentBidQuantity() == 0) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        buttonDoubt.setEnabled(false);
                    }
                });
            }
            else {
                runOnUiThread(new Runnable() {
                    public void run() {
                        buttonDoubt.setEnabled(true);
                    }
                });
            }
            if (response.getCurrentBidValue() == 1) {
                imageButtonBid.setImageResource(R.drawable.dice1);
            } else if (response.getCurrentBidValue() == 2) {
                imageButtonBid.setImageResource(R.drawable.dice2);
            } else if (response.getCurrentBidValue() == 3) {
                imageButtonBid.setImageResource(R.drawable.dice3);
            } else if (response.getCurrentBidValue() == 4) {
                imageButtonBid.setImageResource(R.drawable.dice4);
            } else if (response.getCurrentBidValue() == 5) {
                imageButtonBid.setImageResource(R.drawable.dice5);
            } else if (response.getCurrentBidValue() == 6) {
                imageButtonBid.setImageResource(R.drawable.dice6);
            }
        }
    }

    private static class ClientHandlerThread extends AsyncTask<Void, Void, Void> {
        private WeakReference<GameActivity> gameActivityWeakReference;

        ClientHandlerThread(GameActivity activity) {
            gameActivityWeakReference = new WeakReference<GameActivity>(activity);
        }

        protected Void doInBackground(Void... args) {
            while (true) {
                PerudoServerResponse response = gameActivityWeakReference.get().perudoClient.getResponse();
                System.out.println("response = " + response);
                if (response != null) {
                    if (response.getResponseEnum().equals(PerudoServerResponseEnum.LEFT_GAME)) {
                        return null;
                    }
                    if (response.getResponseEnum().equals(PerudoServerResponseEnum.GAME_END)) {
                        gameActivityWeakReference.get().processResponse(response);
                        break;
                    } else {
                        gameActivityWeakReference.get().processResponse(response);
                    }
                }
            }
            return null;
        }
    }

}

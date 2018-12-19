package com.suai.perudo.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
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

public class GameActivity extends AppCompatActivity {

    Activity activity = this;
    Context context;

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private GameFragment gameFragment;
    private ChatFragment chatFragment;

    PerudoServer perudoServer;
    PerudoClient perudoClient;
    boolean onServerPlayer = false;
    boolean isGameStarted = false;

    private PerudoClientCommand command;
    private PerudoServerResponse serverResponse;
    private ClientHandlerThread clientHandlerThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        isGameStarted = getIntent().getBooleanExtra("isGameStarted", false);
        prepareWidgets();
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
                            if (onServerPlayer) {
                                perudoServer.processOnServerPlayerCommand(command);
                            }
                            else {
                                perudoClient.sendCommand(command);
                            }
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
                        } else {
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

    public void prepareNet() {
        PerudoApplication perudoApplication = (PerudoApplication) this.getApplication();
        perudoClient = perudoApplication.getPerudoClient();
        perudoServer = perudoApplication.getPerudoServer();
        if (perudoClient != null && perudoServer == null) {
            gameFragment.buttonMakeBid.setEnabled(false);
            gameFragment.buttonDoubt.setEnabled(false);
            clientHandlerThread = new ClientHandlerThread(this);
            clientHandlerThread.execute();
        } else if (perudoClient == null && perudoServer != null) {
            onServerPlayer = true;
            perudoServer.setView(this);
            PerudoServerResponse perudoServerResponse = perudoServer.startGame();
            processResponse(perudoServerResponse);
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        gameFragment = new GameFragment();
        adapter.addFragment(gameFragment, "Game");
        chatFragment = new ChatFragment();
        adapter.addFragment(chatFragment, "Chat");
        viewPager.setAdapter(adapter);
    }

    public void prepareWidgets() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }


        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);

//        if (!isGameStarted) {
//            Toast.makeText(getApplicationContext(), "Game is not started!", Toast.LENGTH_LONG).show();
//        }
    }



    public void processResponse(PerudoServerResponse response) {
        if (response != null) {
            serverResponse = response;
            if (response.getDices() != null)
                gameFragment.dices = response.getDices();
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
                                gameFragment.buttonMakeBid.setEnabled(false);
                                gameFragment.buttonDoubt.setEnabled(false);
                            }
                        }
                    });
                    break;
                case IS_MAPUTO:
                    AlertDialog.Builder ad = new AlertDialog.Builder(GameActivity.this);
                    ad.setTitle("Maputo");
                    ad.setMessage("Maputo round?");
                    ad.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
                            Thread sender = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        command = new PerudoClientCommand(PerudoClientCommandEnum.MAPUTO);
                                        if (onServerPlayer) {
                                            perudoServer.processOnServerPlayerCommand(command);
                                        } else {
                                            perudoClient.sendCommand(command);
                                        }
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
                                        if (onServerPlayer) {
                                            perudoServer.processOnServerPlayerCommand(command);
                                        } else {
                                            perudoClient.sendCommand(command);
                                        }
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
                        gameFragment.buttonMakeBid.setEnabled(true);
                        gameFragment.buttonDoubt.setEnabled(true);
                    }
                });
            }
            gameFragment.textCurrentTurn.setText(response.getCurrentTurnPlayerName());
            gameFragment.textPlayerName.setText(response.getCurrentBidPlayerName());
            gameFragment.textPlayerNumber.setText(String.valueOf(response.getTotalDicesCount()));
            gameFragment.seekQuantity.setMax(response.getTotalDicesCount());
            gameFragment.textQuantity.setText(String.valueOf(response.getCurrentBidQuantity()));
            if (response.getCurrentBidQuantity() == 0) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        gameFragment.buttonDoubt.setEnabled(false);
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    public void run() {
                        gameFragment.buttonDoubt.setEnabled(true);
                    }
                });
            }
            if (response.getCurrentBidValue() == 1) {
                gameFragment.imageButtonBid.setImageResource(R.drawable.dice1);
            } else if (response.getCurrentBidValue() == 2) {
                gameFragment.imageButtonBid.setImageResource(R.drawable.dice2);
            } else if (response.getCurrentBidValue() == 3) {
                gameFragment.imageButtonBid.setImageResource(R.drawable.dice3);
            } else if (response.getCurrentBidValue() == 4) {
                gameFragment.imageButtonBid.setImageResource(R.drawable.dice4);
            } else if (response.getCurrentBidValue() == 5) {
                gameFragment.imageButtonBid.setImageResource(R.drawable.dice5);
            } else if (response.getCurrentBidValue() == 6) {
                gameFragment.imageButtonBid.setImageResource(R.drawable.dice6);
            }
        }
    }

//    @Override
//    public void onCommandSelected(PerudoClientCommand clientCommand) {
//        if (clientCommand != null) {
//            if (onServerPlayer) {
//                PerudoServerResponse perudoServerResponse = perudoServer.processOnServerPlayerCommand(command);
//                processResponse(perudoServerResponse);
//            } else {
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            perudoClient.sendCommand(command);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }).start();
//            }
//        }
//    }

    private static class ClientHandlerThread extends AsyncTask<Void, Void, Void> {
        private WeakReference<GameActivity> gameActivityWeakReference;

        ClientHandlerThread(GameActivity activity) {
            gameActivityWeakReference = new WeakReference<GameActivity>(activity);
        }

        protected Void doInBackground(Void... args) {
            try {
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
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
            return null;
        }
    }

}

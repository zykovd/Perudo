package com.suai.perudo.view;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.suai.perudo.R;
import com.suai.perudo.web.PerudoClientCommand;
import com.suai.perudo.web.PerudoClientCommandEnum;
import com.suai.perudo.web.PerudoServerResponse;

import java.io.IOException;

/**
 * Created by dmitry on 19.12.18.
 */

public class GameFragment extends Fragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    ImageButton buttonBid[] = new ImageButton[6];
    int[] dices;

    Button buttonMakeBid;
    Button buttonDoubt;
    Button buttonShowDices;

    TextView textCurrentTurn;
    TextView textPlayerName;
    TextView textPlayerNumber;
    TextView textQuantity;
    ImageButton imageButtonBid;

    TextView seekBarValue;
    SeekBar seekQuantity;

    Toast quantityWarning;
    Toast diceWarning;

    int chosenDice;

    PerudoClientCommand command;

    GameActivity gameActivity;

    public int[] getDices() {
        return dices;
    }

    public void setDices(int[] dices) {
        this.dices = dices;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.game_fragment, container, false);
        buttonBid[0] = (ImageButton) v.findViewById(R.id.buttonMakeBid1);
        buttonBid[0].setOnClickListener(this);
        buttonBid[1] = (ImageButton) v.findViewById(R.id.buttonMakeBid2);
        buttonBid[1].setOnClickListener(this);
        buttonBid[2] = (ImageButton) v.findViewById(R.id.buttonMakeBid3);
        buttonBid[2].setOnClickListener(this);
        buttonBid[3] = (ImageButton) v.findViewById(R.id.buttonMakeBid4);
        buttonBid[3].setOnClickListener(this);
        buttonBid[4] = (ImageButton) v.findViewById(R.id.buttonMakeBid5);
        buttonBid[4].setOnClickListener(this);
        buttonBid[5] = (ImageButton) v.findViewById(R.id.buttonMakeBid6);
        buttonBid[5].setOnClickListener(this);

        buttonMakeBid = (Button) v.findViewById(R.id.buttonMakeBid);
        buttonMakeBid.setOnClickListener(this);
        buttonDoubt = (Button) v.findViewById(R.id.buttonDoubt);
        buttonDoubt.setOnClickListener(this);
        buttonShowDices = (Button) v.findViewById(R.id.buttonShowDices);
        buttonShowDices.setOnClickListener(this);
        imageButtonBid = (ImageButton) v.findViewById(R.id.imageButtonBid);

        seekBarValue = (TextView) v.findViewById(R.id.seekBarValue);
        seekQuantity = (SeekBar) v.findViewById(R.id.seekQuantity);
        seekQuantity.setOnSeekBarChangeListener(this);
        seekQuantity.setMax(30);

        textCurrentTurn = (TextView) v.findViewById(R.id.textCurrentTurn);
        textPlayerName = (TextView) v.findViewById(R.id.textPlayerName);
        textPlayerNumber = (TextView) v.findViewById(R.id.textPlayerNumber);
        textQuantity = (TextView) v.findViewById(R.id.textQuantity);

        quantityWarning = Toast.makeText(getActivity(), "Please, choose quantity!", Toast.LENGTH_SHORT);
        diceWarning = Toast.makeText(getActivity(), "Please, choose dice!", Toast.LENGTH_SHORT);

        gameActivity = ((GameActivity)getActivity());
        gameActivity.prepareNet();
        return v;
    }

    @Override
    public void onClick(View v) {
        command = null;
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
                if (gameActivity.onServerPlayer) {
                    PerudoServerResponse perudoServerResponse = gameActivity.perudoServer.processOnServerPlayerCommand(command);
                    gameActivity.processResponse(perudoServerResponse);
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                gameActivity.perudoClient.sendCommand(command);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
                break;
            case R.id.buttonDoubt:
                command = new PerudoClientCommand(PerudoClientCommandEnum.DOUBT);
                if (gameActivity.onServerPlayer) {
                    PerudoServerResponse perudoServerResponse = gameActivity.perudoServer.processOnServerPlayerCommand(command);
                    gameActivity.processResponse(perudoServerResponse);
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                gameActivity.perudoClient.sendCommand(command);
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
                showDicesDialog.showDialog(getActivity(), dices);
                break;
        }
    }

}

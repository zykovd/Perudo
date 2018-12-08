package com.suai.perudo.view;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.suai.perudo.R;

/**
 * Created by dmitry on 09.10.18.
 */

public class ShowDicesDialog {

    public void showDialog(Activity activity, int[] dices){
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.setTitle("Your dices:");
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_dices_layout);

        GridLayout gridLayout = (GridLayout) dialog.findViewById(R.id.dialogLayout);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.bottomMargin = 10;
        layoutParams.topMargin = 10;
        layoutParams.leftMargin = 10;
        layoutParams.rightMargin = 10;

        for (int i = 0; i < 6; ++i) {
            for (int j = 0; j < dices[i]; ++j) {
                ImageView dice = new ImageView(activity);
                if (i == 0) {
                    dice.setImageResource(R.drawable.dice1);
                }
                else if (i == 1) {
                    dice.setImageResource(R.drawable.dice2);
                }
                else if (i == 2) {
                    dice.setImageResource(R.drawable.dice3);
                }
                else if (i == 3) {
                    dice.setImageResource(R.drawable.dice4);
                }
                else if (i == 4) {
                    dice.setImageResource(R.drawable.dice5);
                }
                else if (i == 5) {
                    dice.setImageResource(R.drawable.dice6);
                }
                dice.setLayoutParams(layoutParams);
                gridLayout.addView(dice);
            }
        }

        Button dialogButton = (Button) dialog.findViewById(R.id.buttonOK);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
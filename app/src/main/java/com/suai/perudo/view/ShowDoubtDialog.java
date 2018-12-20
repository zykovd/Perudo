package com.suai.perudo.view;

import android.app.Activity;
import android.app.Dialog;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.suai.perudo.R;
import com.suai.perudo.model.Player;

import java.util.ArrayList;

/**
 * Created by dmitry on 18.11.18.
 */

public class ShowDoubtDialog {

    public void showDialog(Activity activity, ArrayList<Player> players, String message){
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);

        dialog.setContentView(R.layout.dialog_doubt_layout);
        LinearLayout linearLayout = (LinearLayout) dialog.findViewById(R.id.dialogDoubtLinearLayout);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
//        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        TextView textView = new TextView(activity);
        textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        textView.setText(message);
        textView.setTextSize(24);
        textView.setMovementMethod(new ScrollingMovementMethod());
//        textView.setGravity(Gravity.CENTER);
        linearLayout.addView(textView);

        for (Player player: players) {
            TextView textView2 = new TextView(activity);
            textView2.setText(player.getName());
            textView2.setTextSize(24);
            textView2.setGravity(Gravity.CENTER);
            linearLayout.addView(textView2);

            GridLayout gridLayout = new GridLayout(activity);
            gridLayout.setColumnCount(3);
            linearLayout.addView(gridLayout);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.bottomMargin = 10;
            layoutParams.topMargin = 10;
            layoutParams.leftMargin = 10;
            layoutParams.rightMargin = 10;
            for (int i = 0; i < 6; ++i) {
                for (int j = 0; j < player.getPreviousDices()[i]; ++j) {
                    ImageView dice = new ImageView(activity);
                    if (i == 0) {
                        dice.setImageResource(R.drawable.dice1);
                    } else if (i == 1) {
                        dice.setImageResource(R.drawable.dice2);
                    } else if (i == 2) {
                        dice.setImageResource(R.drawable.dice3);
                    } else if (i == 3) {
                        dice.setImageResource(R.drawable.dice4);
                    } else if (i == 4) {
                        dice.setImageResource(R.drawable.dice5);
                    } else if (i == 5) {
                        dice.setImageResource(R.drawable.dice6);
                    }
                    dice.setLayoutParams(layoutParams);
                    gridLayout.addView(dice);
                }
            }
        }

        Button dialogButton = (Button) dialog.findViewById(R.id.buttonDoubtOK);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

}

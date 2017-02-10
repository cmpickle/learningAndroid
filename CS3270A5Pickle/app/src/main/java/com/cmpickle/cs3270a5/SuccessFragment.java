package com.cmpickle.cs3270a5;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * @author Cameron Pickle
 *         Copyright (C) Cameron Pickle (cmpickle) on 2/9/2017.
 */

public class SuccessFragment extends DialogFragment {

    public SuccessFragment() {
        //mandatory empty public constructor
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Dialog dialog = builder.setMessage(R.string.you_were_able_to_make_the_correct_change)
                .setCancelable(false)
                .setTitle(R.string.you_did_it)
                .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();
        return dialog;

    }
}

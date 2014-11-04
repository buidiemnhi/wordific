package com.evilgeniustechnologies.Wordrific.utilties;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by vendetta on 5/30/14.
 */
public class DawnDialog extends DialogFragment {
    public static final String MESSAGE = "message";
    public static final String TITLE = "title";
    private String message;
    private String title;
    private String buttonLabel;

    public DawnDialog() {}

    public void onClicked() {
        // Caller overrides here
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        message = args.getString(MESSAGE);
        title = args.getString(TITLE);
        buttonLabel = getActivity().getString(android.R.string.ok);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(buttonLabel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        onClicked();
                    }
                });
        setCancelable(false);
        // Retain when rotate
        setRetainInstance(true);
        return builder.create();
    }
}

package com.example.myapplication;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class SimpleAlertDialog extends DialogFragment {

    private static final String TITLE_KEY = "title_key";
    private static final String MESSAGE_KEY = "message_key";

    public SimpleAlertDialog() {
    }

    public static SimpleAlertDialog newInstance(String title, String message) {
        Bundle args = new Bundle();
        args.putString(TITLE_KEY, title);
        args.putString(MESSAGE_KEY, message);

        SimpleAlertDialog fragment = new SimpleAlertDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args == null) {
            throw new AssertionError("Arguments cannot be null.");
        }

        String title = args.getString(TITLE_KEY);
        String message = args.getString(MESSAGE_KEY);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle positive button click if needed
                    }
                });

        return builder.create();
    }
}

package com.rafao.codescanner.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.rafao.codescanner.R;


public class DialogUtils {

    public static AlertDialog createSingleButtonDialog(
            Context context,
            String message,
            String positiveLabel,
            DialogInterface.OnClickListener positiveListener) {

        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_confirmation, null);

        final TextView textviewTitle = view.findViewById(R.id.textview_message);
        textviewTitle.setTextColor(ContextCompat.getColor(context, R.color.font_grey_main));
        textviewTitle.setText(message);

        return new AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(false)
                .setPositiveButton(positiveLabel, positiveListener)
                .create();
    }

    public static AlertDialog createDoubleButtonDialog(
            Context context,
            String message,
            String positiveLabel,
            DialogInterface.OnClickListener positiveListener,
            String negativeLabel,
            DialogInterface.OnClickListener negativeListener) {

        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_confirmation, null);

        final TextView textviewTitle = view.findViewById(R.id.textview_message);
        textviewTitle.setTextColor(ContextCompat.getColor(context, R.color.font_grey_main));
        textviewTitle.setText(message);

        return new AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(false)
                .setPositiveButton(positiveLabel, positiveListener)
                .setNegativeButton(negativeLabel, negativeListener)
                .create();
    }

    public static AlertDialog createInputDialog(
            Context context,
            String message,
            String hint,
            String positiveLabel,
            DialogInterface.OnClickListener positiveListener,
            String negativeLabel,
            DialogInterface.OnClickListener negativeListener) {

        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_input, null);

        final TextView textviewTitle = view.findViewById(R.id.textview_title);
        textviewTitle.setTextColor(ContextCompat.getColor(context, R.color.font_grey_main));
        textviewTitle.setText(message);

        final EditText edittextContent = view.findViewById(R.id.edittext_content);
        textviewTitle.setHintTextColor(ContextCompat.getColor(context, R.color.font_grey_secondary));
        edittextContent.setHint(hint);
        textviewTitle.setTextColor(ContextCompat.getColor(context, R.color.font_grey_main));

        return new AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(false)
                .setPositiveButton(positiveLabel, positiveListener)
                .setNegativeButton(negativeLabel, negativeListener)
                .create();
    }
}

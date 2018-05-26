package com.colaorange.dailymoney.core.util;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.view.LayoutInflater;
import android.view.View;

import com.colaorange.commons.util.Strings;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;

import java.util.List;
import java.util.Set;

/**
 * Utitls to help use handling guis opersation, make sure all api are using in GUI scope.
 *
 * @author dennis
 */
public class Dialogs {


    public static final int OK_BUTTON = AlertDialog.BUTTON_POSITIVE;
    public static final int CANCEL_BUTTON = AlertDialog.BUTTON_NEGATIVE;

    static public void showTextEditor(Context context, @Nullable String title,
                                 @Nullable String msg, int inputType, @Nullable String text, final OnFinishListener listener) {

        I18N i18n = Contexts.instance().getI18n();
        String okText = i18n.string(R.string.act_ok);
        String cancelText = Contexts.instance().getI18n().string(R.string.act_cancel);

        AlertDialog.Builder b = new AlertDialog.Builder(context);
        b.setTitle(title);
        b.setMessage(msg);

        // Set up the input

        View view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.guis_text_input, null, false);

        final AppCompatEditText input = view.findViewById(R.id.guis_input);

        input.setInputType(inputType);

        input.setText(text);

        b.setView(view);

        // Set up the buttons
        b.setPositiveButton(okText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String title = input.getText().toString();
                listener.onFinish(which, title);
            }
        });
        b.setNegativeButton(cancelText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        b.show();
    }

    static public void showListSelector(Context context, @Nullable String title,
                                        @Nullable String msg, List<?> value, @Nullable List<String> label,
                                        Set<?> selection, boolean multiple, final OnFinishListener listener) {

        I18N i18n = Contexts.instance().getI18n();
        String okText = i18n.string(R.string.act_ok);
        String cancelText = Contexts.instance().getI18n().string(R.string.act_cancel);

        AlertDialog.Builder b = new AlertDialog.Builder(context);
        b.setTitle(title);
        b.setMessage(msg);

        // Set up the input

//        View view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.guis_text_input, null, false);
//
//        final AppCompatEditText input = view.findViewById(R.id.guis_input);
//
//        input.setInputType(inputType);
//
//        input.setText(text);
//        b.setView(view);

        // Set up the buttons
        b.setPositiveButton(okText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                String title = input.getText().toString();
                listener.onFinish(which, null);
            }
        });
        b.setNegativeButton(cancelText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        b.show();
    }

    public interface OnFinishListener {
        boolean onFinish(int which, Object data);
    }
}

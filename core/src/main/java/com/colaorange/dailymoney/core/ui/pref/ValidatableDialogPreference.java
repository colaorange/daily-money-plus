package com.colaorange.dailymoney.core.ui.pref;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.colaorange.commons.util.Strings;
import com.colaorange.dailymoney.core.R;

/**
 * @author Dennis
 */
public abstract class ValidatableDialogPreference extends DialogPreference {


    public ValidatableDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    abstract public void onPrepareDialogMember(View view);
    abstract public void onClearDialogMember();
    abstract public void onCloseDialog(boolean positiveResult);
    abstract public boolean onValidation();

    @Override
    public void onBindDialogView(View view){
        super.onBindDialogView(view);
        onPrepareDialogMember(view);
    }

    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        final AlertDialog dlg = (AlertDialog)getDialog();
        dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onValidation()){
                    ValidatableDialogPreference.this.onClick(dlg, AlertDialog.BUTTON_POSITIVE);
                    dlg.dismiss();
                }
            }
        });
    }


    @Override
    protected void onDialogClosed(boolean positiveResult) {
        onCloseDialog(positiveResult);
        onClearDialogMember();
    }
}

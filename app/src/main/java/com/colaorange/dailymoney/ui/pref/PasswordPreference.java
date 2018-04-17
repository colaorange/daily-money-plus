package com.colaorange.dailymoney.ui.pref;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.colaorange.commons.util.Security;
import com.colaorange.commons.util.Strings;
import com.colaorange.dailymoney.R;
import com.colaorange.dailymoney.context.Preference;
import com.colaorange.dailymoney.util.GUIs;

/**
 * Created by Dennis
 */
public class PasswordPreference extends ValidatableDialogPreference {

    Switch enable;
    TextView pwd;
    TextView pwdvd;

    String passwordHash;

    public PasswordPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.pwd_preference);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        setDialogIcon(null);
    }

    @Override
    public void onPrepareDialogMember(View view) {
        enable = view.findViewById(R.id.pwd_preference_enable);
        pwd = view.findViewById(R.id.pwd_preference_pwd);
        pwdvd = view.findViewById(R.id.pwd_preference_pwdvd);
        String pwdhash = getPersistedString("");
        System.out.println(">>>>getPersistedString>"+pwdhash);
        boolean hasPwd = !Strings.isEmpty(pwdhash);

        enable.setChecked(hasPwd);
        pwd.setEnabled(hasPwd);
        pwdvd.setEnabled(hasPwd);


        enable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                pwd.setEnabled(isChecked);
                pwd.setText("");
                pwdvd.setEnabled(isChecked);
                pwdvd.setText("");
            }
        });
    }

    @Override
    public void onClearDialogMember() {
        enable = null;
        pwd = null;
        pwdvd = null;
    }

    @Override
    public void onCloseDialog(boolean positiveResult) {
        if(positiveResult){
            persistString(passwordHash);
            System.out.println(">>>>>>>>>>"+passwordHash);
        }
    }

    @Override
    public boolean onValidation() {
        boolean enabled = this.enable.isChecked();
        if(enabled) {
            String pwd = this.pwd.getText().toString().trim();
            String pwdvd = this.pwdvd.getText().toString().trim();
            if (pwd.isEmpty() || !pwd.equals(pwdvd)) {
                GUIs.alert(getContext(), R.string.msg_wrong_password_validation);
                return false;
            }

            passwordHash = Security.md5String(pwd + Preference.PWD_SALT);
        }else{
            passwordHash = "";
        }
        return true;
    }
}

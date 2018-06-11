package com.colaorange.dailymoney.core.ui.pref;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.colaorange.commons.util.Strings;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Preference;
import com.colaorange.dailymoney.core.ui.GUIs;

/**
 * @author Dennis
 */
public class PasswordPreference extends ValidatableDialogPreference {

    Switch enable;
    TextView pwd;
    TextView pwdvd;

    String passwordHash;

    public PasswordPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.password_preference);
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
        }
    }

    @Override
    public boolean onValidation() {
        boolean enabled = this.enable.isChecked();
        if(enabled) {
            String pwd = this.pwd.getText().toString();
            String pwdvd = this.pwdvd.getText().toString();
            if (pwd.isEmpty() || !pwd.equals(pwdvd)) {
                GUIs.alert(getContext(), R.string.msg_wrong_password_validation);
                return false;
            }
            passwordHash = Preference.passwordMD5(pwd);
        }else{
            passwordHash = "";
        }
        return true;
    }
}

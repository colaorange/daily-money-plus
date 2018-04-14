package com.colaorange.dailymoney.ui;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.colaorange.dailymoney.util.GUIs;
import com.colaorange.dailymoney.context.ContextsActivity;
import com.colaorange.dailymoney.R;

/**
 * @author dennis
 */
public class PasswordProtectionActivity extends ContextsActivity implements OnClickListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pwd_protection);
        findViewById(R.id.pwd_protection_ok).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.pwd_protection_ok) {
            doPasswordOk();
        }
    }

    private void doPasswordOk() {
        String password = preference().getPassword();
        String pd = ((TextView) findViewById(R.id.pwd_protection_text)).getText().toString();
        if (password.equals(pd)) {
            setResult(RESULT_OK);
            finish();
        } else {
            GUIs.shortToast(this, R.string.msg_wrong_password);
        }
    }
}

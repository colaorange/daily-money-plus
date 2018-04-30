package com.colaorange.dailymoney.core.ui;

import android.content.Intent;
import android.os.Bundle;

import com.colaorange.commons.util.Strings;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.bg.StartupReceiver;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.InstanceState;
import com.colaorange.dailymoney.core.data.DataCreator;
import com.colaorange.dailymoney.core.data.IDataProvider;
import com.colaorange.dailymoney.core.ui.legacy.DesktopActivity;
import com.colaorange.dailymoney.core.util.GUIs;

/**
 * Created by Dennis
 */
@InstanceState
public class StartupActivity extends ContextsActivity {

    public static final String PARAM_FIRST_TIME = "startup.firstTime";

    @InstanceState
    private boolean firstTime = false;

    @InstanceState
    private boolean started = false;

    @Override
    public boolean isNoActionBarTheme(){
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.startup);
    }
    @Override
    public void onStart() {
        super.onStart();
        if (isFinishing()) {
            return;
        }
        if (contexts().getAndSetFirstTime()) {
            doFirstTime();
            firstTime = true;
        }

        //notify app is startup
        Intent intent = new Intent();
        intent.setAction(StartupReceiver.ACTION_STARTUP);
        sendBroadcast(intent);

        trackEvent("startup");
        if(!firstTime){
            trackEvent("theme-"+preference().getTheme());
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (isFinishing()) {
            return;
        }
        if (handleProtection()) {
            return;
        }
        doNextActivity();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        finish();
    }


    /**
     * @return true if handled
     */
    private boolean handleProtection() {
        final String passwordHash = preference().getPasswordHash();
        if (Strings.isBlank(passwordHash)) {
            return false;
        }
        Intent intent = null;
        intent = new Intent(this, PasswordProtectionActivity.class);
        startActivityForResult(intent, Constants.REQUEST_PASSWORD_PROTECTION_CODE);
        trackEvent("protection");
        return true;
    }

    private void doNextActivity() {
        Intent intent = new Intent(StartupActivity.this, DesktopActivity.class);
        intent.putExtra(PARAM_FIRST_TIME, firstTime);
        startActivity(intent);
        started = true;
    }


    private void doFirstTime() {
        IDataProvider idp = contexts().getDataProvider();
        if (idp.listAccount(null).size() == 0) {//just in case
            new DataCreator(idp, i18n()).createDefaultAccount();
        }
        GUIs.longToast(this, R.string.msg_firsttime_use_hint);
        trackEvent("first_time");
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {
        if (requestCode == Constants.REQUEST_PASSWORD_PROTECTION_CODE) {
            if (resultCode == RESULT_OK) {
                doNextActivity();
            }
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}

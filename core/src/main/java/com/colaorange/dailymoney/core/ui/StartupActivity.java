package com.colaorange.dailymoney.core.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.colaorange.commons.util.Strings;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.bg.StartupJobSchedulerFacade;
import com.colaorange.dailymoney.core.bg.StartupReceiver;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.InstanceState;
import com.colaorange.dailymoney.core.data.DataCreator;
import com.colaorange.dailymoney.core.data.IDataProvider;
import com.colaorange.dailymoney.core.data.DefaultCardDesktopCreator;
import com.colaorange.dailymoney.core.ui.cards.CardDesktopActivity;
import com.colaorange.dailymoney.core.ui.nav.NavPage;
import com.colaorange.dailymoney.core.ui.nav.NavPageFacade;
import com.colaorange.dailymoney.core.util.Logger;

/**
 * @author Dennis
 */
@InstanceState
public class StartupActivity extends ContextsActivity {

    public static final String ARG_FIRST_TIME = "startup.firstTime";

    @InstanceState
    private boolean firstTime = false;

    @InstanceState
    private boolean started = false;

    @Override
    public boolean isNoActionBarTheme() {
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.startup);
        if (started) {
            finish();
        }
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

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Logger.d(">> sending startup broadcast from activity");
            Intent intent = new Intent();
            intent.setAction(StartupReceiver.ACTION_STARTUP);
            sendBroadcast(intent);
        }else{
            //android 5+
            Logger.d(">> trying startup job scheduler from activity ");
            StartupJobSchedulerFacade.startup(this);
        }

        trackEvent(TE.STARTUP);
        if (!firstTime) {
            trackEvent(TE.THEME + preference().getTheme());
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
        trackEvent(TE.PROTECT);
        return true;
    }

    private void doNextActivity() {
        String action = getIntent().getAction();
        if(Constants.ACTION_CREATE_RECORD.equals(action)){
            new NavPageFacade(this).doPage(NavPage.RECORD_EDITOR);
        }else{
            Intent intent = new Intent(this, CardDesktopActivity.class);
            intent.putExtra(ARG_FIRST_TIME, firstTime);
            startActivity(intent);
        }
        started = true;
        firstTime = false;
    }


    private void doFirstTime() {

        if (!Contexts.instance().getPreference().isAnyDesktop()) {
            new DefaultCardDesktopCreator().createForWholeNew(false);
        }


        IDataProvider idp = contexts().getDataProvider();
        if (idp.listAccount(null).size() == 0) {//just in case
            new DataCreator(idp, i18n()).createDefaultAccount();
        }
        GUIs.longToast(this, R.string.msg_firsttime_use_hint);
        trackEvent(TE.FIRST_TIME);
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

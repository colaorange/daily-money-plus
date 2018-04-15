package com.colaorange.dailymoney.ui;

import android.content.Intent;

import com.colaorange.dailymoney.util.GUIs;
import com.colaorange.dailymoney.R;
import com.colaorange.dailymoney.context.ContextsActivity;
import com.colaorange.dailymoney.data.DataCreator;
import com.colaorange.dailymoney.data.IDataProvider;
import com.colaorange.dailymoney.ui.legacy.DesktopActivity;

/**
 * Created by Dennis
 */
public class StartupActivity extends ContextsActivity {

    public static final String PARAM_FIRST_TIME = "startup.firstTime";
    private boolean passedProtection = false;
    private boolean firstTime = false;

    private static final String startupAction = "com.colaorange.broadcast.Startup";

    @Override
    public void onStart(){
        super.onStart();
        if(isFinishing()) {
            return;
        }
        if(contexts().getAndSetFirstTime()){
            doFirstTime();
            firstTime = true;
        }

        Intent intent = new Intent();
        intent.setAction(startupAction);
        sendBroadcast(intent);

        trackEvent("startup");
    }

    @Override
    public void onResume(){
        super.onResume();
        if(isFinishing()) {
            return;
        }
        if(handleProtection()){
            return;
        }
        if(!passedProtection){
            finish();
        }

        doNextActivity();
    }

    @Override
    public void onRestart(){
        super.onRestart();
        //for the desktop activity back
        finish();
    }


    /**
     *
     * @return true if handling
     */
    private boolean handleProtection() {
        final String password = preference().getPassword();
        if("".equals(password)||passedProtection){
            return false;
        }
        Intent intent = null;
        intent = new Intent(this,PasswordProtectionActivity.class);
        startActivityForResult(intent, Constants.REQUEST_PASSWORD_PROTECTION_CODE);
        trackEvent("protection");
        return true;
    }

    private void doNextActivity(){
        Intent intent = new Intent(StartupActivity.this, DesktopActivity.class);
        intent.putExtra(PARAM_FIRST_TIME, firstTime);
        startActivity(intent);
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
                                 Intent data){
        if(requestCode == Constants.REQUEST_PASSWORD_PROTECTION_CODE){
            if(resultCode!=RESULT_OK){
                finish();
            }else{
                passedProtection = true;
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}

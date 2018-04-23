package com.colaorange.dailymoney.core.context;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.util.Logger;

/**
 * provide life cycle and easy access to contexts
 *
 * @author dennis
 */
public class ContextsActivity extends AppCompatActivity {

    public static final String PARAM_TITLE = "activity.title";


    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        //todo , from config
        getTheme().applyStyle(R.style.textSizeLarge, true);
        Logger.d("activity created:" + this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        for (int g : grantResults) {
            if (g == PackageManager.PERMISSION_GRANTED) {
                //simply reload this activie
                makeRestart();
                break;
            }
        }
    }

    protected void makeRestart() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    public void trackEvent(String action) {
        Contexts ctxs = Contexts.instance();
        ctxs.trackEvent(Contexts.getTrackerPath(getClass()), action, "", null);
    }

    protected I18N i18n(){
        return contexts().getI18n();
    }

    protected CalendarHelper calendarHelper(){
        return preference().getCalendarHelper();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.d("activity destroyed:" + this);
    }


    @Override
    protected void onResume() {
        super.onResume();

        Bundle b = getIntentExtras();
        String t = b.getString(PARAM_TITLE);
        if (t != null) {
            setTitle(t);
        }
    }


    Bundle fakeExtra;

    protected Bundle getIntentExtras() {
        if (getIntent() != null && getIntent().getExtras() != null) {
            return getIntent().getExtras();
        }
        // if extra is null;
        if (fakeExtra == null) {
            fakeExtra = new Bundle();
        }
        return fakeExtra;
    }

    protected Contexts contexts() {
        return Contexts.instance();
    }

    protected Preference preference(){
        return contexts().getPreference();
    }


}

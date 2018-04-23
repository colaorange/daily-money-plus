package com.colaorange.dailymoney.core.context;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
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
        applyTheme();//do before super on create;
        super.onCreate(bundle);

        Logger.d("activity created:" + this);
    }

    protected void restartApp(boolean passedProtection){
        //TODO
        Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage( getBaseContext().getPackageName() );
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        i = (Intent) i.clone();
//        String bypassId = Strings.randomUUID();
//        i.putExtra(StartupActivity.PARAM_BYPASS_PROTECTION,true);

        startActivity(i);
    }

    private void applyTheme() {
        Resources.Theme theme = getTheme();

        boolean light = isLightTheme();

        if (light) {

            if (isDialogTheme()) {
                theme.applyStyle(android.support.v7.appcompat.R.style.Theme_AppCompat_Light_Dialog, true);
            } else {
                theme.applyStyle(android.support.v7.appcompat.R.style.Theme_AppCompat_Light, true);
            }
        } else {

            if (isDialogTheme()) {
                theme.applyStyle(android.support.v7.appcompat.R.style.Theme_AppCompat_Dialog, true);
            } else {
                theme.applyStyle(android.support.v7.appcompat.R.style.Theme_AppCompat, true);
            }
        }

        if (isNoActionBarTheme()) {
            theme.applyStyle(R.style.noActionBar, true);
        }


        Preference preference = preference();
        String userTheme = preference.getTheme();
        switch(userTheme){
            case Preference.THEME_LEMON:
                theme.applyStyle(R.style.themeLemon, true);
                break;
            case Preference.THEME_BALCK_CAT:
            default:
                theme.applyStyle(R.style.themeBlackCat, true);
        }

        //TODO, font
        theme.applyStyle(R.style.textSizeNormal, true);



        //appbar
        AppBarLayout appbar = findViewById(R.id.appbar);
        if(appbar!=null){
            //todo style, theme
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    protected boolean isLightTheme() {
        return preference().isLightTheme();
    }

    protected boolean isNoActionBarTheme() {
        return true;
    }

    protected boolean isDialogTheme() {
        return false;
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

    protected I18N i18n() {
        return contexts().getI18n();
    }

    protected CalendarHelper calendarHelper() {
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

    protected Preference preference() {
        return contexts().getPreference();
    }


}

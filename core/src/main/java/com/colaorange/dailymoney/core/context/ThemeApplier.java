package com.colaorange.dailymoney.core.context;

import android.content.res.Resources;
import android.support.design.widget.AppBarLayout;

import com.colaorange.dailymoney.core.R;

/**
 * @author Dennis
 */
public class ThemeApplier {
    ContextsActivity activity;

    public ThemeApplier(ContextsActivity activity) {
        this.activity = activity;
    }

    public void applyTheme() {
        Resources.Theme theme = activity.getTheme();

        Preference preference = activity.preference();
        Preference.Theme userTheme = preference.getTheme();
        //apply meta first for light,etc pre define
        theme.applyStyle(userTheme.metaResId, true);

        boolean light = activity.isLightTheme();

        if (light) {
            if (activity.isDialogTheme()) {
                theme.applyStyle(android.support.v7.appcompat.R.style.Theme_AppCompat_Light_Dialog, true);
            } else {
                theme.applyStyle(android.support.v7.appcompat.R.style.Theme_AppCompat_Light, true);
            }

        } else {
            if (activity.isDialogTheme()) {
                theme.applyStyle(android.support.v7.appcompat.R.style.Theme_AppCompat_Dialog, true);
            } else {
                theme.applyStyle(android.support.v7.appcompat.R.style.Theme_AppCompat, true);
            }
        }

        if (activity.isNoActionBarTheme()) {
            theme.applyStyle(R.style.noActionBar, true);
        }


        if (light) {
            theme.applyStyle(R.style.lightCommon, true);
            theme.applyStyle(R.style.darkIconSet, true);
        }else{
            theme.applyStyle(R.style.darkCommon, true);
            theme.applyStyle(R.style.lightIconSet, true);
        }

        theme.applyStyle(userTheme.bodyResId, true);

        String userTextSize = preference.getTextSize();
        switch (userTextSize) {
            case Preference.TEXT_SIZE_MEDIUM:
                theme.applyStyle(R.style.textSizeMedium, true);
                break;
            case Preference.TEXT_SIZE_LARGE:
                theme.applyStyle(R.style.textSizeLarge, true);
                break;
            case Preference.TEXT_SIZE_NORMAL:
            default:
                theme.applyStyle(R.style.textSizeNormal, true);

        }


        //appbar
        AppBarLayout appbar = activity.findViewById(R.id.appbar);
        if (appbar != null) {
            //todo style, theme
        }

        theme.applyStyle(R.style.appThemeLatest, true);
    }
}

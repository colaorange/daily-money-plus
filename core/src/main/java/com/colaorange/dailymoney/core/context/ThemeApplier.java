package com.colaorange.dailymoney.core.context;

import android.content.res.Resources;
import android.support.design.widget.AppBarLayout;
import android.util.TypedValue;

import com.colaorange.dailymoney.core.R;

/**
 * Created by Dennis
 */
public class ThemeApplier {
    ContextsActivity activity;

    public ThemeApplier(ContextsActivity activity) {
        this.activity = activity;
    }

    public void applyTheme() {
        Resources.Theme theme = activity.getTheme();

        boolean light = activity.isLightTheme();

        if (light) {
            if (activity.isDialogTheme()) {
                theme.applyStyle(android.support.v7.appcompat.R.style.Theme_AppCompat_Light_Dialog, true);
            } else {
                theme.applyStyle(android.support.v7.appcompat.R.style.Theme_AppCompat_Light, true);
            }
            theme.applyStyle(R.style.darkIconSet, true);
        } else {
            if (activity.isDialogTheme()) {
                theme.applyStyle(android.support.v7.appcompat.R.style.Theme_AppCompat_Dialog, true);
            } else {
                theme.applyStyle(android.support.v7.appcompat.R.style.Theme_AppCompat, true);
            }
            theme.applyStyle(R.style.lightIconSet, true);
        }

        if (activity.isNoActionBarTheme()) {
            theme.applyStyle(R.style.noActionBar, true);
        }


        Preference preference = activity.preference();
        String userTheme = preference.getTheme();
        switch (userTheme) {
            case Preference.THEME_LEMON:
                theme.applyStyle(R.style.themeLemon, true);
                break;
            case Preference.THEME_SAKURA:
                theme.applyStyle(R.style.themeSakura, true);
                break;
            case Preference.THEME_ORANGE:
                theme.applyStyle(R.style.themeOrange, true);
                break;
            case Preference.THEME_COLA:
            default:
                theme.applyStyle(R.style.themeCola, true);
        }

        String userTextSize = preference.getTextSize();
        switch (userTextSize) {
            case Preference.TEXT_SIZE_MEDIUM:
                theme.applyStyle(R.style.textSizeMedium, true);
                break;
            case Preference.TEXT_SIZE_LARGE:
                theme.applyStyle(R.style.textSizeLarge, true);
                break;
            case Preference.TEXT_SIZE_NOMRAL:
            default:
                theme.applyStyle(R.style.textSizeNormal, true);

        }


        //appbar
        AppBarLayout appbar = activity.findViewById(R.id.appbar);
        if (appbar != null) {
            //todo style, theme
        }
    }
}

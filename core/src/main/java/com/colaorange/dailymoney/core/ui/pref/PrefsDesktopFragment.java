package com.colaorange.dailymoney.core.ui.pref;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import com.colaorange.commons.util.Strings;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.ContextsPrefsFragment;
import com.colaorange.dailymoney.core.util.I18N;

/**
 * @author dennis
 */
public class PrefsDesktopFragment extends ContextsPrefsFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.prefs_desktop);
        final I18N i18n = Contexts.instance().getI18n();
        initPrefs(i18n);
    }


    private void initPrefs(final I18N i18n) {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        super.onSharedPreferenceChanged(sharedPreferences, key);
        if(!Strings.isBlank(key) && key.startsWith(com.colaorange.dailymoney.core.context.Preference.CARD_DESKTOP_ENABLE_PREFIX)){
            ((ContextsActivity) getActivity()).markWholeRecreate();
        }
    }

}
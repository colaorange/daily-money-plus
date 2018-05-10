package com.colaorange.dailymoney.core.ui.pref;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.bg.TimeTickReceiver;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.ContextsPrefsFragment;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.util.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * @author dennis
 */
public class PrefsDeveloperFragment extends ContextsPrefsFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.prefs_developer);
        final I18N i18n = Contexts.instance().getI18n();
        initDeveloperPrefs(i18n);
    }



    private void initDeveloperPrefs(final I18N i18n) {

        adjustSummaryValue(findPreference(i18n.string(R.string.pref_csv_encoding)));

        try {
            Preference pref = findPreference(i18n.string(R.string.pref_testsdekstop));
            if (pref != null) {
                pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        try {
                            trackEvent(preference.getKey());

                            ((ContextsActivity)getActivity()).markWholeRecreate();

                        } catch (Exception x) {
                            Logger.w(x.getMessage(), x);
                            trackEvent(preference.getKey() + "_fail");
                        }
                        return true;
                    }
                });
            }
        } catch (Exception x) {
            Logger.w(x.getMessage(), x);
        }
    }

}
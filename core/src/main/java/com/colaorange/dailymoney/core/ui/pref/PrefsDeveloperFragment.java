package com.colaorange.dailymoney.core.ui.pref;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.ContextsPrefsFragment;
import com.colaorange.dailymoney.core.ui.legacy.LogViewerActivity;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.util.Logger;

/**
 * @author dennis
 */
public class PrefsDeveloperFragment extends ContextsPrefsFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.prefs_developer);
        final I18N i18n = Contexts.instance().getI18n();
        initPrefs(i18n);
    }



    private void initPrefs(final I18N i18n) {

        adjustSummaryValue(findPreference(i18n.string(R.string.pref_csv_encoding)));

        Preference pref;
        try {
            pref = findPreference(i18n.string(R.string.pref_testsdekstop));
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

        adjustSummaryValue(findPreference(i18n.string(R.string.pref_log_on)));
        adjustSummaryValue(findPreference(i18n.string(R.string.pref_log_max_line)));

        pref = findPreference("log_viewer");
        if (pref != null) {
            //https://stackoverflow.com/questions/4810803/open-facebook-page-from-android-app
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    try {
                        Intent intent = new Intent(getActivity(), LogViewerActivity.class);
                        startActivity(intent);
                        trackEvent(preference.getKey());
                    } catch (Exception x) {
                        Logger.w(x.getMessage(), x);
                        trackEvent(preference.getKey() + "_fail");
                    }
                    return true;
                }
            });
        }
    }

}
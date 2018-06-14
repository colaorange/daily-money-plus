package com.colaorange.dailymoney.core.ui.pref;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsPrefsFragment;
import com.colaorange.dailymoney.core.util.I18N;

/**
 * @author dennis
 */
public class PrefsRecordFragment extends ContextsPrefsFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.prefs_record);
        final I18N i18n = Contexts.instance().getI18n();
        initPrefs(i18n);
    }


    private void initPrefs(final I18N i18n) {
        adjustSummaryValue(findPreference(i18n.string(R.string.pref_record_list_layout)));
        adjustSummaryValue(findPreference(i18n.string(R.string.pref_max_records)));


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Preference parent = findPreference("parent_screen");
            Preference pref = findPreference(i18n.string(R.string.pref_group_records_by_date));
            if (parent != null && pref != null) {
                ((PreferenceScreen) parent).removePreference(pref);
            }
        }


    }

}
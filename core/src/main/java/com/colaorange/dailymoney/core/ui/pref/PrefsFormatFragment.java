package com.colaorange.dailymoney.core.ui.pref;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsPrefsFragment;
import com.colaorange.dailymoney.core.util.I18N;

/**
 * @author dennis
 */
public class PrefsFormatFragment extends ContextsPrefsFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.prefs_format);
        final I18N i18n = Contexts.instance().getI18n();
        initPrefs(i18n);
    }



    private void initPrefs(final I18N i18n) {
        adjustSummaryValue(findPreference(i18n.string(R.string.pref_format_date)));
        adjustSummaryValue(findPreference(i18n.string(R.string.pref_format_month)));
        adjustSummaryValue(findPreference(i18n.string(R.string.pref_format_time)));
    }

}
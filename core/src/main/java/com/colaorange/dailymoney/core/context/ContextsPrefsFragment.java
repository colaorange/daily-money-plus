package com.colaorange.dailymoney.core.context;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.util.I18N;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author dennis
 */
@InstanceState(stopLookup = true)
public class ContextsPrefsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    boolean dirty = false;

    Set<String> recreateKeys = new HashSet<>();

    Map<String, CharSequence> adjustSummaryCache = new HashMap<>();

    private InstanceStateHelper instanceStateHelper;

    public ContextsPrefsFragment(){
        instanceStateHelper = new InstanceStateHelper(this);
    }

    public void trackEvent(String action) {
        Activity activity = getActivity();
        if (activity instanceof ContextsActivity) {
            ((ContextsActivity) activity).trackEvent(action);
        }
    }

    protected void addRecreateKeys(String... keys){
        for(String key: keys){
            recreateKeys.add(key);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        instanceStateHelper.onRestore(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        instanceStateHelper.onBackup(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (dirty) {
            Contexts.instance().reloadPreference();
        }
        dirty = false;
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        dirty = true;
        trackEvent(Contexts.TE.PREFENCE + key);
        if (recreateKeys.contains(key)) {
            ((ContextsActivity) getActivity()).markWholeRecreate();
            getActivity().recreate();
        }

        adjustSummaryValue(findPreference(key));
    }

    public <T extends Preference> T adjustSummaryValue(T pref) {
        if (pref == null) {
            return pref;
        }

        CharSequence summary = adjustSummaryCache.get(pref.getKey());
        if (summary == null) {
            summary = pref.getSummary();
            adjustSummaryCache.put(pref.getKey(), summary);
        }

        if (pref instanceof ListPreference) {
            ListPreference vp = (ListPreference) pref;

            if (vp.getEntries() == null || vp.getEntryValues() == null || vp.getValue() == null) {
                return pref;
            }


            int idx = -1;
            int i = 0;
            for (CharSequence s : vp.getEntryValues()) {
                if (s.equals(vp.getValue())) {
                    idx = i;
                    break;
                }
                i++;
            }

            if (idx >= 0 && vp.getEntries().length > idx) {
                vp.setSummary(combineSummary(summary, vp.getEntries()[idx]));
            }

        } else if (pref instanceof EditTextPreference) {
            EditTextPreference vp = (EditTextPreference) pref;
            vp.setSummary(combineSummary(summary, vp.getText()));
        } else if (pref instanceof MultiSelectListPreference) {
            MultiSelectListPreference vp = (MultiSelectListPreference) pref;

            if (vp.getEntries() == null || vp.getEntryValues() == null || vp.getValues() == null) {
                return pref;
            }

            Set<String> selected = vp.getValues();
            Set<Integer> idxs = new LinkedHashSet<>();
            int i = -1;
            for (CharSequence s : vp.getEntryValues()) {
                i++;
                if (selected.contains(s)) {
                    idxs.add(i);
                }
            }
            StringBuilder sb = new StringBuilder();

            CharSequence[] csa = vp.getEntries();
            for (Integer idx : idxs) {
                if (idx < csa.length) {
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(csa[idx]);
                }
            }
            vp.setSummary(combineSummary(summary, sb.toString()));
        }
        return pref;
    }

    private CharSequence combineSummary(CharSequence summary, CharSequence value) {
        return summary == null ? value : "[" + value + "] "+summary;
    }
}
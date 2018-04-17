package com.colaorange.dailymoney.ui.legacy;

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

import com.colaorange.dailymoney.R;
import com.colaorange.dailymoney.bg.TimeTickReceiver;
import com.colaorange.dailymoney.context.Contexts;
import com.colaorange.dailymoney.context.ContextsActivity;
import com.colaorange.dailymoney.util.I18N;
import com.colaorange.dailymoney.util.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * @author dennis
 */
public class PrefsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    boolean dirty = false;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.prefs);
        final I18N i18n = Contexts.instance().getI18n();
        initAccountingPrefs(i18n);
        initDataPrefs(i18n);
        initContributionPrefs(i18n);
        initDeveloperPrefs(i18n);
    }

    private void initDataPrefs(I18N i18n) {
        SharedPreferences sprefs = getPreferenceManager().getSharedPreferences();
        Preference pref = findPreference(i18n.string(R.string.pref_auto_backup_weekdays));
        if (pref instanceof MultiSelectListPreference) {
            try {
                Calendar baseTime = Calendar.getInstance();
                SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
                baseTime.setTime(f.parse("20180107"));//which is sunday, value is 1
                DateFormat format = new SimpleDateFormat("EEE");

                String[] weekdays = new String[7];
                for (int i = 0; i < weekdays.length; i++) {
                    weekdays[i] = format.format(baseTime.getTime());
                    baseTime.add(Calendar.DAY_OF_MONTH, 1);
                }
                ((MultiSelectListPreference) pref).setEntries(weekdays);

                //selected value with default
                String str = i18n.string(R.string.default_auto_backup_weekdays);
                Set<String> strs = new LinkedHashSet<>();
                for (String a : str.split(",")) {
                    strs.add(a);
                }
                strs = sprefs.getStringSet(i18n.string(R.string.pref_auto_backup_weekdays), strs);

                ((MultiSelectListPreference)pref).setValues(strs);
            } catch (Exception x) {
                Logger.w(x.getMessage(), x);
            }
        }
        pref = findPreference(i18n.string(R.string.pref_auto_backup_at_hours));
        if (pref instanceof MultiSelectListPreference) {
            try {
                Calendar baseTime = Calendar.getInstance();
                SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd HH:mm:ss", Locale.ENGLISH);
                baseTime.setTime(f.parse("20180107 00:00:00"));//which is sunday, value is 1
                DateFormat format = new SimpleDateFormat("HH");

                String[] weekdays = new String[24];
                for (int i = 0; i < weekdays.length; i++) {
                    weekdays[i] = format.format(baseTime.getTime());
                    baseTime.add(Calendar.HOUR_OF_DAY, 1);
                }
                ((MultiSelectListPreference) pref).setEntries(weekdays);

                //selected value with default
                String str = i18n.string(R.string.default_auto_backup_at_hours);
                Set<String> strs = new LinkedHashSet<>();
                for (String a : str.split(",")) {
                    strs.add(a);
                }
                strs = sprefs.getStringSet(i18n.string(R.string.pref_auto_backup_at_hours), strs);

                ((MultiSelectListPreference)pref).setValues(strs);
            } catch (Exception x) {
                Logger.w(x.getMessage(), x);
            }
        }
    }


    private void initAccountingPrefs(final I18N i18n) {
        Preference pref = findPreference(i18n.string(R.string.pref_startday_year_month));
        if (pref instanceof ListPreference) {
            try {
                Calendar baseTime = Calendar.getInstance();
                SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
                baseTime.setTime(f.parse("20180101"));
                DateFormat format = Contexts.instance().getPreference().getMonthFormat();

                String[] months = new String[12];
                for (int i = 0; i < months.length; i++) {
                    months[i] = format.format(baseTime.getTime());
                    baseTime.add(Calendar.MONTH, 1);
                }
                ((ListPreference) pref).setEntries(months);
            } catch (Exception x) {
                Logger.w(x.getMessage(), x);
            }
        }
    }

    public void trackEvent(String action) {
        Activity activity = getActivity();
        if (activity instanceof ContextsActivity) {
            ((ContextsActivity) activity).trackEvent(action);
        }
    }

    private void initContributionPrefs(final I18N i18n) {
        try {
            Preference pref = findPreference("mailme_lang");
            if (pref != null) {
                pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        try {
                            trackEvent(preference.getKey());
                            Intent intent = new Intent();

                            intent.setAction(Intent.ACTION_SEND).setType("text/plain");
                            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{i18n.string(R.string.mailme_email)});
                            intent.putExtra(Intent.EXTRA_SUBJECT, i18n.string(R.string.mailme_lang_subject));
                            intent.putExtra(Intent.EXTRA_TEXT, i18n.string(R.string.mailme_lang_text));

                            startActivity(intent);
                        } catch (Exception x) {
                            Logger.w(x.getMessage(), x);
                            trackEvent(preference.getKey() + "_fail");
                        }
                        return true;
                    }
                });
            }

            pref = findPreference("vote_dm");
            if (pref != null) {
                pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        try {
                            trackEvent(preference.getKey());

                            Intent intent = new Intent();

                            Uri uri = Uri.parse(i18n.string(R.string.app_market_app));
                            intent.setAction(Intent.ACTION_VIEW).setData(uri);

                            startActivity(intent);
                        } catch (Exception x) {
                            Logger.w(x.getMessage(), x);
                            trackEvent(preference.getKey() + "_fail");
                        }
                        return true;
                    }
                });
            }

            pref = findPreference("like_dm");
            if (pref != null) {
                //https://stackoverflow.com/questions/4810803/open-facebook-page-from-android-app
                pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        try {

                            String ab = "_a";
                            Intent intent = new Intent();

                            String url = i18n.string(R.string.app_like_page);
                            Uri uri = Uri.parse(url);

                            try {
                                ApplicationInfo applicationInfo = getActivity().getPackageManager().getApplicationInfo("com.facebook.katana", 0);
                                if (applicationInfo.enabled) {
                                    uri = Uri.parse("fb://facewebmodal/f?href=" + url);
                                }
                                ab = "_b";
                            } catch (PackageManager.NameNotFoundException ignored) {
                            }

                            intent.setAction(Intent.ACTION_VIEW).setData(uri);

                            trackEvent(preference.getKey() + ab);

                            startActivity(intent);
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

    private void initDeveloperPrefs(final I18N i18n) {
        try {
        } catch (Exception x) {
            Logger.w(x.getMessage(), x);
        }
    }


    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
    }

    public void onPause() {
        super.onPause();
        if (dirty) {
            Contexts.instance().reloadPreference();
        }
        dirty = false;
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);

        Intent intent = new Intent();
        intent.setAction(TimeTickReceiver.ACTION_CLEAR_BACKUP_ERROR);
        getActivity().sendBroadcast(intent);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        dirty = true;
    }
}
package com.colaorange.dailymoney.ui.legacy;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.colaorange.dailymoney.R;
import com.colaorange.dailymoney.context.Contexts;
import com.colaorange.dailymoney.context.ContextsActivity;
import com.colaorange.dailymoney.util.I18N;
import com.colaorange.dailymoney.util.Logger;

/**
 * @author dennis
 */
public class PrefsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    boolean dirty = false;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.prefs);

        initContribution();
    }

    public void trackEvent(String action) {
        Activity activity = getActivity();
        if (activity instanceof ContextsActivity) {
            ((ContextsActivity) activity).trackEvent(action);
        }
    }

    private void initContribution() {
        final I18N i18n = Contexts.instance().getI18n();
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
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        dirty = true;
    }
}
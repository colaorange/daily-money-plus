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
import com.colaorange.dailymoney.core.context.ContextsPrefsFragment;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.util.Logger;

/**
 * @author dennis
 */
public class PrefsContributionFragment extends ContextsPrefsFragment implements SharedPreferences.OnSharedPreferenceChangeListener {


    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.prefs_contribution);
        final I18N i18n = Contexts.instance().getI18n();
        initPrefs(i18n);
    }


    private void initPrefs(final I18N i18n) {
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
}
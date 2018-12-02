package com.colaorange.dailymoney.core.ui.pref;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.SwitchPreference;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsPrefsFragment;
import com.colaorange.dailymoney.core.drive.GoogleDriveHelper;
import com.colaorange.dailymoney.core.ui.GUIs;
import com.colaorange.dailymoney.core.ui.legacy.GoogleDriveActivity;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.util.Logger;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * @author dennis
 */
public class PrefsFragment extends ContextsPrefsFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int REQUEST_DRIVE_AUTH = 101;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.prefs);
        final I18N i18n = Contexts.instance().getI18n();

        initDisplayPrefs(i18n);
        initAccountingPrefs(i18n);
        initDataPrefs(i18n);
        initWorkingBookPrefs(i18n);
    }

    private void initWorkingBookPrefs(final I18N i18n) {
        try {
            final String actionStr = i18n.string(R.string.act_clear_templates);
            Preference pref = findPreference("clear_templates");
            if (pref != null) {
                pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(final Preference preference) {

                        GUIs.OnFinishListener l = new GUIs.OnFinishListener() {
                            @Override
                            public boolean onFinish(int which, Object data) {
                                if (which == GUIs.OK_BUTTON) {
                                    try {
                                        trackEvent(preference.getKey());
                                        Contexts.instance().getPreference().clearRecordTemplates(Contexts.instance().getWorkingBookId());

                                        GUIs.shortToast(getActivity(), i18n.string(R.string.msg_common_finished, actionStr));
                                    } catch (Exception x) {
                                        Logger.w(x.getMessage(), x);
                                        trackEvent(preference.getKey() + "_fail");
                                    }
                                }
                                return true;
                            }
                        };

                        GUIs.confirm(getActivity(), i18n.string(R.string.qmsg_common_confirm, actionStr), l);
                        return true;
                    }
                });
            }
        } catch (Exception x) {
            Logger.w(x.getMessage(), x);
        }
    }

    private void initDisplayPrefs(I18N i18n) {
        addRecreateKeys(i18n.string(R.string.pref_theme));
        addRecreateKeys(i18n.string(R.string.pref_text_size));

        adjustSummaryValue(findPreference(i18n.string(R.string.pref_theme)));
        adjustSummaryValue(findPreference(i18n.string(R.string.pref_text_size)));
    }

    private void initDataPrefs(final I18N i18n) {
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
                String str = i18n.string(R.string.default_pref_auto_backup_weekdays);
                Set<String> strs = new LinkedHashSet<>();
                for (String a : str.split(",")) {
                    strs.add(a);
                }
                strs = sprefs.getStringSet(i18n.string(R.string.pref_auto_backup_weekdays), strs);

                ((MultiSelectListPreference) pref).setValues(strs);
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
                String str = i18n.string(R.string.default_pref_auto_backup_at_hours);
                Set<String> strs = new LinkedHashSet<>();
                for (String a : str.split(",")) {
                    strs.add(a);
                }
                strs = sprefs.getStringSet(i18n.string(R.string.pref_auto_backup_at_hours), strs);

                ((MultiSelectListPreference) pref).setValues(strs);
            } catch (Exception x) {
                Logger.w(x.getMessage(), x);
            }
        }

        adjustSummaryValue(findPreference(i18n.string(R.string.pref_auto_backup_weekdays)));
        adjustSummaryValue(findPreference(i18n.string(R.string.pref_auto_backup_at_hours)));


        pref = (SwitchPreference) findPreference("backup_to_google_drive");
        refreshGoogleDriveSummary(i18n);

        ((SwitchPreference)pref).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (((SwitchPreference)preference).isChecked()) {
                    //try to sign in
                    Task<GoogleDriveHelper> task = GoogleDriveHelper.signIn(getActivity());
                    task.addOnSuccessListener(new OnSuccessListener<GoogleDriveHelper>() {
                        @Override
                        public void onSuccess(GoogleDriveHelper helper) {
                            refreshGoogleDriveSummary(i18n);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            startActivityForResult(GoogleDriveHelper.getSignInIntent(getActivity()), REQUEST_DRIVE_AUTH);
                        }
                    });


                } else {
                    //singout
                    GoogleDriveHelper.revokeAccess(getActivity()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void v) {
                            refreshGoogleDriveSummary(i18n);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            refreshGoogleDriveSummary(i18n);
                        }
                    });
                }
                return false;
            }
        });
    }

    private void refreshGoogleDriveSummary(final I18N i18n) {
        final SwitchPreference drivePref = (SwitchPreference) findPreference("backup_to_google_drive");
        Task<GoogleDriveHelper> task = GoogleDriveHelper.signIn(getActivity());
        task.addOnSuccessListener(new OnSuccessListener<GoogleDriveHelper>() {
            @Override
            public void onSuccess(GoogleDriveHelper helper) {
                drivePref.setSummary(i18n.string(R.string.label_signin_as, helper.getGoogleSignInAccount().getDisplayName()));
                drivePref.setChecked(true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                drivePref.setSummary(i18n.string(R.string.label_not_signin));
                drivePref.setChecked(false);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {
        if (requestCode == REQUEST_DRIVE_AUTH) {
            refreshGoogleDriveSummary(Contexts.instance().getI18n());
        }
        return;
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

        adjustSummaryValue(findPreference(i18n.string(R.string.pref_firstday_week)));
        adjustSummaryValue(findPreference(i18n.string(R.string.pref_startday_month)));
        adjustSummaryValue(findPreference(i18n.string(R.string.pref_startday_year_month)));
        adjustSummaryValue(findPreference(i18n.string(R.string.pref_startday_year_month_day)));
    }
}
package com.colaorange.dailymoney.core.ui.legacy;


import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;

/**
 * @author dennis
 */
public class PrefsActivity extends ContextsActivity {

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new PrefsFragment())
                .commit();
    }

}

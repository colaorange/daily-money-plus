package com.colaorange.dailymoney.core.ui.pref;


import android.os.Bundle;

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

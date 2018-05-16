package com.colaorange.dailymoney.core.ui.pref;


import android.os.Bundle;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.ContextsActivity;

/**
 * @author dennis
 */
public class PrefsDeveloperActivity extends ContextsActivity {

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.prefs_skeleton);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .add(R.id.prefs_main, new PrefsDeveloperFragment())
                .commit();
    }

}

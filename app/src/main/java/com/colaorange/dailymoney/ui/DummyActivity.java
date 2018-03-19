package com.colaorange.dailymoney.ui;

import android.os.Bundle;

import com.colaorange.dailymoney.context.ContextsActivity;
/**
 * a dummy activity, by default, it do nothing thing and quit. 
 * @author dennis
 *
 */
public class DummyActivity extends ContextsActivity {
    
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        finish();
    }
    
    @Override
    protected void onResume(){
        finish();
    }
}

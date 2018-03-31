package com.colaorange.dailymoney.ui;

import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.colaorange.commons.util.GUIs;
import com.colaorange.dailymoney.context.ContextsActivity;
import com.colaorange.dailymoney.R;

/**
 * @author dennis
 */
public class AboutActivity extends ContextsActivity {

    WebView whatsnew;
    WebView contributor;
    WebView aboutapp;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.about);
        setTitle(i18n.string(R.string.app_name)+" "+ contexts().getAppVerName());

        aboutapp = findViewById(R.id.about_app);
        contributor = findViewById(R.id.about_contributor);
        whatsnew = findViewById(R.id.about_whatsnew);


        aboutapp.getSettings().setAllowFileAccess(true);
        aboutapp.getSettings().setJavaScriptEnabled(true);
        aboutapp.addJavascriptInterface(this, "dmctrl");
        aboutapp.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);

        contributor.getSettings().setAllowFileAccess(true);
        contributor.getSettings().setJavaScriptEnabled(true);
        contributor.addJavascriptInterface(this, "dmctrl");
        contributor.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);

        whatsnew.getSettings().setAllowFileAccess(true);
        whatsnew.getSettings().setJavaScriptEnabled(true);
        whatsnew.addJavascriptInterface(this, "dmctrl");
        whatsnew.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);



        aboutapp.loadUrl(Constants.LOCAL_URL_PREFIX + i18n.string(R.string.path_about_app));
        contributor.loadUrl(Constants.LOCAL_URL_PREFIX + i18n.string(R.string.path_contributor));
        whatsnew.loadUrl(Constants.LOCAL_URL_PREFIX + i18n.string(R.string.path_what_is_new));
    }

    @JavascriptInterface
    public void onLinkClicked(final String path) {
        //not in ui thread.
        //android.view.ViewRootImpl$CalledFromWrongThreadException: Only the original thread that created a view hierarchy can touch its views.
        GUIs.post(new Runnable() {
            public void run() {
                whatsnew.setVisibility(View.GONE);
                contributor.setVisibility(View.GONE);
                aboutapp.loadUrl(Constants.LOCAL_URL_PREFIX + path);
            }
        });
    }

}

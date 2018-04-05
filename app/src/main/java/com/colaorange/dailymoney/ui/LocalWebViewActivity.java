package com.colaorange.dailymoney.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Button;

import com.colaorange.commons.util.GUIs;
import com.colaorange.dailymoney.context.ContextsActivity;
import com.colaorange.dailymoney.R;
import com.colaorange.dailymoney.ui.legacy.Desktop;
import com.colaorange.dailymoney.ui.legacy.DesktopActivity;
import com.colaorange.dailymoney.ui.legacy.DesktopItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author dennis
 */
public class LocalWebViewActivity extends ContextsActivity{

    public static final String INTENT_URI = "uri";
    public static final String INTENT_URI_RES_ID = "uriResId";
    public static final String INTENT_TITLE = "title";

    WebView webView;

    String title;
    String uri;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.localwebview);

        initParams();
        initMembers();
        refreshUI();
    }

    private void initParams() {
        Bundle bundle = getIntentExtras();
        uri = null;

        int rid = bundle.getInt(INTENT_URI_RES_ID, -1);
        if (rid != -1) {
            uri = i18n.string(rid);
        } else {
            uri = bundle.getString(INTENT_URI);
        }

        title = bundle.getString(INTENT_TITLE);
    }

    private void initMembers() {

        webView = findViewById(R.id.webview);

        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(this, "wvif");
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
    }

    private void refreshUI() {
        if (title != null) {
            this.setTitle(title);
        }

        webView.loadUrl(Constants.LOCAL_URL_PREFIX + uri);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem mi = menu.add(i18n.string(R.string.cact_close));
        mi.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                LocalWebViewActivity.this.finish();
                return true;
            }
        });

        return true;
    }

    @JavascriptInterface
    public void jsStartView(final String path) {
        //not in ui thread.
        GUIs.post(new Runnable() {
            public void run() {
                Intent intent = new Intent(LocalWebViewActivity.this, LocalWebViewActivity.class);
                intent.putExtra(LocalWebViewActivity.INTENT_URI, path);
                intent.putExtra(LocalWebViewActivity.INTENT_TITLE, LocalWebViewActivity.this.getTitle());
                startActivity(intent);
            }
        });
    }

}

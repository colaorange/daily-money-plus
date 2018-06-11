package com.colaorange.dailymoney.core.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.R;

/**
 * @author dennis
 */
public class LocalWebViewActivity extends ContextsActivity {

    public static final String ARG_URI = "lwv.uri";
    public static final String ARG_URI_RES_ID = "lwv.uriResId";

    WebView vWeb;

    String uri;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.local_webview);

        initArgs();
        initMembers();
        refreshUI();
    }

    private void initArgs() {
        Bundle bundle = getIntentExtras();
        uri = null;

        int rid = bundle.getInt(ARG_URI_RES_ID, -1);
        if (rid != -1) {
            uri = i18n().string(rid);
        } else {
            uri = bundle.getString(ARG_URI);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initMembers() {

        vWeb = findViewById(R.id.local_webview);

        vWeb.getSettings().setAllowFileAccess(true);
        vWeb.getSettings().setJavaScriptEnabled(true);
        vWeb.addJavascriptInterface(this, "wvif");
        vWeb.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        vWeb.setBackgroundColor(Color.TRANSPARENT);
    }

    private void refreshUI() {
        vWeb.loadUrl(Constants.LOCAL_URL_PREFIX + uri);

        trackEvent(TE.WEBVIEW + uri);
    }


    //use home back
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        super.onCreateOptionsMenu(menu);
//        getMenuInflater().inflate(R.menu.close_menu, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == R.id.menu_close) {
//            LocalWebViewActivity.this.finish();
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    @JavascriptInterface
    public void jsStartView(final String path) {
        //not in ui thread.
        GUIs.post(new Runnable() {
            public void run() {
                Intent intent = new Intent(LocalWebViewActivity.this, LocalWebViewActivity.class);
                intent.putExtra(LocalWebViewActivity.ARG_URI, path);
                intent.putExtra(LocalWebViewActivity.ARG_TITLE, LocalWebViewActivity.this.getTitle());
                startActivity(intent);
            }
        });
    }

}

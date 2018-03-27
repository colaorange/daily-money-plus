package com.colaorange.dailymoney.ui;

import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.colaorange.commons.util.GUIs;
import com.colaorange.dailymoney.context.ContextsActivity;
import com.colaorange.dailymoney.R;
/**
 * 
 * @author dennis
 *
 */
public class LocalWebViewActivity extends ContextsActivity {
    
    public static final String INTENT_URI = "uri";
    public static final String INTENT_URI_ID = "uriid";
    public static final String INTENT_TITLE = "title";
    
    WebView webView;
    
    
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.webview);
        initWebView();
        initInit();
    }

    private void initWebView() {
        webView = (WebView)findViewById(R.id.webview);
        
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(this,"dmctrl");
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY); 
    }

    private void initInit() {
        Bundle bundle = getIntentExtras();
        String uri = null;
        int rid = bundle.getInt(INTENT_URI_ID,-1);
        if(rid!=-1){
            uri = i18n.string(rid);
        }else{
            uri = bundle.getString(INTENT_URI);
        }
        
        String title = bundle.getString(INTENT_TITLE);
        if(title!=null){
            this.setTitle(title);
        }
        
        webView.loadUrl(Constants.LOCAL_URL_PREFIX+uri);
    }

    @JavascriptInterface
    public void onLinkClicked(final String path){
        //not in ui thread.
        //android.view.ViewRootImpl$CalledFromWrongThreadException: Only the original thread that created a view hierarchy can touch its views.
        GUIs.post(new Runnable() {
            public void run() {
                webView.loadUrl(Constants.LOCAL_URL_PREFIX + path);
            }
        });
    }

}

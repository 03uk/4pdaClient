package org.softeg.slartus.forpda.classes;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.*;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import org.softeg.slartus.forpda.BaseFragmentActivity;
import org.softeg.slartus.forpda.MyApp;
import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpda.classes.common.ExtPreferences;
import org.softeg.slartus.forpda.common.Log;

/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 16.10.12
 * Time: 8:35
 * To change this template use File | Settings | File Templates.
 */
public abstract class BrowserViewsFragmentActivity extends BaseFragmentActivity implements IWebViewContainer {
    public abstract String Prefix();

    public abstract WebView getWebView();


    WebViewExternals m_WebViewExternals;

    public WebViewExternals getWebViewExternals() {
        if (m_WebViewExternals == null)
            m_WebViewExternals = new WebViewExternals(this);
        return m_WebViewExternals;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    protected boolean getCurrentFullScreen() {
        return getWebViewExternals().getCurrentFullScreen();
    }

    protected void updateFullscreenStatus(Boolean fullScreen) {
        getWebViewExternals().updateFullscreenStatus(fullScreen);
    }

    public void setAndSaveUseZoom(Boolean useZoom) {
        getWebViewExternals().setAndSaveUseZoom(useZoom);
    }

    protected void setWebViewSettings() {
        getWebViewExternals().setWebViewSettings();
    }

    public void onPrepareOptionsMenu() {
        getWebViewExternals().onPrepareOptionsMenu();
    }

    @Override
    public void setContentView(int id) {
        super.setContentView(id);
        if (getWebViewExternals().isUseFullScreen())
            getWebViewExternals().addFullScreenButton();
    }

    @Override
    protected void loadPreferences(SharedPreferences prefs) {
        super.loadPreferences(prefs);
        getWebViewExternals().loadPreferences(prefs);

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return getWebViewExternals().dispatchKeyEvent(event);
    }

    public boolean dispatchSuperKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event);
    }
}

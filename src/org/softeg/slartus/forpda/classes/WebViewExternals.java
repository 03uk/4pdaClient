package org.softeg.slartus.forpda.classes;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CacheManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import org.softeg.slartus.forpda.MyApp;
import org.softeg.slartus.forpda.classes.common.ExtPreferences;
import org.softeg.slartus.forpda.common.Log;

import java.lang.reflect.Method;

/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 24.10.12
 * Time: 8:31
 * To change this template use File | Settings | File Templates.
 */
public class WebViewExternals {

    private IWebViewContainer m_WebViewContainer;
    private boolean useFullScreen;


    public WebViewExternals(IWebViewContainer webViewContainer){

        m_WebViewContainer = webViewContainer;
    }
    protected String Prefix(){
        return m_WebViewContainer.Prefix();
    }
    protected WebView getWebView(){
        return m_WebViewContainer.getWebView();
    }

    private Boolean m_UseVolumesScroll = false;
    private Boolean m_UseZoom = true;
    private Boolean m_FullScreen = false;
    private Boolean m_LoadsImagesAutomatically = true;
    private Boolean m_KeepScreenOn = false;
    private int m_ZoomLevel = 150;


    
    private ImageButton btnFullScreen;
    protected ImageButton getFullScreenButton(){
        return m_WebViewContainer.getFullScreenButton();
    }
    protected void addFullScreenButton(){
        btnFullScreen=getFullScreenButton();
        btnFullScreen.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                updateFullscreenStatus(true);
                btnFullScreen.setVisibility(View.GONE);
            }
        });
    }

    protected void updateFullscreenStatus(){
        if(!m_FullScreen)return ;
        updateFullscreenStatus(true);
    }

    protected void toggleFullscreenStatus(){
        updateFullscreenStatus(!m_CurrentFullScreen);
    }

    private Boolean m_CurrentFullScreen=false;
    protected Boolean getCurrentFullScreen(){
        return m_CurrentFullScreen;
    }
    
    private Window getWindow(){
        return m_WebViewContainer.getWindow();
    }
    
    private com.actionbarsherlock.app.ActionBar getSupportActionBar(){
         return m_WebViewContainer.getSupportActionBar();
    }

    public void onPrepareOptionsMenu(){
        if(!m_FullScreen)return;
        updateFullscreenStatus(false);
    }

    protected void updateFullscreenStatus(Boolean useFullscreen) {
        if (useFullscreen) {
            if(getSupportActionBar()!=null)
                getSupportActionBar().hide();

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        } else {
            if(getSupportActionBar()!=null)
                getSupportActionBar().show();
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if(m_FullScreen)
                btnFullScreen.setVisibility(View.VISIBLE);
        }
        m_CurrentFullScreen=useFullscreen;
    }

    public void setWebViewSettings() {
        WebView webView=getWebView();
        disableWebViewCache();
        webView.setBackgroundColor(MyApp.INSTANCE.getThemeStyleWebViewBackground());
        if(!MyApp.INSTANCE.isWhiteTheme()){
            webView.setBackgroundColor(MyApp.INSTANCE.getThemeStyleWebViewBackground());
            webView.loadData("<html><head></head><body bgcolor="+MyApp.INSTANCE.getCurrentThemeName()+"></body></html>","text/html", "UTF-8");
        }
        webView.getSettings().setLoadsImagesAutomatically(m_LoadsImagesAutomatically);
        webView.setKeepScreenOn(m_KeepScreenOn);
        webView.getSettings().setBuiltInZoomControls(m_UseZoom);
        webView.getSettings().setSupportZoom(m_UseZoom);

        try {
            webView.setInitialScale(m_ZoomLevel);
        } catch (Exception ex) {
            Log.e(null, ex);
        }

        int sdk = Build.VERSION.SDK_INT;
        if (sdk > 7)
            webView.getSettings().setPluginState(WebSettings.PluginState.ON);
    }
    
    private void disableWebViewCache(){
        getWebView().getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
//        try
//        {
//            Method m = CacheManager.class.getDeclaredMethod("setCacheDisabled", boolean.class);
//            m.setAccessible(true);
//            m.invoke(null, true);
//        }
//        catch (Throwable e)
//        {
//            Log.e(null, e);
//        }
    }

    protected void setAndSaveUseZoom( boolean useZoom) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.INSTANCE);
        m_UseZoom = useZoom;
        WebView webView=getWebView();
        webView.getSettings().setBuiltInZoomControls(m_UseZoom);
        webView.getSettings().setSupportZoom(m_UseZoom);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Prefix()+ ".ZoomUsing", m_UseZoom);
        editor.commit();
    }


    public void loadPreferences(SharedPreferences prefs) {
        String prefix = Prefix();
        m_FullScreen = prefs.getBoolean(prefix + ".FullScreen", false);
        if (m_FullScreen)
            updateFullscreenStatus(true);
        m_UseZoom = prefs.getBoolean(prefix + ".ZoomUsing", true);
        m_UseVolumesScroll = prefs.getBoolean(prefix + ".UseVolumesScroll", false);
        m_LoadsImagesAutomatically = prefs.getBoolean(prefix + ".LoadsImagesAutomatically", true);
        m_KeepScreenOn = prefs.getBoolean(prefix + ".KeepScreenOn", false);
        try {
            m_ZoomLevel = ExtPreferences.parseInt(prefs, prefix + ".ZoomLevel", 150);
        } catch (Exception ex) {
            Log.e(null, ex);
        }

    }

    public boolean dispatchKeyEvent( KeyEvent event) {
        if (!m_UseVolumesScroll)
            return m_WebViewContainer.dispatchSuperKeyEvent(event);
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        WebView scrollView = getWebView();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    scrollView.pageUp(false);
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    scrollView.pageDown(false);
                }
                return true;
            default:
                return m_WebViewContainer.dispatchSuperKeyEvent(event);
        }
    }


    public boolean isUseFullScreen() {
        return m_FullScreen;
    }

    public Boolean getLoadsImagesAutomatically() {
        return m_LoadsImagesAutomatically;
    }

    public void setLoadsImagesAutomatically(boolean b) {
        m_LoadsImagesAutomatically=b;
        getWebView().getSettings().setLoadsImagesAutomatically(b);
        // только на текущую сессию
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.INSTANCE);
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.putBoolean(Prefix() + ".LoadsImagesAutomatically",m_LoadsImagesAutomatically);
//        editor.commit();
    }
}

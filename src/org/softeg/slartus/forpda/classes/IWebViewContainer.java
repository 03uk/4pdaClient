package org.softeg.slartus.forpda.classes;

import android.content.Context;
import android.view.KeyEvent;
import android.view.Window;
import android.webkit.WebView;
import android.widget.ImageButton;

/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 24.10.12
 * Time: 8:32
 * To change this template use File | Settings | File Templates.
 */
public interface IWebViewContainer {
    String Prefix();
    WebView getWebView();
    ImageButton getFullScreenButton();
    Window getWindow();
    com.actionbarsherlock.app.ActionBar getSupportActionBar();

    boolean dispatchSuperKeyEvent(KeyEvent event);
}

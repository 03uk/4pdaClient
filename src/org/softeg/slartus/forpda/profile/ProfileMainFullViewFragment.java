package org.softeg.slartus.forpda.profile;

import android.content.SharedPreferences;


import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.actionbarsherlock.app.SherlockFragment;
import org.softeg.slartus.forpda.IntentActivity;
import org.softeg.slartus.forpda.MyApp;
import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpda.classes.AdvWebView;
import org.softeg.slartus.forpda.classes.Post;
import org.softeg.slartus.forpda.classes.common.ExtPreferences;
import org.softeg.slartus.forpda.common.Log;

/**
 * User: slinkin
 * Date: 10.10.12
 * Time: 8:33
 */
public class ProfileMainFullViewFragment extends SherlockFragment {
    private Handler mHandler = new Handler();
    private AdvWebView webView;
    private Boolean m_UseVolumesScroll = false;
    private Boolean m_UseZoom = true;
    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static ProfileMainFullViewFragment newInstance(int groupPosition, int childPosition, String data) {
        ProfileMainFullViewFragment f = new ProfileMainFullViewFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("groupPosition", groupPosition);
        args.putInt("childPosition", childPosition);
        args.putString("data",data);
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        if (container == null) {
            // Currently in a layout without a container, so no
            // reason to create our view.
            return null;
        }

        View v = inflater.inflate(R.layout.profile_main_full_view, null);
        webView = (AdvWebView) v.findViewById(R.id.wvBody);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        loadPreferences(prefs);

        registerForContextMenu(webView);

        webView.getSettings().setLoadsImagesAutomatically(prefs.getBoolean("news.LoadsImagesAutomatically", true));
        webView.setKeepScreenOn(prefs.getBoolean("news.KeepScreenOn", false));


        webView.getSettings().setBuiltInZoomControls(m_UseZoom);
        webView.getSettings().setSupportZoom(m_UseZoom);
        webView.setWebViewClient(new MyWebViewClient());
        try {
            int zoom=         ExtPreferences.parseInt(prefs, "news.ZoomLevel", 150);
            webView.setInitialScale(zoom);
        } catch (Exception ex) {
            Log.e(null, ex);
        }

        int sdk =Build.VERSION.SDK_INT;
        if (sdk > 7)
            webView.getSettings().setPluginState(WebSettings.PluginState.ON);

        String cssFile = MyApp.INSTANCE. getThemeCssFileName();
        String body="<html xml:lang=\"en\" lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\">\n\n" +
                "<head>\n\n" +
                "<meta http-equiv=\"content-type\" content=\"text/html; charset=windows-1251\" />\n" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"file://" + cssFile + "\" />\n" +
                "</head>\n\n" +
                "<body>\n"+getData()+"\n</body></html>";
        webView.loadDataWithBaseURL("\"file:///android_asset/\"", Post.modifyBody(body), "text/html", "UTF-8", null);
        return v;
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (!m_UseVolumesScroll)
            return false;
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        WebView scrollView = webView;
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
                return false;
        }
    }

    private class MyWebViewClient extends WebViewClient {


        @Override
        public boolean shouldOverrideUrlLoading(WebView view, final String url) {
            // if (ThemeActivity.this.webView.GetJavascriptInterfaceBroken())



            IntentActivity.tryShowUrl(getActivity(), mHandler, url, true, false);

            return true;
        }
    }

    private void loadPreferences(SharedPreferences prefs) {
        m_UseZoom = prefs.getBoolean("news.ZoomUsing", true);

        m_UseVolumesScroll = prefs.getBoolean("news.UseVolumesScroll", false);


    }

    public int getGroupPosition() {
        return getArguments().getInt("groupPosition", 0);
    }

    public String getData() {
        return getArguments().getString("data");
    }


    public int getChildPosition() {
        return getArguments().getInt("childPosition", 0);
    }

}

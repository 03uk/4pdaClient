package org.softeg.slartus.forpda.qms_2_0;

import android.*;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.*;
import org.softeg.slartus.forpda.*;
import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpda.classes.common.ExtPreferences;
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpda.qms.QmsPreferencesActivity;
import org.softeg.slartus.forpdaapi.Qms_2_0;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: slinkin
 * Date: 15.06.12
 * Time: 15:50
 */
public class QmsChatActivity extends BaseFragmentActivity {
    private Handler mHandler = new Handler();
    private WebView wvChat;
    private String m_Id;
    private String m_TId;
    private String m_Nick;
    private String m_ThemeTitle;
    private String m_PageBody;
    private long m_LastBodyLength = 0;

    private EditText edMessage;

    private MenuFragment mFragment1;
    private long m_UpdateTimeout = 15000;
    private Timer m_UpdateTimer = new Timer();

    private static final String MID_KEY = "mid";
    private static final String TID_KEY = "tid";
    private static final String THEME_TITLE_KEY = "theme_title";
    private static final String NICK_KEY = "nick";
    private static final String PAGE_BODY_KEY = "page_body";


    final Handler uiHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.qms_chat);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        createActionMenu();

        edMessage = (EditText) findViewById(R.id.edMessage);
        findViewById(R.id.btnSend).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startSendMessage();
            }
        });


        wvChat = (WebView) findViewById(R.id.wvChat);
        wvChat.getSettings().setBuiltInZoomControls(false);
        wvChat.getSettings().setSupportZoom(true);
        wvChat.getSettings().setJavaScriptEnabled(true);
        if (!MyApp.INSTANCE.isWhiteTheme()) {
            wvChat.setBackgroundColor(MyApp.INSTANCE.getThemeStyleWebViewBackground());
            wvChat.loadData("<html><head></head><body bgcolor=" + MyApp.INSTANCE.getCurrentThemeName() + "></body></html>", "text/html", "UTF-8");
        }
        wvChat.setWebViewClient(new MyWebViewClient());
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        m_Id = extras.getString(MID_KEY);
        m_Nick = extras.getString(NICK_KEY);
        m_TId = extras.getString(TID_KEY);
        m_ThemeTitle = extras.getString(THEME_TITLE_KEY);
        m_PageBody = extras.getString(PAGE_BODY_KEY);
        setTitle(m_Nick + ":QMS:" + m_ThemeTitle);

        if(!TextUtils.isEmpty(m_PageBody)){
            m_LastBodyLength = m_PageBody.length();
            m_PageBody = transformChatBody(m_PageBody);
            wvChat.loadDataWithBaseURL("\"file:///android_asset/\"", m_PageBody, "text/html", "UTF-8", null);
            m_PageBody=null;
        }
      //  hidePanels();
    }

    public static void openChat(Context activity, String userId, String userNick,
                                String tid, String themeTitle) {
        Intent intent = new Intent(activity.getApplicationContext(), QmsChatActivity.class);
        intent.putExtra(MID_KEY, userId);
        intent.putExtra(NICK_KEY, userNick);

        intent.putExtra(TID_KEY, tid);
        intent.putExtra(THEME_TITLE_KEY, themeTitle);

        activity.startActivity(intent);
    }

    public static void openChat(Context activity, String userId, String userNick, String tid, String themeTitle,
                                String pageBody) {
        Intent intent = new Intent(activity.getApplicationContext(), QmsChatActivity.class);
        intent.putExtra(MID_KEY, userId);
        intent.putExtra(NICK_KEY, userNick);

        intent.putExtra(TID_KEY, tid);
        intent.putExtra(THEME_TITLE_KEY, themeTitle);
        intent.putExtra(PAGE_BODY_KEY, pageBody);

        activity.startActivity(intent);
    }

//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
//        super.onPrepareOptionsMenu(menu);
//        if (!getSupportActionBar().isShowing()) {
//            getSupportActionBar().show();
//        }
//        return true;
//    }
//
//    public void hidePanels() {
//        getSupportActionBar().hide();
//    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);


        outState.putString(MID_KEY, m_Id);
        outState.putString(NICK_KEY, m_Nick);
        outState.putString(TID_KEY, m_TId);
        outState.putString(THEME_TITLE_KEY, m_ThemeTitle);


    }

    @Override
    protected void onRestoreInstanceState(Bundle outState) {
        super.onRestoreInstanceState(outState);

        m_Id = outState.getString(MID_KEY);
        m_Nick = outState.getString(NICK_KEY);
        m_TId = outState.getString(TID_KEY);
        m_ThemeTitle = outState.getString(THEME_TITLE_KEY);
        setTitle(m_Nick + " - QMS - " + m_ThemeTitle);

    }


    @Override
    public void onResume() {
        super.onResume();
        loadPrefs();
        startUpdateTimer();
    }

    @Override
    public void onPause() {
        super.onPause();
        m_UpdateTimer.cancel();
        m_UpdateTimer.purge();
    }


    @Override
    public void onStop() {
        super.onStop();
        m_UpdateTimer.cancel();
        m_UpdateTimer.purge();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        m_UpdateTimer.cancel();
        m_UpdateTimer.purge();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return true;
    }

    private void loadPrefs() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        m_UpdateTimeout = ExtPreferences.parseInt(preferences, "qms.chat.update_timer", 15) * 1000;
    }

    private String transformChatBody(String chatBody) {
        StringBuilder m_Body = new StringBuilder();
        m_Body.append("<html xml:lang=\"en\" lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\">\n");
        m_Body.append("<head>\n");
        m_Body.append("<meta http-equiv=\"content-type\" content=\"text/html; charset=windows-1251\" />\n");
        EditPost.addStyleSheetLink(m_Body);
        m_Body.append("<script type=\"text/javascript\" src=\"file:///android_asset/theme.js\"></script>\n");

        //m_Body.append("<script type=\"text/javascript\">function ScrollToBottom(){window.scrollTo(0,document.body.scrollHeight);}</script>");
        m_Body.append("</head>\n");
        m_Body.append("<body onload=\"scrollToElement('bottom_element')\">\n");
        m_Body.append(chatBody);
        m_Body.append("<div id=\"bottom_element\" name=\"bottom_element\"></div>");
        m_Body.append("</body>\n");
        m_Body.append("</html>\n");

        return m_Body.toString();
    }

    private void reLoadChatSafe() {
        uiHandler.post(new Runnable() {
            public void run() {
                setSupportProgressBarIndeterminateVisibility(true);
                //pbLoading.setVisibility(View.VISIBLE);
            }
        });
        String chatBody = null;
        Throwable ex = null;
        try {
            String body = Qms_2_0.getChat(Client.INSTANCE, m_Id, m_TId);
            if (body.length() == m_LastBodyLength) {
                uiHandler.post(new Runnable() {
                    public void run() {
                        setSupportProgressBarIndeterminateVisibility(false);
                    }
                });
                return;
            }
            m_LastBodyLength = body.length();
            chatBody = transformChatBody(body);
        } catch (Throwable e) {
            ex = e;
        }
        final Throwable finalEx = ex;
        final String finalChatBody = chatBody;
        uiHandler.post(new Runnable() {
            public void run() {

                if (finalEx == null) {
                    wvChat.loadDataWithBaseURL("\"file:///android_asset/\"", finalChatBody, "text/html", "UTF-8", null);
                } else {
                    if (finalEx != null)
                        Log.e(QmsChatActivity.this, finalEx);
                    else
                        Toast.makeText(QmsChatActivity.this, "Неизвестная ошибка",
                                Toast.LENGTH_SHORT).show();

                }
                setSupportProgressBarIndeterminateVisibility(false);
            }
        });

    }

    private void startUpdateTimer() {
        m_UpdateTimer.cancel();
        m_UpdateTimer.purge();
        m_UpdateTimer = new Timer();
        m_UpdateTimer.schedule(new TimerTask() { // Определяем задачу
            @Override
            public void run() {
                reLoadChatSafe();
            }
        }, 0L, m_UpdateTimeout);

    }

    private void createActionMenu() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        mFragment1 = (MenuFragment) fm.findFragmentByTag("f1");
        if (mFragment1 == null) {
            mFragment1 = new MenuFragment();
            ft.add(mFragment1, "f1");
        }
        ft.commit();
    }

    private String m_MessageText = null;

    private void saveScale(float scale) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat("qms.ZoomLevel", scale);
        editor.commit();
    }

    private float loadScale() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        return prefs.getFloat("qms.ZoomLevel", wvChat.getScrollY());

    }

    private void startSendMessage() {
        m_MessageText = edMessage.getText().toString();
        if (TextUtils.isEmpty(m_MessageText)) {
            Toast.makeText(this, "Введите текст для отправки.", Toast.LENGTH_SHORT).show();
            return;
        }
        new SendTask(this).execute();
    }

    private void zoomOut() {
       wvChat.zoomOut();
    }

    private void zoomIn() {
        wvChat.zoomIn();
    }

    private class SendTask extends AsyncTask<String, Void, Boolean> {


        private final ProgressDialog dialog;
        public String m_ChatBody;


        public SendTask(Context context) {

            dialog = new ProgressDialog(context);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {

                m_ChatBody = transformChatBody(Qms_2_0.sendMessage(Client.INSTANCE, m_Id, m_TId, m_MessageText));

                return true;
            } catch (Throwable e) {
                ex = e;
                return false;
            }
        }

        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.setMessage("Отправка сообщения...");
            this.dialog.show();
        }

        private Throwable ex;

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            if (success) {
                edMessage.getText().clear();

                wvChat.loadDataWithBaseURL("\"file:///android_asset/\"", m_ChatBody, "text/html", "UTF-8", null);
            } else {
                if (ex != null)
                    Log.e(QmsChatActivity.this, ex);
                else
                    Toast.makeText(QmsChatActivity.this, "Неизвестная ошибка",
                            Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static final class MenuFragment extends SherlockFragment {
        public MenuFragment() {

        }

        private QmsChatActivity getInterface(){
            if(getActivity()==null)return null;
            return (QmsChatActivity)getActivity();
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            MenuItem item = menu.add("Обновить").setIcon(R.drawable.ic_menu_refresh);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    new Thread(new Runnable() {
                        public void run() {
                            ((QmsChatActivity) getActivity()).reLoadChatSafe();
                        }
                    }).start();

                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            item = menu.add("Настройки").setIcon(R.drawable.settings);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    Intent intent = new Intent(getActivity(), QmsPreferencesActivity.class);
                    getActivity().startActivity(intent);
                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

            item = menu.add("Увеличить масштаб").setIcon(R.drawable.ic_menu_zoom);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    getInterface().zoomIn();
                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

            item = menu.add("Уменьшить масштаб").setIcon(R.drawable.ic_menu_zoom_out);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    getInterface().zoomOut();
                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }
    }



    private class MyWebViewClient extends WebViewClient {

        public MyWebViewClient() {
            m_Scale = loadScale();
        }

        private float m_Scale;
        private int m_ScrollY;

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

            //  setSupportProgressBarIndeterminateVisibility(true);

            m_ScrollY = wvChat.getScrollY();
            wvChat.setInitialScale((int) (m_Scale * 100));
            //ThemeActivity.this.setProgressBarIndeterminateVisibility(true);
        }

        @Override
        public void onScaleChanged(WebView view, float oldScale, float newScale) {
            super.onScaleChanged(view, oldScale, newScale);
            m_Scale = wvChat.getScale();
            saveScale(m_Scale);
        }

        @Override
        public void onPageFinished(final WebView view, String url) {
            super.onPageFinished(view, url);


            try {

                wvChat.setInitialScale((int) (m_Scale * 100));

            } catch (Throwable ex) {
                Log.e(QmsChatActivity.this, ex);
            }


            setSupportProgressBarIndeterminateVisibility(false);

        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, final String url) {

            IntentActivity.tryShowUrl(QmsChatActivity.this, mHandler, url, true, false, "");

            return true;
        }
    }
}

package org.softeg.slartus.forpda.Mail;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import org.softeg.slartus.forpda.BaseFragmentActivity;
import org.softeg.slartus.forpda.IntentActivity;
import org.softeg.slartus.forpda.Mail.classes.Mail;
import org.softeg.slartus.forpda.MyApp;
import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpda.classes.BrowserViewsFragmentActivity;
import org.softeg.slartus.forpda.classes.MailActivityInterface;
import org.softeg.slartus.forpda.common.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: slinkin
 * Date: 14.03.12
 * Time: 12:38
 */
public class MailActivity extends BrowserViewsFragmentActivity implements MailActivityInterface {
    private Handler mHandler = new Handler();
    private Uri m_Data = null;
    private String m_Id = null;
    private WebView mWvBody;
    private TextView author_text, date_text;

    MenuFragment mFragment1;
    private Mail m_Mail = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.mail);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        createActionMenu();

        author_text = (TextView) findViewById(R.id.author_text);
        date_text = (TextView) findViewById(R.id.date_text);
        mWvBody = (WebView) findViewById(R.id.body_webview);

        configWebView();

        Intent intent = getIntent();
        if (intent != null && intent.getData() != null) {
            m_Data = intent.getData();


            return;
        }
        Bundle extras = intent.getExtras();

        m_Id = extras.getString("MailId");


        String url = "http://4pda.ru/forum/index.php?act=Msg&CODE=03&MSID=" + m_Id;
        showMail(url);
    }


    public ImageButton getFullScreenButton() {
        return (ImageButton)findViewById(R.id.btnFullScreen);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {

            goBack();
            return true;
        }

        return true;
    }

    @Override
    public void onBackPressed() {

        if(getCurrentFullScreen()){
            updateFullscreenStatus(false);
            return;
        }
        super.onBackPressed();
    }
    private void goBack() {
        if (getIntent().getData() == null)
            onBackPressed();
        else {
            Intent intent = new Intent(this, MailBoxActivity.class);


            startActivity(intent);
            finish();
        }
    }


    protected void createActionMenu() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        mFragment1 = (MenuFragment) fm.findFragmentByTag("f1");
        if (mFragment1 == null) {
            mFragment1 = new MenuFragment();
            ft.add(mFragment1, "f1");
        }
        ft.commit();

    }

    private void configWebView() {

        mWvBody.getSettings().setJavaScriptEnabled(false);
        mWvBody.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
        mWvBody.getSettings().setDomStorageEnabled(true);
        mWvBody.getSettings().setAllowFileAccess(true);
        mWvBody.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        mWvBody.setScrollbarFadingEnabled(false);



        setWebViewSettings();
        mWvBody.setWebViewClient(new MyWebViewClient());
    }

    @Override
    public void onResume() {
        super.onResume();

        if (m_Data != null) {
            String url = m_Data.toString();
            m_Data = null;
            if (checkIsTheme(url)) {
                return;
            }
            IntentActivity.tryShowUrl(this, mHandler, url, false, true);

        }
    }

    private void showMail(String url) {


        LoadTask getThemeTask = new LoadTask(this);
        getThemeTask.execute(url.replace("|", ""));
    }

    private boolean checkIsTheme(String url) {
        Pattern p = Pattern.compile("http://4pda.ru/forum/index.php\\?act=Msg&CODE=03&VID=in&MSID=(\\d+)");

        Matcher m = p.matcher(url);

        if (m.find()) {
            showMail(url);

            return true;
        }
        return false;
    }

    public void startDeleteMail() {
        DeleteTask deleteTask = new DeleteTask(this);
        deleteTask.execute();
    }

    public Mail getMail() {
        return m_Mail;
    }

    @Override
    public String Prefix() {
        return "theme";  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public WebView getWebView() {
        return mWvBody;
    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, final String url) {

            IntentActivity.tryShowUrl(MailActivity.this, mHandler, url, true, false);

            return true;
        }
    }

    public static final class MenuFragment extends SherlockFragment {
      

        public MenuFragment() {
           
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        private Boolean m_FirstTime=true;
        @Override
        public void onPrepareOptionsMenu(com.actionbarsherlock.view.Menu menu) {
            if(!m_FirstTime)
                getInterface().onPrepareOptionsMenu();
            m_FirstTime=false;
        }

        private MailActivity getInterface() {
            return (MailActivity)getActivity();
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            com.actionbarsherlock.view.MenuItem item = menu.add("Ответить").setIcon(R.drawable.reply);
            item.setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                    MailActivity activity=(MailActivity)getActivity();
                    if(activity.getMail()==null)return false;

                    EditMailActivity.reply(getActivity(),
                            "CODE=04&act=Msg&MID=" + activity.getMail().getUserId() + "&MSID=" + activity.getMail().getId(),
                            activity.getMail().getUser(),
                            activity.getMail().getTheme());
                    return true;
                }
            });
            item.setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_ALWAYS);

            item = menu.add("Удалить").setIcon(android.R.drawable.ic_menu_delete);
            item.setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                    MailActivity activity=(MailActivity)getActivity();
                    if(activity.getMail()==null)return false;
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Подтвердите действие")
                            .setMessage("Вы действительно хотите удалить это сообщение?")
                            .setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ((MailActivity)getActivity()).startDeleteMail();
                                }
                            })
                            .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();

                                }
                            })
                            .create()
                            .show();

                    return true;
                }
            });

            item = menu.add("Закрыть").setIcon(android.R.drawable.ic_menu_close_clear_cancel);
            item.setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                    getActivity().finish();

                    return true;
                }
            });

            //item.setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_ALWAYS);

        }
    }

    private class LoadTask extends AsyncTask<String, Void, Boolean> {

        
        private final ProgressDialog dialog;
        public String Post;


        public LoadTask(Context context) {
           
            dialog = new ProgressDialog(context);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                m_Mail = Mail.load(params[0]);

                return true;
            } catch (Throwable e) {

                ex = e;
                return false;
            }
        }

        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.setMessage("Загрузка сообщения...");
            this.dialog.show();
        }

        private Throwable ex;

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            if (success) {
                MailActivity.this.setTitle(m_Mail.getTheme());
                author_text.setText(m_Mail.getUser());
                date_text.setText(m_Mail.getDate());
                mWvBody.loadDataWithBaseURL("\"file:///android_asset/\"", m_Mail.getHtmlBody(), "text/html", "UTF-8", null);


            } else {
                if (ex != null)
                    Log.e(MailActivity.this, ex);
                else
                    Toast.makeText(MailActivity.this, "Неизвестная ошибка",
                            Toast.LENGTH_SHORT).show();

            }
        }

    }

    public class DeleteTask extends AsyncTask<String, Void, Boolean> {

       
        private final ProgressDialog dialog;
        public String Post;


        public DeleteTask(Context context) {
           
            dialog = new ProgressDialog(context);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                m_Mail.delete();

                return true;
            } catch (Throwable e) {

                ex = e;
                return false;
            }
        }

        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.setMessage("Удаление сообщения...");
            this.dialog.show();
        }

        private Throwable ex;

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            if (success) {
                Toast.makeText(MailActivity.this, "Сообщение удалено", Toast.LENGTH_SHORT).show();
                MailBoxActivity.DeleteMsgId = m_Mail.getId();
                goBack();

            } else {
                if (ex != null)
                    Log.e(MailActivity.this, ex);
                else
                    Toast.makeText(MailActivity.this, "Неизвестная ошибка",
                            Toast.LENGTH_SHORT).show();

            }
        }

    }

}

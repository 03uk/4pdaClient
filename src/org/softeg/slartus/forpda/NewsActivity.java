package org.softeg.slartus.forpda;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
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
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragment;
import org.softeg.slartus.forpda.classes.BrowserViewsFragmentActivity;
import org.softeg.slartus.forpda.classes.History;
import org.softeg.slartus.forpda.classes.common.ExtUrl;
import org.softeg.slartus.forpda.common.Log;

import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: slinkin
 * Date: 07.12.11
 * Time: 8:07
 */
public class NewsActivity extends BrowserViewsFragmentActivity {
    private static final String URL_KEY = "Url";
    private Handler mHandler = new Handler();
    private WebView webView;
    private RelativeLayout pnlSearch;

    private Boolean m_FromHistory = false;
    private int m_ScrollY = 0;
    private int m_ScrollX = 0;
    private ImageButton btnPrevSearch, btnNextSearch, btnCloseSearch;
    private EditText txtSearch;
    private String m_NewsUrl;
    public static String s_NewsUrl = null;
    private Uri m_Data = null;
    private ArrayList<History> m_History = new ArrayList<History>();
    private MenuFragment mFragment1;

    public static void shownews(Context context, String url){
        Intent intent = new Intent(context, NewsActivity.class);
        intent.putExtra(NewsActivity.URL_KEY, url);

        context.startActivity(intent);
    }

    protected void afterCreate(){
        getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.theme);

        createActionMenu();

        webView = (WebView) findViewById(R.id.wvBody);
        registerForContextMenu(webView);
        setWebViewSettings();


        pnlSearch = (RelativeLayout) findViewById(R.id.pnlSearch);
        txtSearch = (EditText) findViewById(R.id.txtSearch);
        txtSearch.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                doSearch(txtSearch.getText().toString());
            }

            public void afterTextChanged(Editable editable) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });
        btnPrevSearch = (ImageButton) findViewById(R.id.btnPrevSearch);
        btnPrevSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                webView.findNext(false);
            }
        });
        btnNextSearch = (ImageButton) findViewById(R.id.btnNextSearch);
        btnNextSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                webView.findNext(true);
            }
        });
        btnCloseSearch = (ImageButton) findViewById(R.id.btnCloseSearch);
        btnCloseSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                closeSearch();
            }
        });

        // webView.setWebChromeClient(new WebChromeClient() );
        Intent intent = getIntent();
        if (intent != null && intent.getData() != null) {
            m_Data = intent.getData();


            return;
        }
        Bundle extras = intent.getExtras();

        m_NewsUrl = extras.getString(URL_KEY);
        s_NewsUrl=m_NewsUrl;

    }


    public ImageButton getFullScreenButton() {
        return (ImageButton)findViewById(R.id.btnFullScreen);
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

    @Override
    public void onResume() {
        super.onResume();



        if (s_NewsUrl != null) {

            s_NewsUrl = null;
            showNews(m_NewsUrl);
        }

        if (m_Data != null) {
            String url = m_Data.toString();
            m_Data = null;
            if (IntentActivity.isNews(url)) {
                showNews(url);
            }
        }
    }

    public WebView getWebView() {
        return webView;
    }

    @Override
    public String Prefix() {
        return "news";
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

            NewsActivity.this.setProgressBarIndeterminateVisibility(true);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);


            NewsActivity.this.setProgressBarIndeterminateVisibility(false);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            m_ScrollY = 0;
            m_ScrollX = 0;

            if(isReplyUrl(url))
                return true;
            
            if (isAnchor(url)) {
                showAnchor(url);
                return true;
            }

            if (IntentActivity.isNews(url)) {
                showNews(url);
                return true;
            }

            IntentActivity.tryShowUrl(NewsActivity.this, mHandler, url, true, false);

            return true;
        }
    }
    
    private Boolean isReplyUrl(String url){
        Matcher m=Pattern.compile("http://4pdaservice.org/(\\d+)/(\\d+)/(.*)").matcher(url);
        if(m.find()){
            respond(m.group(1),m.group(2),m.group(3));
            return true;
        }
        return false;
    }

    private String getPostId() {
        final Pattern pattern = Pattern.compile("http://4pda.ru/\\d{4}/\\d{2}/\\d{2}/(\\d+)");
        Matcher m = pattern.matcher(m_NewsUrl);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    public static Boolean isAnchor(String url) {
        final Pattern pattern = Pattern.compile("http://4pda.ru/\\d{4}/\\d{2}/\\d{2}/\\d+/*#.*");
        return pattern.matcher(url).find();
    }

    private void showAnchor(String url) {
        final Pattern pattern = Pattern.compile("http://4pda.ru/\\d{4}/\\d{2}/\\d{2}/\\d+/*#(.*)");
        Matcher m = pattern.matcher(url);
        if (m.find()) {
            webView.loadUrl("javascript:scrollToElement('" + m.group(1) + "')");
        }
    }

    public boolean onSearchRequested() {
        pnlSearch.setVisibility(View.VISIBLE);
        return false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        WebView.HitTestResult hitTestResult = webView.getHitTestResult();
        switch (hitTestResult.getType()) {
            case WebView.HitTestResult.UNKNOWN_TYPE:
            case WebView.HitTestResult.EDIT_TEXT_TYPE:
                break;
            default:
                ExtUrl.showSelectActionDialog(NewsActivity.this,hitTestResult.getExtra());
        }
    }

    @Override
    public void onBackPressed() {
        if (pnlSearch.getVisibility() == View.VISIBLE) {
            closeSearch();
            return;
        }
        if(getCurrentFullScreen()){
            updateFullscreenStatus(false);
            return;
        }
        if (!m_History.isEmpty()) {
            m_FromHistory = true;
            History history = m_History.get(m_History.size() - 1);
            m_History.remove(m_History.size() - 1);
            m_ScrollX = history.scrollX;
            m_ScrollY = history.scrollY;
            showNews(history.url);
        } else {

            super.onBackPressed();
        }
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doSearch(query);

        }
    }

    private void doSearch(String query) {
        if (TextUtils.isEmpty(query)) return;
        webView.findAll(query);
        try {
            Method m = WebView.class.getMethod("setFindIsUp", Boolean.TYPE);
            m.invoke(webView, true);
        } catch (Throwable ignored) {
        }
        onSearchRequested();
    }

    private void closeSearch() {
        mHandler.post(new Runnable() {
            public void run() {
                webView.findAll("");
                try {
                    Method m = WebView.class.getMethod("setFindIsUp", Boolean.TYPE);
                    m.invoke(webView, false);
                } catch (Throwable ignored) {
                }

                pnlSearch.setVisibility(View.GONE);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(pnlSearch.getWindowToken(), 0);
            }
        });

    }

    private void copyLinkToClipboard(String link) {
        ExtUrl.copyLinkToClipboard(this, link);
    }

    private void showNews(String url) {
        webView.setWebViewClient(new MyWebViewClient());
        saveHistory(url);
        m_NewsUrl = url;
        closeSearch();
        GetNewsTask getThemeTask = new GetNewsTask(this);
        getThemeTask.execute(url.replace("|", ""));
    }

    private void showThemeBody(String body) {
        try {

            setTitle(m_Title);
            webView.loadDataWithBaseURL("\"file:///android_asset/\"", body, "text/html", "UTF-8", null);


        } catch (Exception ex) {
            Log.e(this, ex);
        }
    }

    private void saveHistory(String nextUrl) {
        if (m_FromHistory) {
            m_FromHistory = false;
            return;
        }
//        URI redirectUrl = Client.INSTANCE.getRedirectUri();
//        if (redirectUrl != null)
//            m_History.add(redirectUrl.toString());
//        else
        if (m_NewsUrl != null && !TextUtils.isEmpty(m_NewsUrl) && !m_NewsUrl.equals(nextUrl)) {
            History history = new History();
            history.url = m_NewsUrl;
            history.scrollX = m_ScrollX;
            history.scrollY = m_ScrollY;
            m_History.add(history);
        }
    }


    private String m_Title = "Новости";

    public static final class MenuFragment extends SherlockFragment {



        public MenuFragment() {
            super();
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

        @Override
        public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            com.actionbarsherlock.view.MenuItem item;

            item = menu.add("Комментировать").setIcon(android.R.drawable.ic_menu_send);
            item.setVisible(Client.INSTANCE.getLogined());
            item.setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                    ((NewsActivity) getActivity()).respond();
                    return true;
                }
            });
            item.setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_ALWAYS);

            item = menu.add("Браузер").setIcon(R.drawable.ic_menu_goto);
            item.setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {

                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {
                    Intent marketIntent = new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(((NewsActivity) getActivity()).getUrl()));
                    getActivity().startActivity(Intent.createChooser(marketIntent, "Выберите"));
                    return true;
                }
            });
            item.setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_NEVER);

            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

            com.actionbarsherlock.view.SubMenu optionsMenu = menu.addSubMenu("Настройки");
            optionsMenu.getItem().setIcon(android.R.drawable.ic_menu_preferences);
            optionsMenu.getItem().setTitle("Настройки");
            optionsMenu.add("Масштабировать").setIcon(android.R.drawable.ic_menu_preferences)
                    .setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                            try {
                                prefs.getBoolean("news.ZoomUsing", true);
                                menuItem.setChecked(!menuItem.isChecked());
                                ((NewsActivity)getActivity()).setAndSaveUseZoom( menuItem.isChecked());

                            } catch (Exception ex) {
                                Log.e(getActivity(), ex);
                            }


                            return true;
                        }
                    }).setCheckable(true).setChecked(prefs.getBoolean("news.ZoomUsing", true));

            // if (getInterface() != null && getInterface().getUseZoom())
            {
                optionsMenu.add("Запомнить масштаб").setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                        try {
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("news.ZoomLevel", Integer.toString((int) (getInterface().getWebView().getScale() * 100)));
                            editor.commit();
                            getInterface().getWebView().setInitialScale((int) (getInterface().getWebView().getScale() * 100));
                            Toast.makeText(getActivity(), "Масштаб запомнен", Toast.LENGTH_SHORT).show();
                        } catch (Exception ex) {
                            Log.e(getActivity(), ex);
                        }


                        return true;
                    }
                });
            }

            optionsMenu.add("Загружать изображения").setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                    Boolean loadImagesAutomatically1 = getInterface().getWebView().getSettings().getLoadsImagesAutomatically();
                    getInterface().getWebView().getSettings().setLoadsImagesAutomatically(!loadImagesAutomatically1);
                    menuItem.setChecked(!loadImagesAutomatically1);
                    return true;
                }
            }).setCheckable(true).setChecked(getInterface().getWebView().getSettings().getLoadsImagesAutomatically());

            item = menu.add("Закрыть").setIcon(android.R.drawable.ic_menu_close_clear_cancel);
            item.setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {

                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {
                    getActivity().finish();
                    return true;
                }
            });


        }

        public NewsActivity getInterface() {
            return (NewsActivity)getActivity();
        }
    }

    private String getUrl() {
        return m_NewsUrl;
    }

    private class GetNewsTask extends AsyncTask<String, String, Boolean> {

      
        private final ProgressDialog dialog;
        public String Comment = null;
        public String ReplyId   ;
        public String Dp    ;

        public GetNewsTask(Context context) {
           

            dialog = new ProgressDialog(context);


        }

        private String m_ThemeBody;

        @Override
        protected Boolean doInBackground(String... forums) {
            try {
                if (isCancelled()) return false;
                Client client = Client.INSTANCE;
                if (TextUtils.isEmpty(Comment))
                    m_ThemeBody = transformBody(client.performGet(m_NewsUrl));
                else {
                    Map<String, String> additionalHeaders = new HashMap<String, String>();
                    additionalHeaders.put("comment", Comment);
                    additionalHeaders.put("comment_post_ID", getPostId());
                    additionalHeaders.put("submit", "Отправить комментарий");
                    additionalHeaders.put("comment_reply_ID", ReplyId);
                    additionalHeaders.put("comment_reply_dp", Dp);
                    m_ThemeBody = transformBody(client.performPost("http://4pda.ru/wp-comments-post.php", additionalHeaders, "UTF-8"));


                }
                return true;
            } catch (Throwable e) {
                // Log.e(ThemeActivity.this, e);
                ex = e;
                return false;
            }
        }



        private String transformBody(String body) {
            String cssFile = MyApp.INSTANCE. getThemeCssFileName();
            final String header = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
                    "<html xmlns=\"http://www.w3.org/1999/xhtml\" dir=\"ltr\" lang=\"ru-RU\">\n" +
                    "\n" +
                    "<head profile=\"http://gmpg.org/xfn/11\">\n" +
                    "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n" +
                    "\n" +
                    "<title></title>\n" +
                    "\n" +
                    "<link rel=\"stylesheet\" href=\"file://" + cssFile + "\" type=\"text/css\" media=\"screen, handheld\" />\n" +
                    "<link rel=\"alternate\" type=\"application/rss+xml\" title=\"4PDA RSS лента\" href=\"http://4pda.ru/feed/\" />\n" +
                    "<script type='text/javascript' src='http://4pda.ru/wp-content/plugins/karma/karma.js'></script>\n" +
                    "<script type=\"text/javascript\" src=\"file:///android_asset/theme.js\"></script>\n" +
                    "\n" +
                    "<link rel=\"EditURI\" type=\"application/rsd+xml\" title=\"RSD\" href=\"http://4pda.ru/xmlrpc.php?rsd\" />\n" +
                    "<link rel=\"wlwmanifest\" type=\"application/wlwmanifest+xml\" href=\"http://4pda.ru/wp-includes/wlwmanifest.xml\" /> \n" +

                    "\n" +
                    "</head>\n" +
                    "\n" +
                    "<body><div id=\"main\">";
            final String footer = "</div><br/><br/><br/><br/></body>\n" +
                    "</html>";

            Matcher matcher = Pattern.compile("<title>(.*?)</title>").matcher(body);
            m_Title = "Новости";
            if (matcher.find()) {
                m_Title = Html.fromHtml(matcher.group(1)).toString();
            }

            Matcher m = Pattern.compile("<div id=\"main\">([\\s\\S]*?)<h3 id=\"respond\">Оставить комментарий</h3>").matcher(body);
            if (m.find()) {
                return header + normalizeCommentUrls(m.group(1)) + getNavi(body) + footer;
            }
            m = Pattern.compile("<div id=\"main\">([\\s\\S]*?)<form action=\"(http://4pda.ru)?/wp-comments-post.php\" method=\"post\" id=\"commentform\">").matcher(body);
            if (m.find()) {
                return header + normalizeCommentUrls(m.group(1)) + getNavi(body) + footer;
            }
            m = Pattern.compile("<div id=\"main\">([\\s\\S]*?)<div id=\"categories\">").matcher(body);
            if (m.find()) {
                return header + normalizeCommentUrls(m.group(1)) + getNavi(body) + footer;
            }

            return normalizeCommentUrls(body);
        }


        private String getNavi(String body) {
            String navi = "<P></P><div class=\"navigation\"><div>";

            Matcher matcher = Pattern.compile("<a href=\"/(\\w+)/newer/(\\d+)/\" rel=\"next\">").matcher(body);
            if (matcher.find()) {
                navi += "<a href=\"http://4pda.ru/" + matcher.group(1) + "/newer/" + matcher.group(2) + "/\" rel=\"next\">&#8592;&nbsp;Назад</a> ";
            }

            matcher = Pattern.compile("&nbsp;<a href=\"/(\\w+)/older/(\\d+)/\" rel=\"prev\">").matcher(body);
            if (matcher.find()) {
                navi += "&nbsp;<a href=\"http://4pda.ru/" + matcher.group(1) + "/older/" + matcher.group(2) + "/\" rel=\"prev\">Вперед&nbsp;&#8594;</a> ";
            }
            return navi + "</div/div>";
        }


        private String normalizeCommentUrls(String body) {
            return body.replace("href=\"#comment", "href=\"" + m_NewsUrl + "/#comment")
                    .replace("href=\"/", "href=\"http://4pda.ru/")
                    .replaceAll("<p class=\"(.*?)\"><a href=\"javascript:void\\(0\\)\" onclick=\"movecfm\\(event,(\\d+),(\\d+),'(.*?)'\\);\">ответить</a></p>",
                            "<p class=\"$1\"><a href=\"http://4pdaservice.org/$2/$3/$4\">ответить</a></p>");
        }

        @Override
        protected void onProgressUpdate(final String... progress) {
            mHandler.post(new Runnable() {
                public void run() {
                    if (dialog != null)
                        dialog.setMessage(progress[0]);
                }
            });
        }

        protected void onPreExecute() {
            try {
                this.dialog.setMessage("Загрузка новости...");
                this.dialog.show();
            } catch (Exception ex) {
                Log.e(null, ex);
                this.cancel(true);
            }
        }

        private Throwable ex;

        protected void onPostExecute(final Boolean success) {
            Comment = null;
            try {
                if (this.dialog.isShowing()) {
                    this.dialog.dismiss();
                }
            } catch (Exception ex) {
                Log.e(NewsActivity.this, ex);
            }

            if (isCancelled()) return;
            if (success) {
                showThemeBody(m_ThemeBody);

            } else {
                NewsActivity.this.setTitle(ex.getMessage());
                webView.loadDataWithBaseURL("\"file:///android_asset/\"", m_ThemeBody, "text/html", "UTF-8", null);
                Log.e(NewsActivity.this, ex);
            }
        }


    }

    public void respond() {
        respond("0","0",null);
    }
    public void respond(final String replyId, final String dp, String user) {


        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.news_comment_edit, null);

        final EditText message_edit = (EditText) layout.findViewById(R.id.comment);
        if(user!=null)
            message_edit.setText("<b>"+ URLDecoder.decode(user) +",</b>");
        new AlertDialog.Builder(this)
                .setTitle("Оставить комментарий")
                .setView(layout)
                .setPositiveButton("Отправить", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                        String message = message_edit.getText().toString();
                        if (TextUtils.isEmpty(message.trim())) {
                            Toast.makeText(NewsActivity.this, "Текст не можут быть пустым!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        GetNewsTask getThemeTask = new GetNewsTask(NewsActivity.this);
                        getThemeTask.Comment = message;
                        getThemeTask.ReplyId= replyId;
                        getThemeTask.Dp= dp;
                        getThemeTask.execute(m_NewsUrl);

                    }
                })
                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create().show();
    }

    @Override
    public void onPause(){
        super.onPause();

        webView.setWebViewClient(null);
        webView.setPictureListener(null);
    }


    @Override
    public void onStop(){
        super.onStop();

        webView.setWebViewClient(null);
        webView.setPictureListener(null);
    }


    @Override
    public void onDestroy(){
        super.onDestroy();

        webView.setWebViewClient(null);
        webView.setPictureListener(null);
    }
}

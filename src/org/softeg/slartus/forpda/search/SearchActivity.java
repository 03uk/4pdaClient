package org.softeg.slartus.forpda.search;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import org.softeg.slartus.forpda.BaseFragmentActivity;
import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.MyApp;
import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpda.Tabs.SearchTab;
import org.softeg.slartus.forpda.classes.Forum;
import org.softeg.slartus.forpda.classes.ForumsAdapter;
import org.softeg.slartus.forpda.classes.common.ArrayUtils;
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpdaapi.NotReportException;
import org.softeg.slartus.forpdaapi.OnProgressChangedListener;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 07.10.11
 * Time: 23:32
 * To change this template use File | Settings | File Templates.
 */
public class SearchActivity extends BaseFragmentActivity {
    private EditText username_edit;
    private EditText query_edit;
    private CheckBox chkSubforums, chkTopics, chkSearchInTopic;
    private ImageButton search_button, btnSettins;
    private SearchTab m_SearchTab;
    private SearchResultView m_SearchPostsTab;
    private ISearchResultView m_CurrentResultView;
    private LinearLayout lnrSettings;
    private Spinner spnrSource, spnrSort;
    private Button btnAddForum;
    private Handler mHandler = new Handler();
    private SearchSettings m_SearchSettings;

    public static void startActivity(Context context, String forumId, String forumTitle, String topicId, String query) {
        Intent intent = new Intent(context, SearchActivity.class);
        intent.putExtra("ForumId", forumId);
        intent.putExtra("ForumTitle", forumTitle);
        intent.putExtra("TopicId", topicId);
        intent.putExtra("Query", query);
        context.startActivity(intent);
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, android.view.View v,
                                    android.view.ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (m_CurrentResultView != null)
            m_CurrentResultView.onCreateContextMenu(menu, v, menuInfo, mHandler);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.search_activity);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        View customNav = LayoutInflater.from(this).inflate(R.layout.search_activity_panel, null);
        getSupportActionBar().setCustomView(customNav);
        getSupportActionBar().setDisplayShowCustomEnabled(true);


        query_edit = (EditText) customNav.findViewById(R.id.query_edit);
        search_button = (ImageButton) customNav.findViewById(R.id.btnSearch);

        m_SearchSettings = new SearchSettings(this, "SearchThemes");
        Boolean searchStartIntent = m_SearchSettings.tryFill(getIntent());
        if (!searchStartIntent)
            m_SearchSettings.loadSettings();


        spnrSource = (Spinner) findViewById(R.id.spnrSource);
        spnrSort = (Spinner) findViewById(R.id.spnrSort);
        spnrSort.setSelection(ArrayUtils.indexOf(m_SearchSettings.getSort(), getResources().getStringArray(R.array.SearchSortValues)));

        lnrSettings = (LinearLayout) findViewById(R.id.lnrSettings);

        username_edit = (EditText) findViewById(R.id.username_edit);
        username_edit.setText(m_SearchSettings.getUserName());

        query_edit.setText(m_SearchSettings.getQuery());
        query_edit.selectAll();
        query_edit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_NULL || actionId == EditorInfo.IME_ACTION_DONE) {
                    search();
                } else if (keyEvent != null && keyEvent.getAction() == KeyEvent.KEYCODE_SEARCH) {
                    search();
                } else if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    search();
                }
                return true;
            }
        });
        btnAddForum = (Button) findViewById(R.id.btnAddForum);
        m_CheckedIds = m_SearchSettings.getCheckedIds();
        setForumButtonText();
        btnAddForum.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (m_MainForum == null) {
                    loadForums();
                } else {
                    showForums();
                }
            }
        }

        );

        chkTopics = (CheckBox) findViewById(R.id.chkTopics);
        chkTopics.setChecked(m_SearchSettings.getResultsInTopicView());

        chkSubforums = (CheckBox) findViewById(R.id.chkSubforums);
        chkSubforums.setChecked(m_SearchSettings.Subforums());

        btnSettins = (ImageButton) findViewById(R.id.btnSettins);
        btnSettins.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                lnrSettings.setVisibility(lnrSettings.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
            }
        }

        );


        search_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                search();
            }
        }

        );

        chkSearchInTopic = (CheckBox) findViewById(R.id.chkSearchInTopic);
        chkSearchInTopic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                spnrSource.setEnabled(!b);
                // spnrSort.setEnabled(!b);
                btnAddForum.setEnabled(!b);
                chkSubforums.setEnabled(!b);
                chkTopics.setEnabled(!b);
            }
        });
        if (m_SearchSettings.isSearchInTopic()) {
            chkSearchInTopic.setVisibility(View.VISIBLE);
            chkSearchInTopic.setChecked(true);
        }

        if (searchStartIntent)
            searchInTopic();

    }



    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (m_CurrentResultView != null && (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN))
            return m_CurrentResultView.dispatchKeyEvent(event);

        return super.dispatchKeyEvent(event);

    }

    private void addSearchTab() {
        if (m_SearchTab != null) return;

        ((LinearLayout) findViewById(R.id.lnrThemes)).removeAllViews();
        m_SearchPostsTab = null;
        m_SearchTab = new SearchTab(this, "SearchThemes");
        m_CurrentResultView = m_SearchTab;
        m_SearchTab.getListView().setOnCreateContextMenuListener(this);
        ((LinearLayout) findViewById(R.id.lnrThemes)).addView(m_SearchTab, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
    }

    private void addSearchPosts() {
        if (m_SearchPostsTab != null) return;

        ((LinearLayout) findViewById(R.id.lnrThemes)).removeAllViews();
        m_SearchTab = null;
        m_SearchPostsTab = new SearchResultView(this);
        m_CurrentResultView = m_SearchPostsTab;
        registerForContextMenu(m_SearchPostsTab.getWebView());
        ((LinearLayout) findViewById(R.id.lnrThemes)).addView(m_SearchPostsTab, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
    }

    private void searchInTopic() {
        hideKeybord(username_edit);
        hideKeybord(query_edit);
        addSearchPosts();
        m_SearchPostsTab.search(m_SearchSettings);
    }

    private void search() {
        search(chkTopics.isChecked() && !chkSearchInTopic.isChecked());
    }

    private void search(Boolean topicsResultView) {
        hideKeybord(username_edit);
        hideKeybord(query_edit);
        SearchActivity.this.lnrSettings.setVisibility(View.GONE);


        m_SearchSettings.fillAndSave(query_edit.getText().toString(),
                username_edit.getText().toString(),
                getResources().getStringArray(R.array.SearchSourceValues)[SearchActivity.this.spnrSource.getSelectedItemPosition()],
                getResources().getStringArray(R.array.SearchSortValues)[SearchActivity.this.spnrSort.getSelectedItemPosition()],
                chkSubforums.isChecked(), m_CheckedIds, chkSearchInTopic.isChecked(),topicsResultView);

        if (!topicsResultView) {
            addSearchPosts();

        } else {
            addSearchTab();
        }

        m_CurrentResultView.search(m_SearchSettings);
    }
    private Forum m_MainForum=null;
    private void loadForums() {
        final ProgressDialog dialog = new ProgressDialog(SearchActivity.this);
        dialog.setCancelable(false);
        dialog.show();
        new Thread(new Runnable() {
            public void run() {
                try {
                    m_MainForum=Client.INSTANCE.loadForums(new OnProgressChangedListener() {
                        public void onProgressChanged(final String state) {
                            mHandler.post(new Runnable() {
                                public void run() {
                                    dialog.setMessage(state);
                                }
                            });
                        }

                        public void cancel() {

                        }

                        public void checkCanceled() throws NotReportException {

                        }
                    });
                    mHandler.post(new Runnable() {
                        public void run() {
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            showForums();
                        }
                    });

                } catch (Exception e) {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    Log.e(SearchActivity.this, e);
                }
            }
        }).start();
    }

    private Hashtable<String, CharSequence> m_CheckedIds = new Hashtable<String, CharSequence>();
    private ArrayList<String> m_VisibleIds = new ArrayList<String>();
    private ArrayList<CharSequence> forumCaptions;

    private void showForums() {
        if (forumCaptions == null) {
            forumCaptions = new ArrayList<CharSequence>();
            addForumCaptions(forumCaptions, m_MainForum, null, "");
        }
        LayoutInflater factory = LayoutInflater.from(this);
        final View view = factory.inflate(R.layout.search_froums_list, null);

        ListView lstTree = (ListView) view.findViewById(R.id.lstTree);
        ForumsAdapter adapter = new ForumsAdapter(this, R.layout.search_forum_item, forumCaptions, m_CheckedIds, m_VisibleIds);

        lstTree.setAdapter(adapter);


        new AlertDialog.Builder(new ContextThemeWrapper(this, MyApp.INSTANCE.getThemeStyleResID()))
                .setTitle("Форумы")
                .setView(view)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        setForumButtonText();
                    }
                })
                .create().show();

    }

    private void setForumButtonText() {
        StringBuilder sb = new StringBuilder();
        Enumeration<String> keys = m_CheckedIds.keys();
        for (int k = 0; k < m_CheckedIds.size(); k++) {
            String key = keys.nextElement();
            if (key.equals("all")) {
                sb.append("Все форумы;");
            } else {
                sb.append(m_CheckedIds.get(key));
//                Forum forum = Client.INSTANCE.MainForum.findById(key, true, false);
//                sb.append(forum.getTitle() + ";");
            }
        }
        if (sb.toString().equals(""))
            sb.append("Все форумы");
        btnAddForum.setText(sb.toString());
    }

    private void addForumCaptions(ArrayList<CharSequence> forumCaptions, Forum forum, Forum parentForum, String node) {
        if (parentForum == null) {
            forumCaptions.add(">> Все форумы");
            m_VisibleIds.add("all");
        } else if (!parentForum.getId().equals(forum.getId())) {
            forumCaptions.add(node + forum.getTitle());
            m_VisibleIds.add(forum.getId());
        }
        if (parentForum == null)
            node = "  ";
        else if (node.trim().equals(""))
            node = "  |--";
        else
            node = node + "--";
        int childSize = forum.getForums().size();

        for (int i = 0; i < childSize; i++) {
            addForumCaptions(forumCaptions, forum.getForums().get(i), forum, node);
        }
    }

    private void hideKeybord(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


}

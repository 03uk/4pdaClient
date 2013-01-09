package org.softeg.slartus.forpda.search;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import org.softeg.slartus.forpda.Tabs.TabDataSettingsActivity;
import org.softeg.slartus.forpda.common.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Created by IntelliJ IDEA.
 * User: slartus
 * Date: 23.10.12
 * Time: 22:23
 * To change this template use File | Settings | File Templates.
 */
public class SearchSettings {

    private Context m_Context;
    private String m_TabTag;
    private String m_TopicId;
    private Boolean m_IsSearchInTopic = false;

    public SearchSettings(Context context, String tabTag) {

        m_Context = context;
        m_TabTag = tabTag;
    }

    private Hashtable<String, CharSequence> m_CheckedIds = new Hashtable<String, CharSequence>();
    private String m_Query;

    public String getQuery() {
        return m_Query;
    }

    private String m_UserName;

    public String getUserName() {
        return m_UserName;
    }

    private String m_Source;

    public String getSource() {
        return m_Source;
    }

    private String m_Name = "Поиск";

    public String getName() {
        return m_Name;
    }

    private String m_Sort = "dd";

    public String getSort() {
        return m_Sort;
    }

    private Boolean m_Subforums = false;
    private Boolean m_ResultsInTopicView = false;
    public Boolean Subforums() {
        return m_Subforums;
    }

    public Hashtable<String, CharSequence> getCheckedIds() {
        return m_CheckedIds;

    }

    public Boolean isSearchInTopic() {
        return m_IsSearchInTopic;
    }

    public String getSearchQuery(String results) {
        // http://4pda.ru/forum/index.php?forums%5B%5D=285&topics%5B%5D=271502&act=search&source=pst&query=remie
        String params = "";
        if (m_IsSearchInTopic) {
            params += "&source=pst";
            params += "&topics%5B%5D=" + m_TopicId;
        } else {
            params += "&source=" + m_Source;
            params += "&subforums=" + (m_Subforums ? "1" : "0");
        }

        params += "&sort=" + m_Sort;
        Enumeration<String> keys = m_CheckedIds.keys();
        for (int i = 0; i < m_CheckedIds.size(); i++) {
            String key = keys.nextElement();

            params += "&forums%5B%5D=" + key;
        }
        if (m_CheckedIds.size() == 0) {
            params += "&forums%5B%5D=all";
        }
        if (!TextUtils.isEmpty(m_Query))
            params += "&query=" + tryUrlEncode(m_Query);

        if (!TextUtils.isEmpty(m_UserName))
            params += "&username=" + tryUrlEncode(m_UserName);


        return "http://4pda.ru/forum/index.php?act=search&query=" + "&result=" + results
                + params;

    }

    private String tryUrlEncode(String url) {
        try {
            return URLEncoder.encode(url, "windows-1251");
        } catch (UnsupportedEncodingException e) {
            Log.e(m_Context, e);
            return url;
        }
    }

    public void fillAndSave(String query, String userName, String source, String sort, Boolean subforums,
                            Hashtable<String, CharSequence> checkedIds, 
                            Boolean searchInTopic,
                            Boolean resultsInTopicsView) {
        m_IsSearchInTopic = searchInTopic;
        m_CheckedIds = checkedIds;
        m_Query = query;
        m_UserName = userName;
        m_Source = source;
        m_Sort = sort;
        m_Subforums = subforums;
        m_ResultsInTopicView=resultsInTopicsView;
        if (!m_IsSearchInTopic)
            saveSettings();
    }


    public void loadSettings() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(m_Context);
        m_Source = preferences.getString(m_TabTag + ".Template.Source", "all");
        m_Name = preferences.getString(m_TabTag + ".Template.Name", "Последние");

        m_Sort = preferences.getString(m_TabTag + ".Template.Sort", "dd");
        m_UserName = preferences.getString(m_TabTag + ".Template.UserName", "");
        m_Query = preferences.getString(m_TabTag + ".Template.Query", "");

        loadChecks(preferences.getString(m_TabTag + ".Template.Forums", "281µAndroid¶"));
        m_Subforums = preferences.getBoolean(m_TabTag + ".Template.Subforums", true);
        m_ResultsInTopicView = preferences.getBoolean(m_TabTag + ".Template.ResultsInTopicView", false);

        if (TextUtils.isEmpty(m_Name)) {
            if (m_Source.equals("all") && m_Sort.equals("dd") && TextUtils.isEmpty(m_UserName)
                    && (m_CheckedIds.size() == 0) && m_Subforums)
                m_Name = "Последние";
            else
                m_Name = "Поиск";
        }
    }

    private void loadChecks(String checksString) {
        m_CheckedIds = new Hashtable<String, CharSequence>();
        if (TextUtils.isEmpty(checksString)) return;
        try {
            String[] pairs = checksString.split(TabDataSettingsActivity.pairsDelimiter);
            for (int i = 0; i < pairs.length; i++) {
                String pair = pairs[i];
                if (TextUtils.isEmpty(pair)) continue;
                String[] vals = pair.split(TabDataSettingsActivity.pairValuesDelimiter);
                m_CheckedIds.put(vals[0], vals[1]);
            }
        } catch (Exception ex) {
            Log.e(m_Context, ex);
        }

    }

    public void saveSettings() {

        String tabTag = m_TabTag;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(m_Context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(tabTag + ".Template.Source", m_Source);
        editor.putString(tabTag + ".Template.Name", m_Name);

        editor.putString(tabTag + ".Template.Sort", m_Sort);
        editor.putString(tabTag + ".Template.UserName", m_UserName);
        editor.putString(tabTag + ".Template.Query", m_Query);
        editor.putString(tabTag + ".Template.Forums", TabDataSettingsActivity.getCheckedIdsString(m_CheckedIds));
//        editor.putString(tabTag + ".Template.Forums",m_Source);
        editor.putBoolean(tabTag + ".Template.Subforums", m_Subforums);
        editor.putBoolean(tabTag + ".Template.ResultsInTopicView", m_ResultsInTopicView);
//

        editor.commit();
    }

    public Boolean tryFill(Intent intent) {
        Boolean res = false;
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                if (extras.containsKey("ForumId")) {
                    m_CheckedIds = new Hashtable<String, CharSequence>();
                    String forumTitle = extras.containsKey("ForumTitle") ? extras.getString("ForumTitle") : extras.getString("ForumId");
                    m_CheckedIds.put(extras.getString("ForumId"), forumTitle);
                    res = true;
                }
                if (extras.containsKey("TopicId")) {
                    m_TopicId = extras.getString("TopicId");
                    m_Sort = "rel";
                    m_IsSearchInTopic = true;
                    res = true;
                }
                if (extras.containsKey("Query")) {
                    m_Query = extras.getString("Query");
                    res = true;
                }
            }
        }

        return res;
    }

    public boolean getResultsInTopicView() {
        return m_ResultsInTopicView;
    }
}

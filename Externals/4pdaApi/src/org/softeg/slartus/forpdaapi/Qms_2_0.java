package org.softeg.slartus.forpdaapi;

import android.text.Html;
import android.text.TextUtils;

import javax.net.ssl.CertPathTrustManagerParameters;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 04.02.13
 * Time: 14:52
 * To change this template use File | Settings | File Templates.
 */
public class Qms_2_0 {

    public static String getChat(IHttpClient httpClient, String mid, String themeId) throws Throwable {
        String pageBody = httpClient.performGet("http://4pda.ru/forum/index.php?act=qms&mid=" + mid + "&t=" + themeId + "&small=1");
        return matchChatBody(pageBody);
    }

    private static String matchChatBody(String pageBody) {
        Matcher m = Pattern.compile("<div class=\"thread_timeline\">([\\s\\S]*?)<script type=\"text/javascript\">init_mess_acts\\(\\);").matcher(pageBody);
        if (m.find())
            return "<div class=\"thread_timeline\">" + m.group(1);
        return pageBody;
    }

    public static String sendMessage(IHttpClient httpClient, String mid, String tid, String message) throws Throwable {
        Map<String, String> additionalHeaders = new HashMap<String, String>();
        additionalHeaders.put("js", "1");
        additionalHeaders.put("act", "qms");
        additionalHeaders.put("mid", mid);
        additionalHeaders.put("t", tid);
        additionalHeaders.put("send", "1");
        additionalHeaders.put("message", message);
       httpClient.performPost("http://4pda.ru/forum/index.php?act=qms&mid=" + mid + "&t=" + tid + "&small=1", additionalHeaders,"UTF-8");
        return getChat(httpClient, mid, tid);
    }

    public static String createThread(IHttpClient httpClient, String username, String title, String message,
                                      Map<String, String> outParams) throws IOException {
        Map<String, String> additionalHeaders = new HashMap<String, String>();
        additionalHeaders.put("act", "qms");
        additionalHeaders.put("create_thread", "1");
        additionalHeaders.put("create", "1");
        additionalHeaders.put("username", username);
        additionalHeaders.put("title", title);
        additionalHeaders.put("message", message);
        String pageBody = httpClient.performPost("http://4pda.ru/forum/index.php?act=qms", additionalHeaders,"UTF-8");

        Matcher m = Pattern.compile("<input type=\"hidden\" name=\"mid\" value=\"(\\d+)\" />").matcher(pageBody);
        if (m.find())
            outParams.put("mid", m.group(1));
        m = Pattern.compile("<input type=\"hidden\" name=\"t\" value=\"(\\d+)\" />").matcher(pageBody);
        if (m.find())
            outParams.put("t", m.group(1));
        m = Pattern.compile("<strong>(.*?)</strong></a>:\\s*(.*?)</span>").matcher(pageBody);
        if (m.find()) {
            outParams.put("user", m.group(1));
            outParams.put("title", m.group(2));
        }
        if (outParams.size() == 0) {
            m = Pattern.compile("<div class=\"form-error\">(.*?)</div>").matcher(pageBody);
            if (m.find())
                throw new NotReportException(m.group(1));
        }
        return matchChatBody(pageBody);
    }

    public static void deleteDialogs(IHttpClient httpClient, String mid, List<String> ids) throws IOException {
        Map<String, String> additionalHeaders = new HashMap<String, String>();
        additionalHeaders.put("act", "qms");
        additionalHeaders.put("mid", mid);
        additionalHeaders.put("action", "delete_threads");
        for (String id : ids) {
            additionalHeaders.put("ids[" + id + "]", id);
        }
        httpClient.performPost("http://4pda.ru/forum/index.php?act=qms", additionalHeaders);
    }

    public static QmsUsers getQmsSubscribers(IHttpClient httpClient) throws Throwable {
        String pageBody = httpClient.performGet("http://4pda.ru/forum/index.php?act=qms&small=1");
        QmsUsers res = new QmsUsers();
        return parseQmsUsers(pageBody, res);
    }

    private static QmsUsers parseQmsUsers(String pageBody, QmsUsers res) {
        Matcher m = Pattern.compile("<a href=\"\\?act=qms&mid=(\\d+)\" title=\".*?\">((<strong>(.*?)\\((\\d+)\\)</strong>)|((.*?)))</a>").matcher(pageBody);

        while (m.find()) {
            QmsUser qmsUser = new QmsUser();
            qmsUser.setMid(m.group(1));
            if (!TextUtils.isEmpty(m.group(3))) {
                qmsUser.setNick(Html.fromHtml(m.group(4)).toString().trim());
                qmsUser.setNewMessagesCount(m.group(5));
            } else {
                qmsUser.setNick(Html.fromHtml(m.group(2)).toString().trim());
            }

            res.add(qmsUser);
        }
        return res;
    }

    public static QmsUserThemes getQmsUserThemes(IHttpClient httpClient, String mid,
                                                 QmsUsers outUsers, Boolean parseNick) throws Throwable {
        String pageBody = httpClient.performGet("http://4pda.ru/forum/index.php?act=qms&mid=" + mid);
        Matcher m = Pattern.compile("<a class=\"threads_title\" href=\"\\?act=qms&mid=" + mid + "&t=(\\d+)\">((<strong>(.*?)\\s*\\((\\d+)/(\\d+)\\)</strong>)|((.*?)\\s*\\((\\d+)\\)))</a>\\s*?<div class=\"threads_update\">(.*?)</div>").matcher(pageBody);
        QmsUserThemes res = new QmsUserThemes();
        while (m.find()) {
            QmsUserTheme item = new QmsUserTheme();
            item.Id = m.group(1);
            if (m.group(4) != null) {
                item.Title = m.group(4);
                item.NewCount = m.group(5);
                item.Count = m.group(6);
            } else {
                item.Title = m.group(8);
                item.Count = m.group(9);
            }

            item.Date = m.group(10);

            res.add(item);
        }
        parseQmsUsers(pageBody, outUsers);
        if (parseNick) {
            m = Pattern.compile("src=\"http://s.4pda.ru/forum/style_images/qms/back.png\"[\\s\\S]*?<strong>(.*?)</strong></a>").matcher(pageBody);
            if (m.find()) {
                res.Nick = Html.fromHtml(m.group(1)).toString();
            } else {
                m = Pattern.compile("<span class=\"title\">Диалоги с: (.*?)</span>").matcher(pageBody);
                if (m.find())
                    res.Nick = Html.fromHtml(m.group(1)).toString();
            }
        }
        return res;
    }
}

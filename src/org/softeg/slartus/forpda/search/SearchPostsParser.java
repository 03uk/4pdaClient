package org.softeg.slartus.forpda.search;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import org.softeg.slartus.forpda.EditPost;
import org.softeg.slartus.forpda.MyApp;
import org.softeg.slartus.forpda.classes.Exceptions.MessageInfoException;
import org.softeg.slartus.forpda.classes.Post;
import org.softeg.slartus.forpda.classes.TopicBodyBuilder;
import org.softeg.slartus.forpda.classes.common.Functions;
import org.softeg.slartus.forpdaapi.NotReportException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 22.10.12
 * Time: 12:54
 * To change this template use File | Settings | File Templates.
 */
public class SearchPostsParser {
    private boolean m_SpoilerByButton = false;

    public SearchPostsParser() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getContext());
        m_SpoilerByButton = prefs.getBoolean("theme.SpoilerByButton", false);
    }

    public SearchResult searchResult;

    public String parse(String body) throws MessageInfoException, NotReportException {

        Matcher m = Pattern.compile("<div class=\"borderwrap-header\">([\\s\\S]*?)<br /><div class=\"borderwrap\">([\\s\\S]*?)").matcher(body);
        if (!m.find()) {
            if (body.indexOf("К сожалению, Ваш поиск не дал никаких результатов") > 0)
                throw new MessageInfoException("Поиск", "К сожалению, Ваш поиск не дал никаких результатов.\n" +
                        "Попробуйте расширить параметры поиска, используя другое ключевое слово или изменением формата поиска.");
            else
                throw new NotReportException("Ошибка разбора результатов поиска");
        }
        searchResult = createSearchResult(m.group(1));
        StringBuilder sb = new StringBuilder();
        beginTopic(sb, searchResult);

        parsePosts(sb, body);

        endTopic(sb, searchResult);
        return sb.toString();
    }

    private SearchResult createSearchResult(String page) {


        final Pattern pagesCountPattern = Pattern.compile("var pages = parseInt\\((\\d+)\\);");
        // http://4pda.ru/forum/index.php?act=search&source=all&result=posts&sort=rel&subforums=1&query=pda&forums=281&st=90
        final Pattern lastPageStartPattern = Pattern.compile("(http://4pda.ru)?/forum/index.php\\?act=Search.*?st=(\\d+)");
        final Pattern currentPagePattern = Pattern.compile("<span class=\"pagecurrent\">(\\d+)</span>");
        String str = page;

        SearchResult searchResult = new SearchResult();

        Matcher m = pagesCountPattern.matcher(str);
        if (m.find()) {
            searchResult.setPagesCount(m.group(1));
        }

        m = lastPageStartPattern.matcher(str);
        while (m.find()) {
            searchResult.setLastPageStartCount(m.group(2));
        }

        m = currentPagePattern.matcher(str);
        if (m.find()) {
            searchResult.setCurrentPage(m.group(1));
        } else
            searchResult.setCurrentPage("1");
        return searchResult;
    }

    public void beginTopic(StringBuilder m_Body, SearchResult searchResult) {
        m_Body.append("<html xml:lang=\"en\" lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\">\n");
        m_Body.append("<head>\n");
        m_Body.append("<meta http-equiv=\"content-type\" content=\"text/html; charset=windows-1251\" />\n");
        EditPost.addStyleSheetLink(m_Body);
        m_Body.append("<script type=\"text/javascript\" src=\"file:///android_asset/theme.js\"></script>\n");
        m_Body.append("<script type=\"text/javascript\" src=\"file:///android_asset/blockeditor.js\"></script>\n");

        m_Body.append("</head>\n");
        m_Body.append("<body>\n");

        if (searchResult.getPagesCount() > 1) {
            TopicBodyBuilder.addButtons(m_Body, searchResult.getCurrentPage(), searchResult.getPagesCount(), Functions.isWebviewAllowJavascriptInterface(MyApp.INSTANCE), true);
        }
        m_Body.append("<br/><br/>");

    }

    public void endTopic(StringBuilder m_Body, SearchResult searchResult) {
        m_Body.append("<div id=\"entryEnd\"></div>\n");
        m_Body.append("<br/><br/>");
        if (searchResult.getPagesCount() > 1) {
            TopicBodyBuilder.addButtons(m_Body, searchResult.getCurrentPage(), searchResult.getPagesCount(), Functions.isWebviewAllowJavascriptInterface(MyApp.INSTANCE), true);
        }

        m_Body.append("<br/><br/>");

        m_Body.append("<br/><br/><br/><br/><br/><br/>\n");
        m_Body.append("</body>\n");
        m_Body.append("</html>\n");

    }


    private StringBuilder parsePosts(StringBuilder m_Body, String page) {
        Matcher postMatcher = Pattern.compile("<div class=\"borderwrap\">([\\s\\S]*?)</table>\\s*?</div>").matcher("<div class=\"borderwrap\">" + page);

        while (postMatcher.find()) {
            m_Body.append(parsePost(postMatcher.group(1)));
        }

        return m_Body;
    }

    private final String POST_TEMPLATE = "<div class=\"between_messages\"></div>\n" +
            "<div class=\"topic_title_post\">%1s</div>\n" +
            "<div class=\"post_header\">\n" +
            "\t<table width=\"100%%\">\n" +
            "\t\t<tr><td>%2s</td>\n" +
            "\t<td><div align=\"right\"><span class=\"post_date_cli\">%3s</span></div></td>\n" +
            "</tr>\n" +
//            "<tr>\n" +
//            "<td><a href=\"http://www.HTMLOUT.ru/showRepMenu?val0=12862922&val1=2142518&val2=sunopera&val3=1&val4=1\"  class=\"system_link\" ><span class=\"post_date_cli\">Реп(0)</span></a></td>\n" +
//            "\t\t\t<td><div align=\"right\"><a href=\"http://www.HTMLOUT.ru/showPostMenu?val0=12862922&val1=0&val2=0\" class=\"system_link\">меню</a></div></td>\t\t" +
//            "</tr>\t" +
            "</table>" +
            "</div>" +
            "<div class=\"post_body\">%4s</div>" +
            "<div class=\"s_post_footer\"><table width=\"100%%\"><tr><td>%5s</td></tr></table></div>";
    private final Pattern postPattern = Pattern.compile("<div class=\"maintitle\"><a href=\"/forum/index.php\\?showtopic=(\\d+)\">(.*?)</a></div>[\\s\\S]*?<span class=\"normalname\"><a href=\"/forum/index.php\\?showuser=(\\d+)\">(.*?)</a></span>[\\s\\S]*?<img src=\"http://s.4pda.ru/forum/style_images/1/to_post_off.gif\" alt=\"\" border=\"0\" style=\"padding-bottom:2px\" />(.*?)</span>[\\s\\S]*?<font color=\".*?\">\\[(.*)?\\]</font>[\\s\\S]*?<div class=\"postcolor\" id=\"post-\\d+\" style=\"height:300px;overflow-x:auto;\">([\\s\\S]*?)</div></td>\\s+</tr>\\s+<tr>\\s+<td class=\"row2\"></td>\\s+<td class=\"row2\">([\\s\\S]*?)</td>");

    private String parsePost(String page) {
        Matcher postMatcher = postPattern.matcher(page);
        if (!postMatcher.find())
            return "";
        String userState = "online".equals(postMatcher.group(6)) ? "post_nick_online_cli" : "post_nick_cli";
        Boolean isWebviewAllowJavascriptInterface = Functions.isWebviewAllowJavascriptInterface(MyApp.INSTANCE);
        String s1 = "<a href=\"http://4pda.ru/forum/index.php?showtopic=" + postMatcher.group(1) + "\">" + postMatcher.group(2) + "</a>";
        String s2 = "<span class=\"" + userState + "\"><a " + TopicBodyBuilder.getHtmlout(isWebviewAllowJavascriptInterface, "showUserMenu",
                postMatcher.group(3), postMatcher.group(4)) + " class=\"system_link\">" + postMatcher.group(4) + "</a></span>";
        String s3 = postMatcher.group(5);
        String s4 = Post.modifyBody(postMatcher.group(7)).replace("<br /><br />--------------------<br />", "");

        if (m_SpoilerByButton) {
            String find = "(<div class='hidetop' style='cursor:pointer;' )" +
                    "(onclick=\"var _n=this.parentNode.getElementsByTagName\\('div'\\)\\[1\\];if\\(_n.style.display=='none'\\)\\{_n.style.display='';\\}else\\{_n.style.display='none';\\}\">)" +
                    "(Спойлер \\(\\+/-\\).*?</div>)" +
                    "(\\s*<div class='hidemain' style=\"display:none\">)";
            String replace = "$1>$3<input class='spoiler_button' type=\"button\" value=\"+\" onclick=\"toggleSpoilerVisibility\\(this\\)\"/>$4";
            s4 = s4.replaceAll(find, replace);
        }
        String s5 = Post.modifyBody(postMatcher.group(8));
        return String.format(POST_TEMPLATE, s1, s2, s3, s4, s5);
    }


}

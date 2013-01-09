package org.softeg.slartus.forpda.Tabs;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.classes.Forum;
import org.softeg.slartus.forpda.classes.Topic;
import org.softeg.slartus.forpdaapi.OnProgressChangedListener;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: slartus
 * Date: 27.10.11
 * Time: 20:38
 */
public class CatalogTab extends TreeTab {
    public static final String TITLE = "Каталог";

    public String getTitle() {
        return "Каталог";
    }

    public String getTemplate() {
        return Tabs.TAB_FORUMS;
    }

    public CatalogTab(Context context, String tabTag) {
        super(context, tabTag);
    }

    @Override
    protected void loadForum(Forum forum, OnProgressChangedListener progressChangedListener) throws Exception {

        loadCatalog(forum, progressChangedListener);

    }

    @Override
    protected Boolean isShowForumTitle() {
        return true;
    }


    @Override
    protected void getThemes(OnProgressChangedListener progressChangedListener) throws IOException {
        if(m_ForumForLoadThemes.getParent()==null)
            return ;
        if (m_Themes.size() == 0) {
            if (m_ForumForLoadThemes.getTag() != null && m_ForumForLoadThemes.getTag().equals("games")) {
                if (m_ForumForLoadThemes.level == 1 && m_ForumForLoadThemes.getForums().size() == 0) {
                    loadGameCategoryThemes(m_ForumForLoadThemes, m_ForumForLoadThemes.getHtmlTitle(), progressChangedListener);
                } else if (m_ForumForLoadThemes.getParent().getId().equals(m_ForumForLoadThemes.getId()))
                    loadGameCategoryThemes(m_ForumForLoadThemes, m_ForumForLoadThemes.getParent().getHtmlTitle(), progressChangedListener);
                else
                    loadGameSubCategoryThemes(m_ForumForLoadThemes, m_ForumForLoadThemes.getParent().getHtmlTitle(), progressChangedListener);

            } else {
                if (m_ForumForLoadThemes.getParent().getId().equals(m_ForumForLoadThemes.getId()))
                    loadCategoryThemes(m_ForumForLoadThemes, m_ForumForLoadThemes.getParent().getTitle(), progressChangedListener);
                else
                    loadSubCategoryThemes(m_ForumForLoadThemes.getParent(),m_ForumForLoadThemes, progressChangedListener);
            }

            m_Themes = m_ForumForLoadThemes.getThemes();
            Collections.sort(m_Themes, new Comparator<Topic>() {
                public int compare(Topic topic, Topic topic1) {
                    return topic.getTitle().toString().toUpperCase().compareTo(topic1.getTitle().toString().toUpperCase());
                }
            });
        }
    }

    private static final String appCatalogUrl = "http://4pda.ru/forum/index.php?showtopic=112220";
    private static final String gameCatalogUrl = "http://4pda.ru/forum/index.php?showtopic=117270";

    private void loadCatalog(Forum catalog, OnProgressChangedListener progressChangedListener) throws Exception {
        Exception appException=null;
        Forum appCatalog = new Forum("112220", "Программы");
        try{
            loadAppCatalog(appCatalog, progressChangedListener);
            catalog.addForum(appCatalog);
        }
        catch (Exception ex){
            appException=ex;
            
        }

        Forum gameCatalog = new Forum("117270", "Игры");
        gameCatalog.setTag("games");
        try{
            loadGameCatalog(gameCatalog, progressChangedListener);
            catalog.addForum(gameCatalog);
        }
        catch (Exception ex){
            throw new Exception("Каталог игр: "+ ex.getMessage(), ex);
        }
        if(appException!=null)
            throw new Exception("Каталог программ: "+ appException.getMessage(), appException);
    }

    private void loadAppCatalog(Forum catalog, OnProgressChangedListener progressChangedListener) throws IOException {

        String pageBody = Client.INSTANCE.loadPageAndCheckLogin(appCatalogUrl, progressChangedListener);
        Matcher contentMatcher=Pattern.compile("<a name=\"entry7958338\">([\\s\\S]*)?<!-- TABLE FOOTER -->").matcher(pageBody);
        if(!contentMatcher.find()){
            throw new IOException("Не найден пост с содержанием");

        }
        String contentPost=contentMatcher.group(1);
        
       
        Matcher categoryMatcher=Pattern.compile("<li>.*?p=(\\d+).*?<b>(.*?)</b>(.*?)(<br /></li>|</ol>)").matcher(contentPost);
        Pattern subCategoryPattern= Pattern.compile("<b>(.*?)</b>");

       
        int id = -1;
        while (categoryMatcher.find()) {
            Forum category = new Forum(categoryMatcher.group(1), categoryMatcher.group(2));
            category.addForum(new Forum(category.getId(), category.getTitle() + " @ темы"));
            catalog.addForum(category);
            
            Matcher m=subCategoryPattern.matcher(categoryMatcher.group(3))  ;
            while(m.find()){
                Forum subcategory = new Forum(Integer.toString(id++), m.group(1));

                category.addForum(subcategory);
            }

        }


    }

    private void loadGameCatalog(Forum catalog, OnProgressChangedListener progressChangedListener) throws IOException {

        String pageBody = Client.INSTANCE.loadPageAndCheckLogin(gameCatalogUrl, progressChangedListener);

        Pattern pattern = Pattern.compile("<div class=\"post_body\">(<div align='center'>)?<!--coloro:coral--><span style=\"color:coral\"><!--/coloro--><b>(<div align='center'>)?\\d+. (.*?)<.*?/div>(.*?)</div>");
        Pattern subCategoryPattern = Pattern.compile("<ol type='1'><!--coloro:coral--><span style=\"color:coral\"><!--/coloro--><b>(.*?)</b><!--colorc--></span><!--/colorc--></li>");

        Matcher m = pattern.matcher(pageBody);
        pageBody = null;
        Forum category = null;
        Forum subcategory = null;
        int id = -1;
        while (m.find()) {

            category = new Forum(Integer.toString(id++), Html.fromHtml(m.group(3)).toString());
            category.setHtmlTitle(m.group(3));
            category.setTag(catalog.getTag());
            // category.addForum(new Forum(category.getId(), category.getTitle() + " @ темы"));
            catalog.addForum(category);

            Matcher m1 = subCategoryPattern.matcher(m.group(4));
            while (m1.find()) {
                subcategory = new Forum(Integer.toString(id++), Html.fromHtml(m1.group(1)).toString());
                subcategory.setHtmlTitle(m1.group(1));
                subcategory.setTag(category.getTag());
                category.addForum(subcategory);
            }

            if (category.getForums().size() > 1) {
                Forum f = new Forum(category.getId(), category.getTitle() + " @ темы");
                f.setTag(category.getTag());
                category.addForum(0, f);
            }
        }


    }


    private void loadCategoryThemes(Forum category, String title, OnProgressChangedListener progressChangedListener) throws IOException {
        category.getThemes().clear();

        String pageBody = loadPageAndCheckLogin(appCatalogUrl, progressChangedListener);


        //Pattern pattern = Pattern.compile("<li><b>(<!--coloro:royalblue--><span style=\"color:royalblue\"><!--/coloro-->(.*?)<!--colorc--></span><!--/colorc-->\\s*)?<a href=\"http://4pda.ru/forum/index.php\\?showtopic=(\\d+)\" target=\"_blank\">(.*?)</a></b> - (.*?)</li>");
        Pattern pattern = Pattern.compile("<a name=\"entry"+category.getId()+"\">([\\s\\S]*?)</div><!--Begin Msg Number");
        Matcher m = pattern.matcher(pageBody);
        pageBody = null;
        Pattern themesPattern = Pattern.compile("((?<!<div align='center'>)<!--coloro:coral--><span style=\"color:coral\"><!--/coloro--><b>(.*?)</b><!--colorc--></span><!--/colorc--></li>)?<li><b>(<!--coloro:royalblue--><span style=\"color:royalblue\"><!--/coloro-->(.*?)<!--colorc--></span><!--/colorc-->\\s*)?<a href=\"http://4pda.ru/forum/index.php\\?showtopic=(\\d+)\" target=\"_blank\">(.*?)</a></b> - (.*?)</li>");
        Forum subCategory = new Forum("", "");
        if (m.find()) {
            Matcher m1 = themesPattern.matcher(m.group(1));
            int i = 1;
            while (m1.find()) {
                if (!TextUtils.isEmpty(m1.group(2))) {
                    subCategory.setTitle(m1.group(2));
                    i = 1;
                }
                Topic topic = new Topic(m1.group(5), m1.group(6));
                topic.setDescription(m1.group(7));
                topic.setForumTitle(subCategory.getTitle());
                category.addTheme(topic);
            }
        }
    }

    private void loadGameCategoryThemes(Forum category, String title, OnProgressChangedListener progressChangedListener) throws IOException {
        category.getThemes().clear();

        String pageBody = loadPageAndCheckLogin(gameCatalogUrl, progressChangedListener);

        //Pattern pattern = Pattern.compile("<li><b>(<!--coloro:royalblue--><span style=\"color:royalblue\"><!--/coloro-->(.*?)<!--colorc--></span><!--/colorc-->\\s*)?<a href=\"http://4pda.ru/forum/index.php\\?showtopic=(\\d+)\" target=\"_blank\">(.*?)</a></b> - (.*?)</li>");
        Pattern pattern = Pattern.compile("<div class=\"post_body\">(<div align='center'>)?<!--coloro:coral--><span style=\"color:coral\"><!--/coloro--><b>(<div align='center'>)?\\d+. " + Pattern.quote(title) + ".*?</div>(.*?)</div>");
        Matcher m = pattern.matcher(pageBody);
        pageBody = null;
        Pattern themesPattern = Pattern.compile("(<ol type='1'><!--coloro:coral--><span style=\"color:coral\"><!--/coloro--><b>(.*?)</b><!--colorc--></span><!--/colorc--></li>)?<li>.*?<a href=\"http://4pda.ru/forum/index.php\\?showtopic=(\\d+).*?\" target=\"_blank\">(.*?)</a>(.*?)</li>");
        Forum subCategory = new Forum("", title);
        if (m.find()) {
            Matcher m1 = themesPattern.matcher(m.group(3));
            while (m1.find()) {
                if (!TextUtils.isEmpty(m1.group(2))) {
                    subCategory.setTitle(Html.fromHtml(m1.group(2)).toString());
                }
                Topic topic = new Topic(m1.group(3), Html.fromHtml(m1.group(4)).toString());
                topic.setDescription(Html.fromHtml(m1.group(5)).toString());
                topic.setForumTitle(subCategory.getTitle());
                category.addTheme(topic);
            }
        }
    }

    private void loadSubCategoryThemes(Forum category, Forum subCategory, OnProgressChangedListener progressChangedListener) throws IOException {
        subCategory.getThemes().clear();

        String pageBody = loadPageAndCheckLogin(appCatalogUrl, progressChangedListener);

        //Pattern pattern = Pattern.compile("<li><b>(<!--coloro:royalblue--><span style=\"color:royalblue\"><!--/coloro-->(.*?)<!--colorc--></span><!--/colorc-->\\s*)?<a href=\"http://4pda.ru/forum/index.php\\?showtopic=(\\d+)\" target=\"_blank\">(.*?)</a></b> - (.*?)</li>");
        Pattern pattern = Pattern.compile("<a name=\"entry"+category.getId()+"\">([\\s\\S]*?)</div><!--Begin Msg Number");
        Matcher m = pattern.matcher(pageBody);
        Pattern subCategoryPattern = Pattern.compile("(?<!<div align='center'>)<!--coloro:coral--><span style=\"color:coral\"><!--/coloro--><b>" + Pattern.quote(subCategory.getTitle()) + "</b><!--colorc--></span><!--/colorc--></li>(.*?)((<!--coloro:coral--><span style=\"color:coral\"><!--/coloro--><b>)|(#end#))");
        Pattern themesPattern = Pattern.compile("(<ol type='1'><!--coloro:coral--><span style=\"color:coral\"><!--/coloro--><b>(.*?)</b><!--colorc--></span><!--/colorc--></li>)?<li>(<b>)?(<!--coloro:royalblue--><span style=\"color:royalblue\"><!--/coloro-->(.*?)<!--colorc--></span><!--/colorc-->\\s*)?<a href=\"http://4pda.ru/forum/index.php\\?showtopic=(\\d+)\" target=\"_blank\">(.*?)</a>.*?(</b>)? - (.*?)<");

        if (m.find()) {
            Matcher m1 = subCategoryPattern.matcher(m.group(1) + "#end#");
            if (m1.find()) {
                Matcher m2 = themesPattern.matcher(m1.group(1));
                while (m2.find()) {

                    Topic topic = new Topic(m2.group(6), m2.group(7));
                    topic.setDescription(m2.group(9));
                    topic.setForumTitle(subCategory.getTitle());
                    subCategory.addTheme(topic);
                }
            }
        }
    }



    private void loadGameSubCategoryThemes(Forum subCategory, String categoryTitle, OnProgressChangedListener progressChangedListener) throws IOException {
        subCategory.getThemes().clear();


        String pageBody = loadPageAndCheckLogin(gameCatalogUrl, progressChangedListener);

        //Pattern pattern = Pattern.compile("<li><b>(<!--coloro:royalblue--><span style=\"color:royalblue\"><!--/coloro-->(.*?)<!--colorc--></span><!--/colorc-->\\s*)?<a href=\"http://4pda.ru/forum/index.php\\?showtopic=(\\d+)\" target=\"_blank\">(.*?)</a></b> - (.*?)</li>");
        Pattern pattern = Pattern.compile("<div class=\"post_body\">(<div align='center'>)?<!--coloro:coral--><span style=\"color:coral\"><!--/coloro--><b>(<div align='center'>)?\\d+. " + Pattern.quote(categoryTitle) + ".*?</div>(.*?)</div>");
        Matcher m = pattern.matcher(pageBody);
        Pattern subCategoryPattern = Pattern.compile("(?<!<div align='center'>)<!--coloro:coral--><span style=\"color:coral\"><!--/coloro--><b>" + Pattern.quote(subCategory.getHtmlTitle()) + "<(.*?)((<ol type='1'>)|(</div>))");
        Pattern themesPattern = Pattern.compile("(<ol type='1'><!--coloro:coral--><span style=\"color:coral\"><!--/coloro--><b>(.*?)</b><!--colorc--></span><!--/colorc--></li>)?<li>.*?<a href=\"http://4pda.ru/forum/index.php\\?showtopic=(\\d+).*?\" target=\"_blank\">(.*?)</a>(.*?)</li>");

        if (m.find()) {
            Matcher m1 = subCategoryPattern.matcher(m.group(3) + "</div>");
            if (m1.find()) {
                Matcher m2 = themesPattern.matcher(m1.group(1));
                while (m2.find()) {

                    Topic topic = new Topic(m2.group(3), m2.group(4));
                    topic.setDescription(Html.fromHtml(m2.group(5)).toString());
                    topic.setForumTitle(subCategory.getTitle());
                    subCategory.addTheme(topic);
                }
            }
        }
    }

    private String loadPageAndCheckLogin(String url, OnProgressChangedListener progressChangedListener) throws IOException {
        return Client.INSTANCE.loadPageAndCheckLogin(url, progressChangedListener);
    }
}

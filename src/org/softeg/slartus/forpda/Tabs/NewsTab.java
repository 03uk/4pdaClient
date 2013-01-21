package org.softeg.slartus.forpda.Tabs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.View;
import android.widget.AdapterView;
import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.NewsActivity;
import org.softeg.slartus.forpda.classes.Topic;
import org.softeg.slartus.forpda.classes.common.ExtUrl;
import org.softeg.slartus.forpdaapi.NotReportException;
import org.softeg.slartus.forpdaapi.OnProgressChangedListener;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 06.12.11
 * Time: 16:08
 * To change this template use File | Settings | File Templates.
 */
public class NewsTab extends ThemesTab {


    public NewsTab(Context context, String tabTag) {
        super(context, tabTag);

    }

    public String getTemplate() {
        return TEMPLATE;
    }

    public static final String TEMPLATE = Tabs.TAB_NEWS;
    public static final String TITLE = "Новости";

    @Override
    public void refresh() {
        super.refresh();
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, Handler handler) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        if (info.id == -1) return;
        final Topic topic = m_ThemeAdapter.getItem((int) info.id);
        if (TextUtils.isEmpty(topic.getId())) return;

        ExtUrl.addUrlMenu(getContext(), menu, topic.getId());

    }

    @Override
    public void getThemes(OnProgressChangedListener progressChangedListener) throws Exception {
        Client.INSTANCE.doOnOnProgressChanged(progressChangedListener, "Получение данных...");
        Client.INSTANCE.loadTestPage();
        if (m_Themes.size() == 0)
            getRssItems(progressChangedListener);
        else
            getHttpItems(progressChangedListener);
    }

    private String normalizeRss(String body) {
        return body.replaceAll("&(?!.{1,4};)", "&amp;");
    }

    private void getRssItems(OnProgressChangedListener progressChangedListener) throws Exception {
        try {
            m_Themes.setThemesCountInt(200);


            String body = Client.INSTANCE.performGet("http://4pda.ru/feed/");
            if (TextUtils.isEmpty(body))
                throw new NotReportException("Сервер вернул пустую страницу!");
            Client.INSTANCE.doOnOnProgressChanged(progressChangedListener, "Обработка данных...");

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            DocumentBuilder db = dbf.newDocumentBuilder();

           body = normalizeRss(body);

            Document document = db.parse(new InputSource(new StringReader(body)));

            Element element = document.getDocumentElement();

            NodeList nodeList = element.getElementsByTagName("item");

            if (nodeList.getLength() > 0) {

                for (int i = 0; i < nodeList.getLength(); i++) {

                    Element entry = (Element) nodeList.item(i);

                    Element _titleE = (Element) entry.getElementsByTagName("title").item(0);

                    Element _descriptionE = (Element) entry.getElementsByTagName("description").item(0);

                    Element _pubDateE = (Element) entry.getElementsByTagName("pubDate").item(0);

                    Element _linkE = (Element) entry.getElementsByTagName("link").item(0);


                    StringBuilder _title = new StringBuilder();
                    NodeList nodes=_titleE.getChildNodes();
                    int nodesLength=nodes.getLength();
                    for (int c = 0; c < nodesLength; c++) {
                        _title.append(nodes.item(c).getNodeValue());
                    }


                    //String _description = _descriptionE.getFirstChild().getNodeValue();
                    StringBuilder _description = new StringBuilder();
                     nodes=_descriptionE.getChildNodes();
                     nodesLength=nodes.getLength();
                    for (int c = 0; c < nodesLength; c++) {
                        _description.append(nodes.item(c).getNodeValue().replace("\n"," "));
                    }

                    Date _pubDate = new Date(_pubDateE.getFirstChild().getNodeValue());

                    String _link = _linkE.getFirstChild().getNodeValue();

                    String author = entry.getElementsByTagName("dc:creator").item(0).getChildNodes().item(0).getNodeValue();

                    NewsTheme topic = new NewsTheme(_link, _title.toString());
                    topic.setLastMessageDate(_pubDate);
                    topic.setLastMessageAuthor(author);
                    topic.setDescription(_description.toString().replaceAll("(<img.*?/>)",""));
                    topic.Page = 1;
                    m_Themes.add(topic);

                }

            }
        } catch (Exception ex) {
            throw new NotReportException("Ошибка разбора rss: " + ex.getMessage(), ex);
        }

    }

    private void getHttpItems(OnProgressChangedListener progressChangedListener) throws Exception {

        loadNextNewsPage();
    }

    private void loadNextNewsPage() throws IOException, ParseException {

        NewsTheme newsTheme = (NewsTheme) m_Themes.get(m_Themes.size() - 1);

        String url = newsTheme.getId();
        Matcher m = Pattern.compile("4pda.ru/(\\d+)/(\\d+)/(\\d+)/(\\d+)").matcher(url);
        m.find();

        int year = Integer.parseInt(m.group(1));
        int nextPage = newsTheme.Page + 1;
        loadPage(year, nextPage,0);
    }

    private void loadPage(int year, int nextPage, int iteration) throws IOException, ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy");
        String dailyNewsUrl = "http://4pda.ru/" + year + "/page/" + nextPage;

        String dailyNewsPage = Client.INSTANCE.performGet(dailyNewsUrl);


        Matcher m = Pattern.compile("<a href=\"(/\\d+/\\d+/\\d+/(\\d+))/\" rel=\"bookmark\" title=\"(.*?)\" alt=\"\">.*?</a></h2>.*?<p style=\"text-align: justify;\">(.*?)</p>[\\s\\S]*?<strong>(.*?)</strong>&nbsp;\\|\\s*(\\d+\\.\\d+\\.\\d+)\\s*\\| ")
                .matcher(dailyNewsPage);
        Boolean someUnloaded = false;// одна из новостей незагружена - значит и остальные
        int before = m_Themes.size();
        while (m.find()) {
            String id = "http://4pda.ru" + m.group(1);

            if (!someUnloaded && m_Themes.findByTitle(id) != null) continue;
            someUnloaded = true;

            NewsTheme topic = new NewsTheme(id, m.group(3));

            Date _pubDate = dateFormat.parse(m.group(6));
            topic.setLastMessageDate(_pubDate);
            topic.setLastMessageAuthor(m.group(5));
            topic.setDescription(m.group(4));
            topic.Page = nextPage;
            m_Themes.add(topic);
        }
        if (before == m_Themes.size()) {
            if(iteration>0)return ;
            if(dailyNewsPage.contains("По указанным параметрам не найдено ни одного поста"))
                loadPage(year-1,1,iteration+1);
            else
                loadPage(year,nextPage+1,iteration+1);
        }
    }

    protected void listItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        l = ListViewMethodsBridge.getItemId(getContext(), i, l);
        if (l < 0 || m_ThemeAdapter.getCount() <= l) return;
        if (m_ThemeAdapter == null) return;
        Topic topic = m_ThemeAdapter.getItem((int) l);
        if (TextUtils.isEmpty(topic.getId())) return;
        topic.setIsNew(false);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (prefs.getString("tabs." + m_TabId + ".Action", "getlastpost").equals("browser"))
            showNewsBrowser(topic.getId());
        else
            showNewsActivity(topic.getId());

        m_ThemeAdapter.notifyDataSetChanged();
    }

    private void showNewsActivity(String url) {
        NewsActivity.shownews(getContext(), url);
    }

    private void showNewsBrowser(String url) {
        Intent marketIntent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url));
        getContext().startActivity(Intent.createChooser(marketIntent, "Выберите"));
    }

    private class NewsTheme extends Topic {

        public NewsTheme(String id, String title) {
            super(id, title);
        }

        public int Page;
    }
}




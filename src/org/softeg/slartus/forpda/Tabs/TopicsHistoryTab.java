package org.softeg.slartus.forpda.Tabs;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.View;
import android.widget.AdapterView;
import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.classes.ThemeOpenParams;
import org.softeg.slartus.forpda.classes.Topic;
import org.softeg.slartus.forpda.classes.common.ExtUrl;
import org.softeg.slartus.forpda.db.TopicsHistoryTable;
import org.softeg.slartus.forpdaapi.OnProgressChangedListener;

/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 22.11.12
 * Time: 8:53
 * To change this template use File | Settings | File Templates.
 */
public class TopicsHistoryTab  extends ThemesTab {


    public TopicsHistoryTab(Context context, String tabTag) {
        super(context, tabTag);

    }

    public String getTemplate() {
        return TEMPLATE;
    }

    public static final String TEMPLATE = Tabs.TAB_TOPICS_HISTORY;
    public static final String TITLE = "Посещенные темы";

    @Override
    public void refresh() {
        super.refresh();
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    protected Boolean isShowForumTitle() {
        return true;
    }

    @Override
    protected String getDefaultOpenThemeParams() {
        return ThemeOpenParams.getUrlParams(ThemeOpenParams.NEW_POST,
                super.getDefaultOpenThemeParams());
    }

    @Override
    public void getThemes(OnProgressChangedListener progressChangedListener) throws Exception {
        Client.INSTANCE.loadTestPage();
        TopicsHistoryTable.getTopicsHistory(m_Themes);
    }

}

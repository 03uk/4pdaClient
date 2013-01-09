package org.softeg.slartus.forpda.Tabs;

import android.content.Context;
import android.os.Bundle;
import android.widget.LinearLayout;

/**
 * Created by IntelliJ IDEA.
 * User: slartus
 * Date: 21.10.12
 * Time: 13:46
 * To change this template use File | Settings | File Templates.
 */
public abstract class BaseTab extends LinearLayout implements ITab{
    public BaseTab(Context context) {
        super(context);
    }

    private OnTabTitleChangedListener m_OnTabTitleChangedListener;

    public interface OnTabTitleChangedListener {
        void onTabTitleChanged(String title);
    }

    public void doOnTabTitleChangedListener(String title) {
        if (m_OnTabTitleChangedListener != null) {
            m_OnTabTitleChangedListener.onTabTitleChanged(title);
        }
    }

    public void setOnTabTitleChangedListener(OnTabTitleChangedListener p) {
        m_OnTabTitleChangedListener = p;
    }

    public abstract void refresh(Bundle extras);
}

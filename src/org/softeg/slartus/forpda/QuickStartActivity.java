package org.softeg.slartus.forpda;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.ContextMenu;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragment;
import org.softeg.slartus.forpda.Tabs.BaseTab;
import org.softeg.slartus.forpda.Tabs.Tabs;
import org.softeg.slartus.forpdaapi.NotReportException;

/**
 * User: slinkin
 * Date: 14.11.11
 * Time: 11:48
 */
public class QuickStartActivity extends BaseFragmentActivity{
    private BaseTab themesTab;
    private Handler mHandler = new Handler();
    private  MenuFragment mFragment1;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        createActionMenu();


        setContentView(R.layout.empty_activity);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        String template = extras.getString("template");
        themesTab = Tabs.create(this, template, "QuickTab");
        setContentView(themesTab);
        try {
            setTitle(Tabs.getDefaultTemplateName(template));
        } catch (NotReportException e) {

        }
        themesTab.refresh(extras);
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


    private Boolean m_ExitWarned = false;

    @Override
    public void onBackPressed() {

        if (!themesTab.onParentBackPressed()) {
            if (!m_ExitWarned) {
                Toast.makeText(getApplicationContext(), "Нажмите кнопку НАЗАД снова, чтобы закрыть", Toast.LENGTH_SHORT).show();
                m_ExitWarned = true;
            } else {
                finish();
            }

        } else {
            m_ExitWarned = false;
        }
    }



    public void refresh() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Boolean refreshed() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ListView getListView() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, Handler handler) {

    }

    @Override
    public void onResume() {
        super.onResume();
        m_ExitWarned = false;
    }

    public static final class MenuFragment extends SherlockFragment {
        public MenuFragment() {

        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        private QuickStartActivity getInterface() {
            return (QuickStartActivity)getActivity();
        }

        @Override
        public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
            com.actionbarsherlock.view.MenuItem item = menu.add("Обновить").setIcon(R.drawable.ic_menu_refresh);
            item.setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                    getInterface().themesTab.refresh();
                    return true;
                }
            });
            item.setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_ALWAYS);

            item = menu.add("Закрыть").setIcon(android.R.drawable.ic_menu_close_clear_cancel);
            item.setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                    getActivity().finish();

                    return true;
                }
            });
        }
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, android.view.View v, android.view.ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        themesTab.onCreateContextMenu(menu, v, menuInfo, mHandler);

    }
}

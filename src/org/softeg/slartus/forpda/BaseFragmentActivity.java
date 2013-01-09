package org.softeg.slartus.forpda;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.WindowManager;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import org.softeg.slartus.forpda.classes.common.ExtPreferences;

/**
 * User: slinkin
 * Date: 14.03.12
 * Time: 12:51
 */
public class BaseFragmentActivity extends SherlockFragmentActivity {
    protected void afterCreate(){

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        afterCreate();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        loadPreferences(prefs);

        setTheme(MyApp.INSTANCE.getThemeStyleResID());

    }

    protected void loadPreferences(SharedPreferences prefs) {
        setRequestedOrientation(ExtPreferences.parseInt(prefs, "theme.ScreenOrientation", -1));
    }


}

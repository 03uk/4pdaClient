package org.softeg.slartus.forpda.prefs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.MyApp;
import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpda.Tabs.Tabs;
import org.softeg.slartus.forpda.classes.ForumUser;
import org.softeg.slartus.forpda.classes.common.FileUtils;
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpda.download.DownloadsService;
import org.softeg.slartus.forpda.topicview.ThemeActivity;
import org.softeg.slartus.forpdaapi.NotReportException;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 * User: slinkin
 * Date: 03.10.11
 * Time: 10:47
 */
public class PreferencesActivity extends SherlockPreferenceActivity {
    private Handler mHandler = new Handler();

    private Context getContext() {
        return this;
    }

//    private static String getFilesDir(Context context){
//        return  context.getApplicationContext().getFilesDir().toString();
//    }

    private static String getSystemCookiesPath() {
        String defaultFile = MyApp.INSTANCE.getFilesDir() + "/4pda_cookies";
        if (MyApp.INSTANCE.getFilesDir() == null)
            defaultFile = Environment.getExternalStorageDirectory() + "/data/4pdaClient/4pda_cookies";
        return defaultFile;
    }

    private static String getAppCookiesPath() throws IOException {
        String defaultFile = MyApp.INSTANCE.getAppExternalFolderPath() + "4pda_cookies";

        return defaultFile;
    }

    public static String getCookieFilePath(Context context) throws IOException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String res = prefs.getString("cookies.path", "");

        if (TextUtils.isEmpty(res))
            res = getAppCookiesPath();

        return res.replace("/", File.separator);
    }

    private void setCookiesPathWithToast(String value) {
        try {

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("cookies.path", value);
            if (editor.commit()) {
                ((EditTextPreference) findPreference("cookies.path")).setText(value);
//                ((EditTextPreference)findPreference("cookies.path")).getEditor().putString("cookies.path",value) .commit();
                Toast.makeText(getContext(), "Путь к cookies изменен", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception ex) {
            Log.e(getContext(), ex);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        addPreferencesFromResource(R.xml.preferences);
        Preference aboutAppVersion = findPreference("About.AppVersion");
        aboutAppVersion.setTitle(getProgramFullName(this));
        aboutAppVersion.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                showAbout();
                return true;
            }
        });
        findPreference("cookies.path.SetSystemPath").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                setCookiesPathWithToast(getSystemCookiesPath());
                return true;
            }
        });
        findPreference("cookies.path.SetAppPath").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                try {
                    setCookiesPathWithToast(getAppCookiesPath());
                } catch (IOException e) {
                   Log.e(getContext(),e);
                }
                return true;
            }
        });
        findPreference("cookies.delete").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {

                new AlertDialog.Builder(getContext())
                        .setTitle("Подтвердите действие")
                        .setMessage("Вы действительно хотите удалить файл?")
                        .setCancelable(true)
                        .setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                try {
                                    dialogInterface.dismiss();
                                    File f = new File(getCookieFilePath(getContext()));
                                    if (!f.exists()) {
                                        Toast.makeText(getContext(), "Файл cookies не найден: " + getCookieFilePath(getContext()), Toast.LENGTH_LONG).show();
                                    }
                                    if (f.delete())
                                        Toast.makeText(getContext(), "Файл cookies удален: " + getCookieFilePath(getContext()), Toast.LENGTH_LONG).show();
                                    else
                                        Toast.makeText(getContext(), "Не удалось удалить файл cookies: " + getCookieFilePath(getContext()), Toast.LENGTH_LONG).show();
                                } catch (Exception ex) {
                                    Log.e(getContext(), ex);
                                }
                            }
                        })
                        .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).create().show();


                return true;
            }
        });
        fillStyles();
//        EditTextPreference editTextPreference = (EditTextPreference) findPreference("Additional.CookiesFile");
//        if (TextUtils.isEmpty(editTextPreference.getEditText().getText()))
//            editTextPreference.setText(getCookieFilePath(this));
//        editTextPreference.setSummary(getCookieFilePath(this));
//        editTextPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//            public boolean onPreferenceChange(Preference preference, Object o) {
//                try {
//                    String filePath = ((EditTextPreference) preference).getText().replace("/", File.separator);
//                    File file = new File(filePath);
//
//
//                    if (!Functions.mkDirs(file.getAbsolutePath()))
//                        throw new NotReportException("Не могу создать указанную директорию!");
//                    if (!file.createNewFile() && !file.exists())
//                        throw new NotReportException("Не могу создать указанный файл!");
//
//                } catch (Exception ex) {
//                    String text = ex.getMessage();
//                    if (TextUtils.isEmpty(text))
//                        text = ex.toString();
//                    Log.e(PreferencesActivity.this, new NotReportException(text));
//                    return false;
//                }
//                Toast.makeText(PreferencesActivity.this, "Файл успешно создан!", Toast.LENGTH_SHORT).show();
//                return true;
//
//            }
//        });

        findPreference("About.History").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                StringBuilder sb = new StringBuilder();
                try {

                    BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open("history.txt"), "UTF-8"));
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }

                } catch (IOException e) {
                    Log.e(PreferencesActivity.this, e);
                }
                AlertDialog dialog = new AlertDialog.Builder(PreferencesActivity.this)
                        .setIcon(R.drawable.icon)
                        .setTitle("История изменений")
                        .setMessage(sb)
                        .setPositiveButton(android.R.string.ok, null)
                        .create();
                dialog.show();
                TextView textView = (TextView) dialog.findViewById(android.R.id.message);
                textView.setTextSize(12);
                return true;
            }
        });

        findPreference("About.ShareIt").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent sendMailIntent = new Intent(Intent.ACTION_SEND);
                sendMailIntent.putExtra(Intent.EXTRA_SUBJECT, "Рекомендую установить программу 4pda");
                sendMailIntent.putExtra(Intent.EXTRA_TEXT, "Привет, я использую 4pda, это первый android-клиент для лучшего сайта о мобильных устройствах 4PDA." +
                        "Ты можешь найти его через поиск в Android Market по слову \"4pda\" или жми на ссылку http://goo.gl/jJp6m");
                sendMailIntent.setType("text/plain");

                startActivity(Intent.createChooser(sendMailIntent, "Отправить через..."));
                return true;
            }
        });

        findPreference("About.SendFeedback").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent marketIntent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://market.android.com/details?id=" + getPackageName()));
                PreferencesActivity.this.startActivity(marketIntent);
                return true;
            }
        });

        findPreference("About.AddRep").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                if (!Client.INSTANCE.getLogined()) {
                    Toast.makeText(PreferencesActivity.this, "Необходимо залогиниться!", Toast.LENGTH_SHORT).show();
                    return true;
                }
                ForumUser.startChangeRep(PreferencesActivity.this, mHandler, "236113", "slartus", "0", "add", "Поднять репутацию");
                return true;
            }
        });

        findPreference("About.ShowTheme").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(PreferencesActivity.this, ThemeActivity.class);
                intent.putExtra("ThemeUrl", "271502");

                PreferencesActivity.this.startActivity(intent);
                return true;
            }
        });

        findPreference("appstyle").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object o) {
                Toast.makeText(PreferencesActivity.this, "Необходимо перезапустить программу для применения темы!", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        Preference downloadsPathPreference = findPreference("downloads.path");
        downloadsPathPreference.setSummary(DownloadsService.getDownloadDir(getApplicationContext()));
        ((EditTextPreference) downloadsPathPreference)
                .setText(DownloadsService.getDownloadDir(getApplicationContext()));
        downloadsPathPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object o) {
                try {
                    String dirPath = o.toString();
                    if (!dirPath.endsWith(File.separator))
                        dirPath += File.separator;
                    File dir = new File(dirPath);
                    File file = new File(FileUtils.getUniqueFilePath(dirPath, "4pda.tmp"));

                    if (!dir.exists() && !dir.mkdirs())
                        throw new NotReportException("Не удалось создать папку по указанному пути");

                    if (!file.createNewFile())
                        throw new NotReportException("Не удалось создать файл по указанному пути");
                    file.delete();
                    return true;
                } catch (Throwable ex) {
                    Log.e(PreferencesActivity.this, new NotReportException(ex.toString()));
                }
                return false;
            }
        });

        DonateActivity.setDonateClickListeners(this);

        try {
            setTabsThemeActionText();

            Tabs.configTabsData(this);
        } catch (Throwable ex) {
            Log.e(this, ex);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            setTabsThemeActionText();

            Tabs.configTabsData(this);
        } catch (Throwable ex) {
            Log.e(this, ex);
        }
    }

    private void setTabsThemeActionText() {
        ArrayList<String> values = new ArrayList();
        Collections.addAll(values, getResources().getStringArray(R.array.ThemeActionsValues));

        ArrayList<String> captions = new ArrayList();
        Collections.addAll(captions, getResources().getStringArray(R.array.ThemeActionsArray));

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        for (int i = 1; i <= getResources().getStringArray(R.array.tabsArray).length; i++) {
            setTabThemeActionText(prefs, "Tab" + i, captions, values);
        }
    }

    private void setTabThemeActionText(SharedPreferences prefs, String tabId
            , ArrayList<String> captions, ArrayList<String> values) {
        String action = prefs.getString("tabs." + tabId + ".Action", "getlastpost");
        if (TextUtils.isEmpty(action))
            action = "getlastpost";

        findPreference("tabs." + tabId + ".Action").setSummary(captions.get(values.indexOf(action)));

    }


    private void fillStyles() {
        try {
            File file = new File(MyApp.INSTANCE.getAppExternalFolderPath() + "styles/");
            if (!file.exists()) return;

            ListPreference stylesList = (ListPreference) findPreference("appstyle");

            ArrayList<CharSequence> newStyleNames = new ArrayList<CharSequence>();
            ArrayList<CharSequence> newstyleValues = new ArrayList<CharSequence>();

            CharSequence[] styleNames = stylesList.getEntries();
            for (CharSequence s : styleNames) {
                newStyleNames.add(s);
            }
            CharSequence[] styleValues = stylesList.getEntryValues();
            for (CharSequence s : styleValues) {
                newstyleValues.add(s);
            }

            file = new File(MyApp.INSTANCE.getAppExternalFolderPath() + "styles/white/");
            if (file.exists()) {
                File[] cssFiles = file.listFiles(new FilenameFilter() {
                    public boolean accept(File file, String s) {
                        return s.endsWith(".css");
                    }
                });
                for (File cssFile : cssFiles) {
                    newStyleNames.add(cssFile.getName());
                    newstyleValues.add(cssFile.getPath());
                }
            }

            file = new File(MyApp.INSTANCE.getAppExternalFolderPath() + "styles/black/");
            if (file.exists()) {
                File[] cssFiles = file.listFiles(new FilenameFilter() {
                    public boolean accept(File file, String s) {
                        return s.endsWith(".css");
                    }
                });
                for (File cssFile : cssFiles) {
                    newStyleNames.add(cssFile.getName());
                    newstyleValues.add(cssFile.getPath());
                }
            }


            stylesList.setEntries(newStyleNames.toArray(new CharSequence[newStyleNames.size()]));
            stylesList.setEntryValues(newstyleValues.toArray(new CharSequence[newstyleValues.size()]));
        } catch (Exception ex) {
            Log.e(this, ex);
        }


    }

    private void showAbout() {

        String text = "Неофициальный клиент для сайта <a href=\"http://www.4pda.ru\">4pda.ru</a><br/><br/>\n" +
                "<b>Автор: </b> Артём Слинкин aka slartus<br/>\n" +
                "<b>E-mail:</b> <a href=\"mailto:slartus+4pda@gmail.com\">slartus+4pda@gmail.com</a><br/><br/>\n" +
                "<b>Благодарности: </b> <br/>\n" +
                "* <b><a href=\"http://4pda.ru/forum/index.php?showuser=474658\">zlodey.82</a></b> иконка программы<br/>\n" +
                "* <b><a href=\"http://4pda.ru/forum/index.php?showuser=1429916\">sbarrofff</a></b> иконка программы<br/>\n" +
                "* <b><a href=\"http://4pda.ru/forum/index.php?showuser=680839\">SPIDER3220</a></b> (иконки, баннеры)<br/>\n" +
                "* <b><a href=\"http://4pda.ru/forum/index.php?showuser=1392892\">ssmax2015</a></b> (иконки, баннеры)<br/>\n" +
                "* <b><a href=\"http://4pda.ru/forum/index.php?showuser=2523\">e202</a></b> (иконки сообщения для черной темы)<br/>\n" +
                "* <b><a href=\"http://4pda.ru/forum/index.php?showuser=2040700\">Remie-l</a></b> (новые стили для топиков)<br/>\n" +
                "* <b><a href=\"http://www.4pda.ru\">пользователям 4pda</a></b> (тестирование, идеи, поддержка)\n" +
                "<br/>";

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setIcon(R.drawable.icon)
                .setTitle(getProgramFullName(this))
                .setMessage(Html.fromHtml(text))
                .setPositiveButton(android.R.string.ok, null)
                .create();
        dialog.show();
        TextView textView = (TextView) dialog.findViewById(android.R.id.message);
        textView.setTextSize(12);

        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public static String getProgramFullName(Context context) {
        String programName = context.getString(R.string.app_name);
        try {
            String packageName = context.getPackageName();
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(
                    packageName, PackageManager.GET_META_DATA);

            programName += " v" + pInfo.versionName;

        } catch (PackageManager.NameNotFoundException e1) {
            Log.e(context, e1);
        }
        return programName;
    }


}

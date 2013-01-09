package org.softeg.slartus.forpda;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import org.softeg.slartus.forpda.Mail.EditMailActivity;
import org.softeg.slartus.forpda.Mail.MailActivity;
import org.softeg.slartus.forpda.Tabs.Tabs;
import org.softeg.slartus.forpda.classes.ForumUser;
import org.softeg.slartus.forpda.common.Log;

import org.softeg.slartus.forpda.download.DownloadsService;
import org.softeg.slartus.forpda.profile.ProfileActivity;
import org.softeg.slartus.forpda.qms.QmsChatActivity;
import org.softeg.slartus.forpda.topicview.ThemeActivity;
import org.softeg.slartus.forpdaapi.NotReportException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: slartus
 * Date: 17.01.12
 * Time: 13:26
 * To change this template use File | Settings | File Templates.
 */
public class IntentActivity extends Activity {
    private Uri m_Data = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public Boolean isAppUrl(String url) {
        return isTheme(url) || isFileOrImage(url) || isNews(url) || isDevDb(url);
    }

    public static boolean isFileOrImage(String url) {
        Pattern filePattern = Pattern.compile("http://4pda.ru/forum/dl/post/\\d+/.*");
        Pattern stFilePattern = Pattern.compile("http://st.4pda.ru/wp-content/uploads/.*");
        return filePattern.matcher(url).find() || stFilePattern.matcher(url).find();
    }

    public static Boolean isNews(String url) {
        final Pattern pattern = Pattern.compile("http://4pda.ru/\\d{4}/\\d{2}/\\d{2}/\\d+");
        final Pattern pattern1 = Pattern.compile("http://4pda.ru/(\\w+)/(older|newer)/\\d+");

        return pattern.matcher(url).find() || pattern1.matcher(url).find();
    }

    public static Boolean tryShowNews(Activity context,String url, Boolean finish) {
        if(isNews(url))
        {
            NewsActivity.shownews(context, url);
            if (finish)
                context.finish();
            return true;
        }
        return false;
    }

    public static boolean isTheme(String url) {
        Pattern p = Pattern.compile("http://" + Client.SITE + "/forum/index.php\\?((.*)?showtopic=.*)");
        Pattern p1 = Pattern.compile("http://" + Client.SITE + "/forum/index.php\\?((.*)?act=findpost&pid=\\d+(.*)?)");
        Pattern p2 = Pattern.compile("http://" + Client.SITE + "/index.php\\?((.*)?act=findpost&pid=\\d+(.*)?)");

        return p.matcher(url).find() || p1.matcher(url).find()|| p2.matcher(url).find();
    }

    public static boolean isMail(String url) {


        Pattern p = Pattern.compile("http://4pda.ru/forum/index.php\\?act=Msg&CODE=03&VID=in&MSID=(\\d+)");

        Matcher m = p.matcher(url);


        return m.find();
    }

    public static boolean isDevDb(String url) {
        Pattern p = Pattern.compile("http://devdb.ru");

        Matcher m = p.matcher(url);

        return m.find();
    }

    public static boolean tryShowReputation(Activity context, String url, Boolean finish) {
        Matcher m = Pattern.compile("http://4pda.ru/forum/index.php\\?act=rep&type=history&mid=(\\d+)").matcher(url);
        if (m.find()) {
            ReputationActivity.showRep(context, m.group(1));
            if (finish)
                context.finish();
            return true;
        }
        return false;
    }

    public static boolean tryPlusReputation(Activity context,  Handler handler, String url, Boolean finish) {
        Matcher m = Pattern.compile("http://4pda.ru/forum/index.php\\?act=rep&type=win_add&mid=(\\d+)&p=(\\d+)").matcher(url);
        if (m.find()) {
            ForumUser.startChangeRep(context, handler, m.group(1), m.group(1), m.group(2), "add", "Поднять репутацию");
            if (finish)
                context.finish();
            return true;
        }
        return false;
    }

    public static boolean tryMinusReputation(Activity context,  Handler handler,String url, Boolean finish) {
        Matcher m = Pattern.compile("http://4pda.ru/forum/index.php\\?act=rep&type=win_minus&mid=(\\d+)&p=(\\d+)").matcher(url);
        if (m.find()) {
            ForumUser.startChangeRep(context, handler, m.group(1), m.group(1), m.group(2), "minus", "Опустить репутацию");
            if (finish)
                context.finish();
            return true;
        }
        return false;
    }

    public static boolean tryShowClaim(Activity context, Handler handler, String url, Boolean finish) {
        Matcher m = Pattern.compile("http://4pda.ru/forum/index.php\\?act=report&t=(\\d+)&p=(\\d+)").matcher(url);
        if (m.find()) {
            org.softeg.slartus.forpda.classes.Post.claim(context, handler, m.group(1), m.group(2));

            if (finish)
                context.finish();
            return true;
        }
        return false;
    }

    public static boolean tryProfile(Activity context, String url, Boolean finish) {
        Matcher m = Pattern.compile("http://4pda.ru/forum/index.php\\?.*?act=profile.*?id=(\\d+)").matcher(url);
        if (m.find()) {
            ProfileActivity.startActivity(context, m.group(1));

            if (finish)
                context.finish();
            return true;
        }

        m = Pattern.compile("http://4pda.ru/forum/index.php\\?.*?showuser=(\\d+)").matcher(url);
        if (m.find()) {
            ProfileActivity.startActivity(context, m.group(1));

            if (finish)
                context.finish();
            return true;
        }

        return false;
    }

    public static boolean tryShowForum(Activity context, String url, Boolean finish) {
        Matcher m = Pattern.compile("http://4pda.ru/forum/index.php\\?showforum=(\\d+)$").matcher(url);
        if (m.find()) {
            Intent intent = new Intent(context, QuickStartActivity.class);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("QuickTab.startforum", Integer.parseInt(m.group(1)));
            editor.commit();


            intent.putExtra("template", Tabs.TAB_FORUMS);

            context.startActivity(intent);
            if (finish)
                context.finish();
            return true;
        }
        return false;
    }

    public static Boolean tryShowUrl(Activity context, Handler handler, String url, Boolean showInDefaultBrowser,
                                     final Boolean finishActivity) {
        return tryShowUrl(context, handler, url, showInDefaultBrowser, finishActivity,null);
    }

    public static Boolean tryShowUrl(Activity context, Handler handler, String url, Boolean showInDefaultBrowser,
                                     final Boolean finishActivity, String authKey) {
        if (isTheme(url)) {
            Intent themeIntent = new Intent(context, ThemeActivity.class);
            themeIntent.setData(Uri.parse(url));
            context.startActivity(themeIntent);
            if (finishActivity)
                context.finish();
            return true;
        }

        if (isMail(url)) {
            Intent themeIntent = new Intent(context, MailActivity.class);
            themeIntent.setData(Uri.parse(url));
            context.startActivity(themeIntent);
            if (finishActivity)
                context.finish();
            return true;
        }

        if (tryShowNews(context, url, finishActivity)) {
            return true;
        }

        if (tryShowFile(context, handler, url, finishActivity)) {
            return true;
        }

        if (tryProfile(context, url, finishActivity)) {
            return true;
        }

        if (tryShowForum(context, url, finishActivity)) {
            return true;
        }

        if (tryShowReputation(context, url, finishActivity))
            return true;

        if (tryPlusReputation(context, handler, url, finishActivity))
            return true;

        if (tryMinusReputation(context, handler, url, finishActivity))
            return true;

        if (tryShowClaim(context, handler, url, finishActivity))
            return true;

        if (tryShowQms(context, url, finishActivity))
            return true;

        if (tryShowPm(context, url, finishActivity))
            return true;

        if (tryShowEditPost(context, url,authKey, finishActivity))
            return true;

        if (showInDefaultBrowser)
            showInDefaultBrowser(context, url);


        if (finishActivity)
            context.finish();
        return false;
    }



    public static boolean tryShowEditPost(Activity context, String url, String authKey, Boolean finish) {
        Matcher m = Pattern.compile("http://4pda.ru/forum/index.php\\?act=post&do=edit_post&f=(\\d+)&t=(\\d+)&p=(\\d+)").matcher(url);
        if (m.find()) {
            EditPostPlusActivity.editPost(context, m.group(1), m.group(2), m.group(3), authKey);

            if (finish)
                context.finish();
            return true;
        }
        return false;
    }

    public static boolean tryShowQuote(Activity context, String url, Boolean finish) {
        Matcher m = Pattern.compile("http://4pda.ru/forum/index.php?act=Post&CODE=02&f=285&t=271502&qpid=16587169").matcher(url);
        if (m.find()) {
            QmsChatActivity.openChat(context, m.group(1), "");

            if (finish)
                context.finish();
            return true;
        }
        return false;
    }

    public static boolean tryShowQms(Activity context, String url, Boolean finish) {
        Matcher m = Pattern.compile("http://4pda.ru/forum/index.php\\?autocom=qms&mid=(\\d+)").matcher(url);
        if (m.find()) {
            QmsChatActivity.openChat(context, m.group(1), "");

            if (finish)
                context.finish();
            return true;
        }
        return false;
    }

    public static boolean tryShowPm(Activity context, String url, Boolean finish) {
        Matcher m = Pattern.compile("http://4pda.ru/forum/index.php\\?act=Msg&CODE=4&MID=(\\d+)").matcher(url);
        if (m.find()) {
            EditMailActivity.sendMessage(context, "CODE=04&act=Msg&MID=" + m.group(1), "", true);


            if (finish)
                context.finish();
            return true;
        }
        return false;
    }

    private static boolean tryShowFile(final Activity activity, final Handler handler, final String url, final Boolean finish) {
        Pattern filePattern = Pattern.compile("http://4pda.ru/forum/dl/post/\\d+/.*");
        Pattern stFilePattern = Pattern.compile("http://st.4pda.ru/wp-content/uploads/.*");
        final Pattern imagePattern = Pattern.compile("http://.*?\\.(png|jpg|jpeg|gif)$");
        if (filePattern.matcher(url).find() || stFilePattern.matcher(url).find()) {
            if (!Client.INSTANCE.getLogined() && !Client.INSTANCE.hasLoginCookies()) {
                Client.INSTANCE.showLoginForm(activity, new Client.OnUserChangedListener() {
                    public void onUserChanged(String user, Boolean success) {
                        if (success) {
                            if (imagePattern.matcher(url).find()) {
                                showImage(activity, url);
                                if (finish)
                                    activity.finish();
                            } else
                                downloadFileStart(activity, handler, url, finish);
                        } else if (finish)
                            activity.finish();

                    }
                });
            } else {
                if (imagePattern.matcher(url).find()) {
                    showImage(activity, url);
                    if (finish)
                        activity.finish();
                } else
                    downloadFileStart(activity, handler, url, finish);
            }

            return true;
        }
        if (imagePattern.matcher(url).find()) {
            showImage(activity, url);
            if (finish)
                activity.finish();
            return true;
        }
        return false;
    }

    private static void showImage(Context context, String url) {
        ImageViewActivity.showImageUrl(context, url);
    }

    public static void downloadFileStart(final Activity activity, final Handler handler, final String url, final Boolean finish) {
        if (PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext()).getBoolean("files.ConfirmDownload", true)) {
            new AlertDialog.Builder(activity)
                    .setTitle("Уверены?")
                    .setMessage("Начать закачку файла?")
                    .setPositiveButton("ОК", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            DownloadsService.download(activity, url);
                            dialogInterface.dismiss();
                            if (finish)
                                activity.finish();
                        }
                    })
                    .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (finish)
                                activity.finish();
                        }
                    })
                    .create().show();
        } else {
            DownloadsService.download(activity, url);
            if (finish)
                activity.finish();
        }

    }

    public static void showInDefaultBrowser(Context context, String url) {
        try {
            Intent marketIntent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url));
            context.startActivity(Intent.createChooser(marketIntent, "Открыть с помощью"));
        } catch (Exception ex) {
            Log.e(context, new NotReportException("Не найдено ни одно приложение для ссылки: " + url));
        }
    }
}
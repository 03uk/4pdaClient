package org.softeg.slartus.forpda.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.text.TextUtils;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;
import org.softeg.slartus.forpda.MyApp;
import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpda.classes.common.StringUtils;
import org.softeg.slartus.forpda.common.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: slartus
 * Date: 19.10.12
 * Time: 22:08
 * To change this template use File | Settings | File Templates.
 */
public class DbHelper extends SQLiteAssetHelper {
    public static SimpleDateFormat DateTimeFormat = new SimpleDateFormat(
            "yyyy.MM.dd HH:mm:ss");
    private static final int DATABASE_VERSION = 4;
    private static final String DATABASE_NAME = "base";

    public DbHelper(Context context) throws IOException {
        super(context, DATABASE_NAME,MyApp.INSTANCE.getAppExternalFolderPath(), null, DATABASE_VERSION);
        setForcedUpgradeVersion(4);
    }


    public static Date parseDate(String text) throws ParseException {
        if (TextUtils.isEmpty(text))
            return null;
        return DateTimeFormat.parse(text);
    }


}

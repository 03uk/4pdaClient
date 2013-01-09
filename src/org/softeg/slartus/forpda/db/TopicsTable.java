package org.softeg.slartus.forpda.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.softeg.slartus.forpda.MyApp;
import org.softeg.slartus.forpda.classes.Topic;
import org.softeg.slartus.forpda.common.Log;

/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 22.11.12
 * Time: 7:25
 * To change this template use File | Settings | File Templates.
 */
public class TopicsTable {
    public static final String TABLE_NAME = "Topics";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_FORUM_ID = "ForumId";
    public static final String COLUMN_TITLE = "Title";
    public static final String COLUMN_DESCRIPTION = "Description";

    public static void addTopic(Topic topic) {
        SQLiteDatabase db = null;
        Cursor c = null;
        try {
            DbHelper dbHelper = new DbHelper(MyApp.INSTANCE);
            db = dbHelper.getWritableDatabase();

            String count = "SELECT count(*) FROM " + TABLE_NAME + " where _id=" + topic.getId();
            Cursor mcursor = db.rawQuery(count, null);
            mcursor.moveToFirst();
            int icount = mcursor.getInt(0);
            mcursor.close();

            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, topic.getId());
            values.put(COLUMN_FORUM_ID, topic.getForumId());
            values.put(COLUMN_TITLE, topic.getTitle().toString());
            values.put(COLUMN_DESCRIPTION, topic.getDescription()==null?null:topic.getDescription().toString());

            if(icount>0){
                db.update(TABLE_NAME, values,"_id=?",new String[]{topic.getId()});
            }else{
                db.insertOrThrow(TABLE_NAME, null, values);
            }
        } catch (Exception ex) {
            Log.e(MyApp.INSTANCE, ex);
        } finally {
            if (db != null) {
                if (c != null)
                    c.close();
                db.close();
            }
        }
    }
}

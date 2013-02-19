package org.softeg.slartus.forpda;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import org.softeg.slartus.forpda.classes.BbCodesPanel;
import org.softeg.slartus.forpda.classes.common.FileUtils;
import org.softeg.slartus.forpda.common.HtmlUtils;
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpda.emotic.SmilesBbCodePanel;
import org.softeg.slartus.forpda.topicview.ThemeActivity;
import org.softeg.slartus.forpdaapi.Post;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: slinkin
 * Date: 08.11.11
 * Time: 12:42
 */
public class EditPostPlusActivity extends SherlockFragmentActivity {
    private Button tglGallerySwitcher;
    private Gallery glrSmiles, glrBbCodes;
    private EditText txtPost;
    private ToggleButton tglEnableEmo, tglEnableSig;
    private Button btnAttachments;

    private String forumId;
    private String m_AttachFilePath;
    private String lastSelectDirPath = "/sdcard";
    private String themeId;
    private String postId;
    private String authKey;
    private String attachPostKey;
    // подтверждение отправки
    private Boolean m_ConfirmSend = true;
    // флаг добавлять подпись к сообщению
    private Boolean m_Enablesig = true;
    private Boolean m_EnableEmo = true;
    private int REQUEST_SAVE = 0;
    private int REQUEST_SAVE_IMAGE = 1;
    private MenuFragment mFragment1;
    private String postText;
    private View m_BottomPanel;

    public static void editPost(Context context, String forumId, String topicId, String postId, String authKey) {
        Intent intent = new Intent(context, EditPostPlusActivity.class);

        intent.putExtra("forumId", forumId);
        intent.putExtra("themeId", topicId);
        intent.putExtra("postId", postId);
        intent.putExtra("authKey", authKey);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        //getWindow().requestFeature(Window.FEATURE_NO_TITLE);

//        setTheme(MyApp.INSTANCE.getThemeStyleResID());
        setContentView(R.layout.edit_post_plus);

//        if (getResources().getBoolean(R.bool.screen_small))
//            getSupportActionBar().hide();

        createActionMenu();

        lastSelectDirPath = prefs.getString("EditPost.AttachDirPath", lastSelectDirPath);
        m_ConfirmSend = prefs.getBoolean("theme.ConfirmSend", true);
        m_BottomPanel = findViewById(R.id.bottomPanel);
        glrBbCodes = (Gallery) findViewById(R.id.glrBbCodes);
        glrSmiles = (Gallery) findViewById(R.id.glrSmiles);
        txtPost = (EditText) findViewById(R.id.txtPost);
        new BbCodesPanel(this, glrBbCodes, txtPost);
        new SmilesBbCodePanel(this, glrSmiles, txtPost);

        tglGallerySwitcher = (Button) findViewById(R.id.tglGallerySwitcher);
        tglGallerySwitcher.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                glrBbCodes.setVisibility(glrBbCodes.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                glrSmiles.setVisibility(glrSmiles.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                tglGallerySwitcher.setText(glrBbCodes.getVisibility() == View.VISIBLE ? ":)" : "Bb");
            }
        });
        tglEnableSig = (ToggleButton) findViewById(R.id.tglEnableSig);
        tglEnableEmo = (ToggleButton) findViewById(R.id.tglEnableEmo);

        btnAttachments = (Button) findViewById(R.id.btnAttachments);
        btnAttachments.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                showAttachesListDialog();
            }
        });


        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        forumId = extras.getString("forumId");
        themeId = extras.getString("themeId");
        postId = extras.getString("postId");
        authKey = extras.getString("authKey");
        if (isNewPost()) {
            txtPost.setText(extras.getString("body"));
        }
        startLoadPost();
    }

    private void createActionMenu() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        mFragment1 = (MenuFragment) fm.findFragmentByTag("f1");
        if (mFragment1 == null) {
            mFragment1 = new MenuFragment();
            ft.add(mFragment1, "f1");
        }
        ft.commit();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//
//        menu.add("Отправить")
//                .setIcon(android.R.drawable.ic_menu_send)
//                .setOnMenuItemClickListener(new android.view.MenuItem.OnMenuItemClickListener() {
//
//                    public boolean onMenuItemClick(android.view.MenuItem item) {
//
//                        final String body = getPostText();
//                        if (TextUtils.isEmpty(body))
//                            return true;
//
//                        if (getConfirmSend()) {
//                            new AlertDialog.Builder(EditPostPlusActivity.this)
//                                    .setTitle("Уверены?")
//                                    .setMessage("Подтвердите отправку")
//                                    .setPositiveButton("ОК", new DialogInterface.OnClickListener() {
//                                        public void onClick(DialogInterface dialogInterface, int i) {
//                                            dialogInterface.dismiss();
//                                            sendPost(body);
//
//                                        }
//                                    })
//                                    .setNegativeButton("Отмена", null)
//                                    .create().show();
//                        } else {
//                            sendPost(body);
//                        }
//
//                        return true;
//                    }
//                });
//
//
//        return super.onCreateOptionsMenu(menu);
//    }

    private Boolean isNewPost() {
        return postId.equals("-1");
    }

    private Dialog mAttachesListDialog;

    private void showAttachesListDialog() {
//        if (attaches.size() == 0) {
//            Toast.makeText(this, "Ни одного файла не загружено", Toast.LENGTH_SHORT).show();
//            return;
//        }
        String[] caps = new String[attaches.size()];
        int i = 0;
        for (Attach attach : attaches) {
            caps[i++] = attach.getName();
        }
        AttachesAdapter adapter = new AttachesAdapter(attaches, this);
        //  ListAdapter adapter = new ArrayAdapter<Attach>(this, R.layout.attachment_spinner_item, attaches);
        mAttachesListDialog = new AlertDialog.Builder(this)
                .setCancelable(true)
                .setTitle("Вложения")
                .setSingleChoiceItems(adapter, -1, null)
                .setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        startAddAttachment();
                    }
                })
                .setNegativeButton("Закрыть", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create();
        mAttachesListDialog.show();
    }

    private void startAddAttachment() {
        if (TextUtils.isEmpty(txtPost.getText().toString())) {
            Toast.makeText(EditPostPlusActivity.this, "Вы должны ввести сообщение", Toast.LENGTH_SHORT).show();
            return;
        }


        CharSequence[] items = new CharSequence[]{"Файл", "Изображение"};
        new AlertDialog
                .Builder(EditPostPlusActivity.this)
                .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        switch (i) {
                            case 0://файл
                                try {
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_GET_CONTENT);
                                    intent.setType("file/*");
                                    intent.setDataAndType(Uri.parse("file:/" + lastSelectDirPath), "file/*");
                                    startActivityForResult(intent, REQUEST_SAVE);

                                } catch (ActivityNotFoundException ex) {
                                    Toast.makeText(EditPostPlusActivity.this, "Ни одно приложение не установлено для выбора файла!", Toast.LENGTH_LONG).show();
                                } catch (Exception ex) {
                                    Log.e(EditPostPlusActivity.this, ex);
                                }

//                                Intent intent = new Intent(EditPostPlusActivity.this.getBaseContext(),
//                                        FileDialog.class);
//                                intent.putExtra(FileDialog.START_PATH, lastSelectDirPath);
//                                EditPostPlusActivity.this.startActivityForResult(intent, REQUEST_SAVE);
                                break;
                            case 1:// Изображение
                                try {
                                    Intent imageintent = new Intent(
                                            Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                                    startActivityForResult(imageintent, REQUEST_SAVE_IMAGE);
                                } catch (ActivityNotFoundException ex) {
                                    Toast.makeText(EditPostPlusActivity.this, "Ни одно приложение не установлено для выбора изображения!", Toast.LENGTH_LONG).show();
                                } catch (Exception ex) {
                                    Log.e(EditPostPlusActivity.this, ex);
                                }
                                break;
                        }
                    }
                }).create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode == RESULT_OK) {

                if (requestCode == REQUEST_SAVE) {
//                    m_AttachFilePath = data.getStringExtra(FileDialog.RESULT_PATH);
                    if(data.getData().getPath().startsWith("content://")) {
                        m_AttachFilePath = getRealPathFromURI(data.getData());
                    }else{
                        m_AttachFilePath = data.getData().getPath();
                    }


                    saveAttachDirPath();

                    m_Enablesig = tglEnableSig.isChecked();
                    m_EnableEmo = tglEnableEmo.isChecked();
                    new UpdateTask(EditPostPlusActivity.this).execute(txtPost.getText().toString());
                } else if (requestCode == REQUEST_SAVE_IMAGE) {

                    Uri selectedImage = data.getData();
//                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
//
//                    Cursor cursor = getContentResolver().query(selectedImage,
//                            filePathColumn, null, null, null);
//                    cursor.moveToFirst();
//
//                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    m_AttachFilePath = getRealPathFromURI(selectedImage);
                    //cursor.close();

                    m_Enablesig = tglEnableSig.isChecked();
                    m_EnableEmo = tglEnableEmo.isChecked();
                    new UpdateTask(EditPostPlusActivity.this).execute(txtPost.getText().toString());
                }
            }
        } catch (Exception ex) {
            Log.e(this, ex);
        }

    }

    public String getRealPathFromURI(Uri contentUri) {

        // can post image
        String [] filePathColumn={MediaStore.Images.Media.DATA};
        Cursor cursor =  getContentResolver().query( contentUri,
                filePathColumn, // Which columns to return
                null,       // WHERE clause; which rows to return (all rows)
                null,       // WHERE clause selection arguments (none)
                null); // Order-by clause (ascending by name)
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(column_index);
    }

    private void saveAttachDirPath() {
        lastSelectDirPath = FileUtils.getDirPath(m_AttachFilePath);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("EditPost.AttachDirPath", lastSelectDirPath);
        editor.commit();
    }

    private void startLoadPost() {
        new LoadTask(this).execute();
    }

    private void sendPost(final String text) {
        m_Enablesig = tglEnableSig.isChecked();
        m_EnableEmo = tglEnableEmo.isChecked();
        if (isNewPost()) {
            new PostTask(EditPostPlusActivity.this).execute(text);
        } else {
            new AcceptEditTask(EditPostPlusActivity.this).execute(text);
        }
    }

    private void parsePody(String body) {
        String startFlag = "<textarea name=\"Post\" rows=\"8\" cols=\"150\" style=\"width:98%; height:160px\" tabindex=\"0\">";
        int startIndex = body.indexOf(startFlag);
        startIndex += startFlag.length();
        int endIndex = body.indexOf("</textarea>", startIndex);

        if (TextUtils.isEmpty(txtPost.getText().toString()))
            txtPost.setText(HtmlUtils.modifyHtmlQuote(body.substring(startIndex, endIndex)));

        Pattern pattern = Pattern.compile("name='attach_post_key' value='(.*?)'");
        Matcher m = pattern.matcher(body);
        if (m.find()) {
            EditPostPlusActivity.this.attachPostKey = m.group(1);
        }
        parseAttaches(body);
    }

    private Attaches attaches = new Attaches();

    private void parseAttaches(String body) {
        Pattern pattern = Pattern.compile("onclick=\"insText\\('\\[attachment=(\\d+):(.*?)\\]'\\)");
        Pattern attachBodyPattern = Pattern.compile("<!-- ATTACH -->([\\s\\S]*?)</i>", Pattern.MULTILINE);
        Matcher m = attachBodyPattern.matcher(body);
        attaches = new Attaches();
        if (m.find()) {
            Matcher m1 = pattern.matcher(m.group(1));
            while (m1.find()) {
                attaches.add(new Attach(m1.group(1), m1.group(2)));
            }
        } else {
            Pattern checkPattern = Pattern.compile("\t\t<h4>Причина:</h4>\n" +
                    "\n" +
                    "\t\t<p>(.*?)</p>", Pattern.MULTILINE);
            m = checkPattern.matcher(body);
            if (m.find()) {
                Toast.makeText(this, m.group(1), Toast.LENGTH_LONG).show();
            }
        }
        btnAttachments.setText(attaches.size() + "");

    }

    public String getPostText() {
        return txtPost.getText().toString();
    }

    public boolean getConfirmSend() {
        return m_ConfirmSend;
    }


//
//    public static final class MenuFragment extends SherlockFragment {
//        public MenuFragment() {
//            super();
//        }
//
//        @Override
//        public void onCreate(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
//            setHasOptionsMenu(true);
//        }
//
//        @Override
//        public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
//            super.onCreateOptionsMenu(menu, inflater);
//            com.actionbarsherlock.view.MenuItem item;
//
////            item = menu.add("Отправить").setIcon(android.R.drawable.ic_menu_send);
////            //item.setVisible(Client.INSTANCE.getLogined());
////            item.setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
////                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
////                    return sendMail();
////                }
////            });
////            item.setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_ALWAYS);
//
//            item = menu.add("Отправить").setIcon(android.R.drawable.ic_menu_send);
//            //item.setVisible(Client.INSTANCE.getLogined());
//            item.setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
//                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
//                    return sendMail();
//                }
//            });
//            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
//
//        }
//
//        private boolean sendMail() {
//            final String body = ((EditPostPlusActivity) getActivity()).getPostText();
//            if (TextUtils.isEmpty(body))
//                return true;
//
//            if (((EditPostPlusActivity) getActivity()).getConfirmSend()) {
//                new AlertDialog.Builder(getActivity())
//                        .setTitle("Уверены?")
//                        .setMessage("Подтвердите отправку")
//                        .setPositiveButton("ОК", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                dialogInterface.dismiss();
//                                ((EditPostPlusActivity) getActivity()).sendPost(body);
//
//                            }
//                        })
//                        .setNegativeButton("Отмена", null)
//                        .create().show();
//            } else {
//                ((EditPostPlusActivity) getActivity()).sendPost(body);
//            }
//
//            return true;
//        }
//
//        public NewsActivity getInterface() {
//            return (NewsActivity) getActivity();
//        }
//    }

    private class UpdateTask extends AsyncTask<String, Void, Boolean> {


        private final ProgressDialog dialog;

        public UpdateTask(Context context) {

            dialog = new ProgressDialog(context);
        }

        String body;

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                body = Client.INSTANCE.attachFilePost(forumId, themeId, authKey, attachPostKey,
                        postId, m_Enablesig, m_EnableEmo, params[0], m_AttachFilePath, attaches.getFileList());
                return true;
            } catch (Exception e) {
                ex = e;
                return false;
            }
        }

        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.setMessage("Загрузка файла...");
            this.dialog.show();
        }

        private Exception ex;

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            if (success) {
                parseAttaches(body);

            } else {
                if (ex != null)
                    Log.e(EditPostPlusActivity.this, ex);
                else
                    Toast.makeText(EditPostPlusActivity.this, "Неизвестная ошибка", Toast.LENGTH_SHORT).show();

            }
        }

    }

    private class DeleteTask extends AsyncTask<String, Void, Boolean> {


        private final ProgressDialog dialog;

        public DeleteTask(Context context) {

            dialog = new ProgressDialog(context);
        }

        String body;

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                body = Client.INSTANCE.deleteAttachFilePost(forumId, themeId, authKey, postId, m_Enablesig, m_EnableEmo,
                        params[0],
                        m_AttachFilePath, attaches.getFileList());
                return true;
            } catch (Exception e) {
                ex = e;
                return false;
            }
        }

        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.setMessage("Удаление файла...");
            this.dialog.show();
        }

        private Exception ex;

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            if (success) {
                parseAttaches(body);

            } else {
                if (ex != null)
                    Log.e(EditPostPlusActivity.this, ex);
                else
                    Toast.makeText(EditPostPlusActivity.this, "Неизвестная ошибка", Toast.LENGTH_SHORT).show();

            }
        }

    }

    private class AcceptEditTask extends AsyncTask<String, Void, Boolean> {


        private final ProgressDialog dialog;

        public AcceptEditTask(Context context) {

            dialog = new ProgressDialog(context);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                Client.INSTANCE.editPost(forumId, themeId, authKey, postId, m_Enablesig, m_EnableEmo,
                        params[0], attaches.getFileList());
                return true;
            } catch (Exception e) {

                ex = e;
                return false;
            }
        }

        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.setMessage("Редактирование сообщения...");
            this.dialog.show();
        }

        private Exception ex;

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            if (success) {
                ThemeActivity.s_ThemeId = themeId;
                ThemeActivity.s_Params = "view=findpost&p=" + postId;
                EditPostPlusActivity.this.finish();
            } else {
                if (ex != null)
                    Log.e(EditPostPlusActivity.this, ex);
                else
                    Toast.makeText(EditPostPlusActivity.this, "Неизвестная ошибка",
                            Toast.LENGTH_SHORT).show();

            }
        }

    }

    private class LoadTask extends AsyncTask<String, Void, Boolean> {


        private final ProgressDialog dialog;

        public LoadTask(Context context) {

            dialog = new ProgressDialog(context);
        }

        String body;

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                body = Client.INSTANCE.getEditPostPlus(forumId, themeId, postId, authKey);
                return true;
            } catch (Throwable e) {

                ex = e;
                return false;
            }
        }

        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.setMessage("Загрузка сообщения...");
            this.dialog.show();
        }

        private Throwable ex;

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            if (success) {
                parsePody(body);
            } else {
                if (ex != null)
                    Log.e(EditPostPlusActivity.this, ex);
                else
                    Toast.makeText(EditPostPlusActivity.this, "Неизвестная ошибка", Toast.LENGTH_SHORT).show();

            }
        }

    }

    private class PostTask extends AsyncTask<String, Void, Boolean> {


        private final ProgressDialog dialog;
        private String mPostResult = null;
        private String mError = null;

        public PostTask(Context context) {

            dialog = new ProgressDialog(context);
        }


        @Override
        protected Boolean doInBackground(String... params) {
            try {
                mPostResult = Client.INSTANCE.reply(forumId, themeId, authKey, attachPostKey,
                        params[0], m_Enablesig, m_EnableEmo, false, attaches.getFileList());

                mError = Post.checkPostErrors(mPostResult);
                return true;
            } catch (Exception e) {

                ex = e;
                return false;
            }
        }

        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.setMessage("Отправка сообщения...");
            this.dialog.show();
        }

        private Exception ex;

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            if (success) {
                if (!TextUtils.isEmpty(mError)) {
                    Toast.makeText(EditPostPlusActivity.this, "Ошибка: " + mError, Toast.LENGTH_LONG).show();
                    return;
                }
                ThemeActivity.s_ThemeBody = mPostResult;
                if (isNewPost())
                    ThemeActivity.s_Params = "view=getlastpost";
                else
                    ThemeActivity.s_Params = "view=findpost&p=" + postId;

                EditPostPlusActivity.this.finish();
            } else {
                if (ex != null)
                    Log.e(EditPostPlusActivity.this, ex);
                else
                    Toast.makeText(EditPostPlusActivity.this, "Неизвестная ошибка",
                            Toast.LENGTH_SHORT).show();

            }
        }

    }

    public class AttachesAdapter extends BaseAdapter {
        private Activity activity;
        private final ArrayList<Attach> content;

        public AttachesAdapter(ArrayList<Attach> content, Activity activity) {
            super();
            this.content = content;
            this.activity = activity;
        }

        public int getCount() {
            return content.size();
        }

        public Attach getItem(int i) {
            return content.get(i);
        }

        public long getItemId(int i) {
            return i;
        }


        public View getView(final int position, View convertView, ViewGroup parent) {

            final ViewHolder holder;

            if (convertView == null) {
                final LayoutInflater inflater = activity.getLayoutInflater();

                convertView = inflater.inflate(R.layout.attachment_spinner_item, parent, false);


                holder = new ViewHolder();


                holder.btnDelete = (ImageButton) convertView
                        .findViewById(R.id.btnDelete);
                holder.btnDelete.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        mAttachesListDialog.dismiss();

                        Attach attach = (Attach) view.getTag();
                        m_AttachFilePath = attach.getId();
                        new DeleteTask(EditPostPlusActivity.this).execute(txtPost.getText().toString());
                    }
                });

                holder.txtFile = (TextView) convertView
                        .findViewById(R.id.txtFile);
                holder.txtFile.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        mAttachesListDialog.dismiss();
                        int selectionStart = txtPost.getSelectionStart();
                        if (selectionStart == -1)
                            selectionStart = 0;
                        Attach attach = (Attach) view.getTag();
                        txtPost.getText().insert(selectionStart, "[attachment=" + attach.getId() + ":" + attach.getName() + "]");
                    }
                });

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Attach attach = this.getItem(position);
            holder.btnDelete.setTag(attach);
            holder.txtFile.setText(attach.getName());
            holder.txtFile.setTag(attach);

            return convertView;
        }

        public class ViewHolder {

            ImageButton btnDelete;
            TextView txtFile;
        }
    }

    private class Attach {
        private String mId;
        private String mName;

        public Attach(String id, String name) {
            mId = id;
            mName = name;
        }

        public String getId() {
            return mId;
        }

        public String getName() {
            return mName;
        }

        @Override
        public String toString() {
            return mName;
        }
    }

    private class Attaches extends ArrayList<Attach> {
        public String getFileList() {
            String res = "0";
            for (Attach attach : this) {
                res += "," + attach.getId();
            }
            return res;
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (!getSupportActionBar().isShowing()) {
            getSupportActionBar().show();
            m_BottomPanel.setVisibility(View.VISIBLE);
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
//            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        return true;
    }

    public void hidePanels() {
        getSupportActionBar().hide();
        m_BottomPanel.setVisibility(View.GONE);
    }



    public static final class MenuFragment extends SherlockFragment {
        public MenuFragment() {
            super();
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
//            if (getResources().getBoolean(R.bool.screen_small))
//                getInterface().getSupportActionBar().hide();
        }

        @Override
        public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            com.actionbarsherlock.view.MenuItem item;

            item = menu.add("Отправить").setIcon(android.R.drawable.ic_menu_send);
            //item.setVisible(Client.INSTANCE.getLogined());
            item.setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                    return sendMail();
                }
            });
            item.setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_ALWAYS);


            item = menu.add("Скрыть панели").setIcon(R.drawable.ic_media_fullscreen);
            //item.setVisible(Client.INSTANCE.getLogined());
            item.setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                    getInterface().hidePanels();
//                    getInterface().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//                    getInterface().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                    return true;
                }
            });

            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }

        private boolean sendMail() {
            final String body = ((EditPostPlusActivity) getActivity()).getPostText();
            if (TextUtils.isEmpty(body))
                return true;

            if (((EditPostPlusActivity) getActivity()).getConfirmSend()) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Уверены?")
                        .setMessage("Подтвердите отправку")
                        .setPositiveButton("ОК", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                ((EditPostPlusActivity) getActivity()).sendPost(body);

                            }
                        })
                        .setNegativeButton("Отмена", null)
                        .create().show();
            } else {
                ((EditPostPlusActivity) getActivity()).sendPost(body);
            }

            return true;
        }

        public EditPostPlusActivity getInterface() {
            return (EditPostPlusActivity) getActivity();
        }
    }

}


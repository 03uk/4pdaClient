package org.softeg.slartus.forpda.classes;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.Toast;
import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpda.classes.common.UnZip;
import org.softeg.slartus.forpda.common.Log;


import java.io.*;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * User: slinkin
 * Date: 30.08.11
 * Time: 16:11
 */
public class BackupManager {

    private Context mContext;

    public BackupManager(Context context) {
        mContext = context;
    }

    public void backup() {
        ExportDatabaseFileTask task = new ExportDatabaseFileTask(mContext);
        task.execute(new String[0]);
    }

    public void restore() {
        showSelectBackupDialog();

    }

    private static SimpleDateFormat backupFormat = new SimpleDateFormat(
            "yyyy-MM-dd_HH-mm-ss");

    private String getBackupDirPath() {
        return Environment.getExternalStorageDirectory()
                + "/Android/data/org.softeg.slartus.nighttime/backups/";
    }

    private String getPrefsPath() {
        String packageName = mContext.getPackageName();
        return Environment.getDataDirectory() + "/data/" + packageName
                + "/shared_prefs/";
    }

    private class ExportDatabaseFileTask extends
            AsyncTask<String, Void, Boolean> {

        
        private final ProgressDialog dialog;

        public ExportDatabaseFileTask(Context context) {
            mContext = context;
            dialog = new ProgressDialog(mContext);
        }

        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.setMessage("Резервное копирование..");
            this.dialog.show();
        }

        private Exception ex;
        private String backupFilePath;

        // automatically done on worker thread (separate from UI thread)
        protected Boolean doInBackground(final String... args) {
            try {

                String packageName = mContext.getPackageName();

                File exportDir = new File(getBackupDirPath(), "");
                if (!exportDir.exists()) {
                    if (!exportDir.mkdirs()) {
                        throw new IOException("Не могу создать папку по пути: "+exportDir.getPath());
                    }
                }

                Date nowDate = new Date();
                backupFilePath = exportDir.getPath() + "/dbv"

                        + backupFormat.format(nowDate) + ".zip";
                ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(
                        backupFilePath));
                try {

                    savePrefs(zos, exportDir, packageName);
                } finally {
                    zos.close();
                }

                return true;
            } catch (IOException e) {
                ex = e;
                return false;
            }
        }


        private void savePrefs(ZipOutputStream zos, File exportDir,
                               String packageName) throws IOException {
            File prefsFolder = new File(Environment.getDataDirectory()
                    + "/data/" + packageName + "/shared_prefs/");
            File[] prefsFiles = prefsFolder.listFiles();
            if (prefsFiles != null)
                for (File pref : prefsFiles) {
                    addFileToZip(pref.getPath(), zos);
                }
        }

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            if (success) {
                Toast.makeText(mContext,
                        "Резервная копия создана!\n" + backupFilePath,
                        Toast.LENGTH_LONG).show();
            } else {
                Log.e(mContext, ex);
//                Toast.makeText(mContext, mContext.getResources().getString(R.string.backup_error) + ": " + ex.toString(),
//                        Toast.LENGTH_LONG).show();
            }
        }

        private void addFileToZip(String source, ZipOutputStream zos)
                throws IOException {
            FileInputStream fis = new FileInputStream(source);
            try {
                File file = new File(source);
                zos.putNextEntry(new ZipEntry(file.getName()));

                int size = 0;
                byte[] buffer = new byte[1024];

                while ((size = fis.read(buffer, 0, buffer.length)) > 0) {
                    zos.write(buffer, 0, size);
                }

                zos.closeEntry();
            } finally {
                fis.close();
            }

        }

    }

    private void showSelectBackupDialog() {
        File exportDir = new File(getBackupDirPath(), "");
        if (!exportDir.exists()) {
            Toast.makeText(mContext,"Папка с резервными копиями не найдена: "+exportDir.getPath(),
                    Toast.LENGTH_LONG).show();
            return;
        }

        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String filename) {

                return filename.startsWith("dbv") && filename.endsWith(".zip");
            }
        };

        final String[] files = exportDir.list(filter);

        if (files == null || files.length == 0) {
            Toast.makeText(mContext, "Папка не содержит резервных копий",
                    Toast.LENGTH_LONG).show();
            return;
        }

        new AlertDialog.Builder(mContext).setTitle("Выберите файл")
                .setItems(files, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        final String selectedFile = files[which];

                        ResotreDatabaseFileTask task = new ResotreDatabaseFileTask(
                                mContext);
                        task.execute(selectedFile);


                    }
                }).create().show();

    }

    private class ResotreDatabaseFileTask extends
            AsyncTask<String, Void, Boolean> {

        Context mContext;
        private final ProgressDialog dialog;

        public ResotreDatabaseFileTask(Context context) {
            mContext = context;
            dialog = new ProgressDialog(mContext);
        }

        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.setMessage("Восстановление настроек");
            this.dialog.show();
        }

        private Exception ex;

        private String selectedBackup;

        // automatically done on worker thread (separate from UI thread)
        protected Boolean doInBackground(final String... args) {
            try {
                selectedBackup = args[0];

                File exportDir = new File(getBackupDirPath(), "");
                if (!exportDir.exists()) {
                    if (!exportDir.mkdirs()) {
                        ex = new Exception("Папка с резервными копиями не найдена");
                        return false;
                    }
                }
                String backupDirPath = getBackupDirPath();
                File backupFile = new File(backupDirPath + selectedBackup);
                String unzipTempDirPath = backupDirPath + "/temp/";
                UnZip unZip = new UnZip(backupFile, unzipTempDirPath);
                if (!unZip.run()) {
                    ex = new Exception("Ошибка разархивирования");
                    return false;
                }


                restorePrefs(unzipTempDirPath);
                deleteDirectory(new File(unzipTempDirPath));
                return true;
            } catch (Exception e) {
                // TODO Auto-generated catch block
                ex = e;
                return false;
            }


        }

        public boolean deleteDirectory(File path) {
            if (path.exists()) {
                File[] files = path.listFiles();
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    } else {
                        files[i].delete();
                    }
                }
            }
            return (path.delete());
        }


        private void restorePrefs(String backupDirPath) throws IOException {
            File backupDir = new File(backupDirPath);
            File prefsFolder = new File(getPrefsPath());
            File[] prefsFiles = backupDir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String filename) {
                    // TODO Auto-generated method stub
                    return filename.endsWith(".xml");
                }
            });
            if (prefsFiles != null)
                for (File pref : prefsFiles) {
                    File file = new File(prefsFolder, pref.getName());
                    file.createNewFile();
                    copyFile(pref, file);
                }
        }

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            if (success) {
                Toast.makeText(mContext,"Настройки воостановлены. Необходимо перезапустить программу",
                        Toast.LENGTH_SHORT).show();

            } else {
                Log.e(null, ex, false);
            }
        }

        private void copyFile(File src, File dst) throws IOException {
            FileChannel inChannel = new FileInputStream(src).getChannel();
            FileChannel outChannel = new FileOutputStream(dst).getChannel();
            try {
                inChannel.transferTo(0, inChannel.size(), outChannel);
            } finally {
                if (inChannel != null)
                    inChannel.close();
                if (outChannel != null)
                    outChannel.close();
            }
        }

    }
}
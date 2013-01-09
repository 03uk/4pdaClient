package org.softeg.slartus.forpda.classes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import org.softeg.slartus.forpda.Client;

/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 15.10.12
 * Time: 9:24
 * To change this template use File | Settings | File Templates.
 */
public class ShowInBrowserDialog {
    public static void showDialog(final Context context, ShowInBrowserException ex){
        showDialog(context,"Ошибка",ex.getMessage(),ex.Url);
    }
    public static void showDialog(final Context context, String title, String message, final String url){
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message+"\nОткрыть ссылку в браузере?")
                .setPositiveButton("Открыть", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                        Intent marketIntent = new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(url));
                        context.startActivity(Intent.createChooser(marketIntent, "Выберите"));
                    }
                })
                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create().show();
    }
}

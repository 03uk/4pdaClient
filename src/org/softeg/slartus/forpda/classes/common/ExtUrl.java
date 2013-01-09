package org.softeg.slartus.forpda.classes.common;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * Created by IntelliJ IDEA.
 * User: slartus
 * Date: 27.10.12
 * Time: 16:20
 * To change this template use File | Settings | File Templates.
 */
public class ExtUrl {

    public static void showInBrowser(Context context, String url) {
        Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(Intent.createChooser(marketIntent, "Выберите"));
    }

    public static void shareIt(Context context, String url) {
        Intent sendMailIntent = new Intent(Intent.ACTION_SEND);
        sendMailIntent.putExtra(Intent.EXTRA_SUBJECT, url);
        sendMailIntent.putExtra(Intent.EXTRA_TEXT, url);
        sendMailIntent.setType("text/plain");

        context.startActivity(Intent.createChooser(sendMailIntent, "Поделиться через.."));
    }

    public static void copyLinkToClipboard(Context context, String link) {
        StringUtils.copyToClipboard(context, link);
        Toast.makeText(context, "Ссылка скопирована в буфер обмена", Toast.LENGTH_SHORT).show();
    }

    public static void addUrlSubMenu(final Context context, Menu menu, final String url) {
        addUrlMenu(context, menu.addSubMenu("Ссылка.."), url);
    }

    public static void addUrlMenu(final Context context, Menu menu, final String url) {
        menu.add("Открыть в браузере").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                showInBrowser(context, url);
                return true;
            }
        });

        menu.add("Поделиться ссылкой").setIcon(android.R.drawable.ic_menu_share).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                shareIt(context, url);
                return true;
            }
        });

        menu.add("Скопировать ссылку").setIcon(android.R.drawable.ic_menu_share).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                copyLinkToClipboard(context, url);
                return true;
            }
        });
    }

    public static void showSelectActionDialog(final Context context, final String url) {
        CharSequence[] items = new CharSequence[]{"Открыть в браузере", "Поделиться ссылкой", "Скопировать ссылку"};
        new AlertDialog.Builder(context)
                .setTitle(url)
                .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        switch (i) {
                            case 0://Открыть в браузере
                                showInBrowser(context, url);
                                break;
                            case 1:// Поделиться ссылкой
                                shareIt(context, url);
                                break;
                            case 2:// Скопировать ссылку
                                copyLinkToClipboard(context, url);
                                break;
                        }
                    }
                }
                )
                .setNegativeButton("Отмена",null)
                .setCancelable(true)
                .create()
                .show();
    }
}

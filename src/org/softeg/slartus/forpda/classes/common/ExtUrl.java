package org.softeg.slartus.forpda.classes.common;

import android.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
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

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(org.softeg.slartus.forpda. R.layout.link_dialog, null);

        final AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Ссылка...")
                .setView(layout)
                .setNegativeButton("Отмена",null)
                .setCancelable(true)
                .create();

        ((TextView)layout.findViewById(org.softeg.slartus.forpda.R.id.text)).setText(url);
        layout.findViewById(org.softeg.slartus.forpda.R.id.rbShowInBrowser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                showInBrowser(context, url);
            }
        });
        layout.findViewById(org.softeg.slartus.forpda.R.id.rbShareIt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                shareIt(context, url);
            }
        });
        layout.findViewById(org.softeg.slartus.forpda.R.id.rbCopyToClipboard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                copyLinkToClipboard(context, url);
            }
        });


        dialog.show();

    }
}

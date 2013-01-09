package org.softeg.slartus.forpda.classes;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import org.softeg.slartus.forpda.MyApp;
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpda.emotic.Smiles;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * User: slinkin
 * Date: 16.03.12
 * Time: 9:46
 */
public class BbCodesPanel extends BbCodesBasePanel {

    public BbCodesPanel(Context context, Gallery gallery, EditText editText) {
        super(context, gallery, editText);


        gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    String bbCode = m_BbCodes[i];
                    if (bbCode.equals("LIST")) {
                        getListBbCodeOnClickListener("");
                    } else if (bbCode.equals("NUMLIST")) {
                        getListBbCodeOnClickListener("=1");
                    } else if (bbCode.equals("URL")) {
                        getUrlBbCodeOnClickListener();
                    } else if (bbCode.equals("SPOILER")) {
                        getSpoilerBbCodeOnClickListener(i);
                    } else {

                        bbCodeClick(i);

                    }
                } catch (Exception ex) {
                    org.softeg.slartus.forpda.common.Log.e(mContext, ex);
                }
            }
        });


    }

    private void getSpoilerBbCodeOnClickListener(int tagIndex) {
        int selectionStart = txtPost.getSelectionStart();
        int selectionEnd = txtPost.getSelectionEnd();
        if (selectionEnd < selectionStart && selectionEnd != -1) {
            int c = selectionStart;
            selectionStart = selectionEnd;
            selectionEnd = c;
        }
        String spoilerText = null;
        if (selectionStart != -1 && selectionStart != selectionEnd) {
            spoilerText = txtPost.getText().toString()
                    .substring(selectionStart, selectionEnd);
        } else {
            if (mNotClosedCodes[tagIndex] > 0) {
                txtPost.getText().insert(selectionStart, "[/SPOILER]");
                mNotClosedCodes[tagIndex]--;
                return;
            } else if (mNotClosedCodes[mNotClosedCodes.length - 1] > 0) {
                txtPost.getText().insert(selectionStart, "[/SPOIL]");
                mNotClosedCodes[mNotClosedCodes.length - 1]--;
                return;
            }
        }
        createSpoilerDialog(spoilerText, tagIndex);
    }

    private void createSpoilerDialog(final String spoilerText, final int tagIndex) {


        LinearLayout layout = new LinearLayout(mContext);

        layout.setPadding(5, 5, 5, 5);
        layout.setOrientation(LinearLayout.VERTICAL);

        final TextView tx = new TextView(mContext);
        tx.setText("Название спойлера");
        layout.addView(tx);

        // Set an EditText view to get user input
        final EditText input = new EditText(mContext);

        input.requestFocus();
        layout.addView(input);
        AlertDialog alertDialog = new AlertDialog.Builder(mContext)
                .setView(layout)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                        int selectionStart = txtPost.getSelectionStart();
                        int selectionEnd = txtPost.getSelectionEnd();
                        if (selectionEnd < selectionStart && selectionEnd != -1) {
                            int c = selectionStart;
                            selectionStart = selectionEnd;
                            selectionEnd = c;
                        }
                        String spoilerName = input.getText().toString();
                        if (!TextUtils.isEmpty(spoilerName))
                            spoilerName = "=" + spoilerName;
                        String bbcode = TextUtils.isEmpty(spoilerName) ? "SPOILER" : "SPOIL";
                        String startSpoiler = "[" + bbcode + spoilerName + "]";

                        if (selectionStart != -1 && selectionStart != selectionEnd)
                            txtPost.getText().replace(selectionStart, selectionEnd, startSpoiler + spoilerText + "[/" + bbcode + "]");
                        else {
                            txtPost.getText().insert(selectionStart, startSpoiler);
                            mNotClosedCodes[TextUtils.isEmpty(spoilerName) ? tagIndex : (mNotClosedCodes.length - 1)]++;
                        }
                    }
                })
                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                        return;

                    }
                }).create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            public void onShow(DialogInterface dialogInterface) {
                input.requestFocus();
                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Service.INPUT_METHOD_SERVICE);
                imm.showSoftInput(input, 0);
            }
        });
        alertDialog.show();
    }

    @Override
    protected void initVars() {
        m_BbCodes = new String[]{"B", "I", "U", "S", "SUB", "SUP", "LEFT", "CENTER",
                "RIGHT", "URL", "QUOTE", "OFFTOP", "CODE", "SPOILER", "HIDE", "LIST", "NUMLIST"};
        mNotClosedCodes = new int[m_BbCodes.length + 1];  //+1 - для спойлера с отрицательным индексом
    }

    @Override
    protected String[] getImages() {
        String style = MyApp.INSTANCE.getCurrentThemeName();

        String[] res = new String[m_BbCodes.length];
        String path = "forum/style_images/1/folder_editor_buttons_" + style + "/";
        for (int i = 0; i < res.length; i++)
            res[i] = path + m_BbCodes[i].toLowerCase() + ".png";

        return res;
    }

    private String[] m_BbCodes;
    private int[] mNotClosedCodes;


    private void getUrlBbCodeOnClickListener() {

        int selectionStart = txtPost.getSelectionStart();
        int selectionEnd = txtPost.getSelectionEnd();
        if (selectionEnd < selectionStart && selectionEnd != -1) {
            int c = selectionStart;
            selectionStart = selectionEnd;
            selectionEnd = c;
        }
        String urlText = null;
        if (selectionStart != -1 && selectionStart != selectionEnd) {
            urlText = txtPost.getText().toString()
                    .substring(selectionStart, selectionEnd);
        }

        createUrlDialog(null, urlText, "Пожалуйста, введите полный URL адрес", "http://");

    }

    private void createUrlDialog(final String url, final String urlText, String captionText, String editText) {


        LinearLayout layout = new LinearLayout(mContext);
        layout.setPadding(5, 5, 5, 5);
        layout.setOrientation(LinearLayout.VERTICAL);

        final TextView tx = new TextView(mContext);
        tx.setText(captionText);
        layout.addView(tx);

        // Set an EditText view to get user input
        final EditText input = new EditText(mContext);
        input.setText(editText);
        input.requestFocus();
        layout.addView(input);
        AlertDialog alertDialog = new AlertDialog.Builder(mContext)
                .setCancelable(false)
                .setView(layout)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        String tempUrlText = urlText;
                        String tempUrl = url;
                        if (!TextUtils.isEmpty(url)) {
                            tempUrlText = input.getText().toString();
                        } else {
                            tempUrl = input.getText().toString();
                        }

                        if (TextUtils.isEmpty(tempUrlText)) {
                            createUrlDialog(input.getText().toString(), null, "Пожалуйста, введите заголовок", "Посетить мою домашнюю страницу");
                            return;
                        }
                        int selectionStart = txtPost.getSelectionStart();
                        int selectionEnd = txtPost.getSelectionEnd();
                        if (selectionEnd < selectionStart && selectionEnd != -1) {
                            int c = selectionStart;
                            selectionStart = selectionEnd;
                            selectionEnd = c;
                        }
                        txtPost.getText().replace(selectionStart, selectionEnd, "[URL=" + (tempUrl == null ? "" : tempUrl) + "]" + tempUrlText + "[/URL]");
                    }
                })
                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                        return;

                    }
                }).create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            public void onShow(DialogInterface dialogInterface) {
                input.requestFocus();
                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Service.INPUT_METHOD_SERVICE);
                imm.showSoftInput(input, 0);
            }
        });
        alertDialog.show();
    }

    private void getListBbCodeOnClickListener(final String listTagPostFix) throws IOException {

        int selectionStart = txtPost.getSelectionStart();
        int selectionEnd = txtPost.getSelectionEnd();
        if (selectionEnd < selectionStart && selectionEnd != -1) {
            int c = selectionStart;
            selectionStart = selectionEnd;
            selectionEnd = c;
        }
        if (selectionStart != -1 && selectionStart != selectionEnd) {
            String selectedText = txtPost.getText().toString()
                    .substring(selectionStart, selectionEnd);
            while (selectedText.indexOf("\n\n") != -1) {
                selectedText = selectedText.replace("\n\n", "\n");
            }
            String modifiedText = "[LIST" + listTagPostFix + "]"
                    + selectedText
                    .replaceAll("^", "[*]")
                    .replace("\n", "\n[*]")
                    + "[/LIST]";
            txtPost.getText().replace(selectionStart, selectionEnd, modifiedText);
            return;
        }
        StringBuilder sb = new StringBuilder();
        createListDialog(1, sb, listTagPostFix);

    }

    private void createListDialog(final int ind, final StringBuilder sb, final String listTagPostFix) {


        LinearLayout layout = new LinearLayout(mContext);
        layout.setPadding(5, 5, 5, 5);
        layout.setOrientation(LinearLayout.VERTICAL);

        final TextView tx = new TextView(mContext);
        tx.setText("Введите содержание " + ind + " пункта списка");
        layout.addView(tx);

        // Set an EditText view to get user input
        final EditText input = new EditText(mContext);
        input.requestFocus();
        layout.addView(input);

        AlertDialog alertDialog = new AlertDialog.Builder(mContext)
                .setCancelable(false)
                .setView(layout)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        if (input.getText().toString().isEmpty()) {
                            tryInsertListText(sb, listTagPostFix);
                            return;
                        }
                        sb.append("[*]" + input.getText().toString() + "\n");
                        createListDialog(ind + 1, sb, listTagPostFix);
                    }
                })
                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                        tryInsertListText(sb, listTagPostFix);
                        return;

                    }
                }).create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            public void onShow(DialogInterface dialogInterface) {
                input.requestFocus();
                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Service.INPUT_METHOD_SERVICE);
                imm.showSoftInput(input, 0);
            }
        });
        alertDialog.show();
    }

    private void tryInsertListText(StringBuilder sb, final String listTagPostFix) {
        String text = sb.toString().trim();
        if (TextUtils.isEmpty(text)) return;

        int selectionStart = txtPost.getSelectionStart();
        txtPost.getText().insert(selectionStart, "[LIST" + listTagPostFix + "]" + text + "[/LIST]");
    }

    private void bbCodeClick(int tagIndex) {
        String tag = m_BbCodes[tagIndex];

        int selectionStart = txtPost.getSelectionStart();
        int selectionEnd = txtPost.getSelectionEnd();
        if (selectionEnd < selectionStart && selectionEnd != -1) {
            int c = selectionStart;
            selectionStart = selectionEnd;
            selectionEnd = c;
        }
        if (selectionStart != -1 && selectionStart != selectionEnd) {
            txtPost.getText().insert(selectionStart, "[" + tag + "]");
            txtPost.getText().insert(selectionEnd + tag.length() + 2, "[/" + tag + "]");
            return;
        }

        if (mNotClosedCodes[tagIndex] > 0) {
            txtPost.getText().insert(selectionStart, "[/" + tag + "]");
            mNotClosedCodes[tagIndex]--;
        } else {
            txtPost.getText().insert(selectionStart, "[" + tag + "]");
            mNotClosedCodes[tagIndex]++;
        }
    }


}

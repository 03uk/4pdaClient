package org.softeg.slartus.forpda.emotic;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.softeg.slartus.forpda.MyApp;
import org.softeg.slartus.forpda.classes.BbCodesBasePanel;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 17.10.12
 * Time: 9:46
 * To change this template use File | Settings | File Templates.
 */
public class SmilesBbCodePanel extends BbCodesBasePanel {
    public SmilesBbCodePanel(Context context, Gallery gallery, EditText editText) {
        super(context, gallery, editText);
        gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try{
                    Smiles smiles=new Smiles();
                    tryInsertText(smiles.get(i).HtmlText);
                }
                catch (Exception ex) {
                    org.softeg.slartus.forpda.common.Log.e(mContext,ex);
                }
            }
        });

    }

    @Override
    protected String[] getImages() {
        return new Smiles().getFilesList();
    }


    private void tryInsertText(String text) {

        if (TextUtils.isEmpty(text)) return;

        int selectionStart = txtPost.getSelectionStart();
        txtPost.getText().insert(selectionStart, " "+text+" ");
    }
}

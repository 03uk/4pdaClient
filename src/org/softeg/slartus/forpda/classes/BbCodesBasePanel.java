package org.softeg.slartus.forpda.classes;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpda.emotic.Smiles;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 17.10.12
 * Time: 9:42
 * To change this template use File | Settings | File Templates.
 */
public abstract class BbCodesBasePanel {
    protected Context mContext;
    protected LinearLayout lnrBbCodes;
    protected EditText txtPost;
    public BbCodesBasePanel(Context context, Gallery gallery, EditText editText){
        mContext=context;
        initVars();
        gallery.setAdapter(new ImageAdapter(context,getImages()));
        txtPost=editText;

        gallery.setSelection(3, true);
    }

    protected void initVars() {

    }

    protected abstract String[] getImages() ;


    /**
     * Helper Functions
     *
     * @throws IOException
     */
    protected Bitmap getBitmapFromAsset(String strName) throws IOException {
        AssetManager assetManager = mContext.getAssets();
        // BufferedInputStream buf = new BufferedInputStream(assetManager.open(strName));
        InputStream istr = assetManager.open(strName);
        Bitmap bitmap = BitmapFactory.decodeStream(istr);

        return bitmap;
    }

    public class ImageAdapter extends BaseAdapter {
        /** The parent context */
        private Context myContext;
        private float m_Density;
        // Put some images to project-folder: /res/drawable/
        // format: jpg, gif, png, bmp, ...

        private String[] m_Images=null;
        /** Simple Constructor saving the 'parent' context. */
        public ImageAdapter(Context c, String[] images) {
            this.myContext = c;
            m_Images=images;
            m_Density= mContext.getResources().getDisplayMetrics().density;
        }

        // inherited abstract methods - must be implemented
        // Returns count of images, and individual IDs
        public int getCount() {
            return m_Images.length;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }



        // Returns a new ImageView to be displayed,
        public View getView(int position, View convertView,
                            ViewGroup parent) {
            if (convertView == null) {
                // Get a View to display image data
                convertView = new ImageView(this.myContext);

                ((ImageView)convertView).setScaleType(ImageView.ScaleType.FIT_END);
                // Set the Width & Height of the individual images
                convertView.setLayoutParams(new Gallery.LayoutParams((int) (m_Density * 30), (int) (m_Density * 30)));
            }

            try {
                ((ImageView)convertView).setImageBitmap(getBitmapFromAsset(m_Images[position]));
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            return convertView;
        }
    }
}

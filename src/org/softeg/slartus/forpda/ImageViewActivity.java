package org.softeg.slartus.forpda;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import org.apache.http.HttpEntity;
import org.softeg.slartus.forpda.classes.DownloadTask;
import org.softeg.slartus.forpda.classes.TouchImage.TouchImageView;
import org.softeg.slartus.forpda.classes.common.ExtDisplay;
import org.softeg.slartus.forpda.classes.common.FileUtils;
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpda.download.DownloadsService;

import java.io.*;
import java.net.URLEncoder;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.StreamHandler;

/**
 * User: slinkin
 * Date: 28.11.11
 * Time: 14:04
 */
public class ImageViewActivity extends BaseActivity {
    private static final int COMPLETE = 0;
    private static final int FAILED = 1;
    private static final String URL_KEY = "url";
    private TouchImageView mImage;
    private ProgressBar mSpinner;
    private Drawable mDrawable;
    private Bitmap mBitmap;
    private String mUrl;
    private Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.image_view_activity);

        mSpinner = (ProgressBar) findViewById(R.id.progress);

        mImage = (TouchImageView) findViewById(R.id.image);
        mImage.setClickable(true);


        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        mUrl = extras.getString(URL_KEY);

        setImageDrawable(mUrl);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Скачать")
                .setIcon(android.R.drawable.ic_menu_view)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                    public boolean onMenuItemClick(MenuItem item) {

                        DownloadsService.download(ImageViewActivity.this, mUrl);
                        return true;
                    }
                });
        menu.add("Закрыть")
                .setIcon(android.R.drawable.ic_menu_close_clear_cancel)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                    public boolean onMenuItemClick(MenuItem item) {

                        finish();
                        return true;
                    }
                });

        return true;
    }


    public static void showImageUrl(Context activity, String imgUrl) {
        try {
            Intent intent = new Intent(activity, ImageViewActivity.class);
            intent.putExtra(ImageViewActivity.URL_KEY, imgUrl);
            activity.startActivity(intent);
        } catch (Exception ex) {
            Log.e(activity, ex);
        }
    }

    /**
     * Callback that is received once the image has been downloaded
     */
    private final Handler imageLoadedHandler = new Handler(new Handler.Callback() {

        public boolean handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case COMPLETE:
                        Display display = getWindowManager().getDefaultDisplay();
                        Point displaySize = ExtDisplay.getDisplaySize(display);
                        // mImage.setDrawable(mDrawable, width, height);
                        mImage.setImage(mBitmap, displaySize.x, displaySize.y);
                        mImage.setVisibility(View.VISIBLE);
                        mSpinner.setVisibility(View.GONE);
                        break;
                    case FAILED:
                        mSpinner.setVisibility(View.GONE);
                        Bundle data = msg.getData();
                        Log.e(ImageViewActivity.this, data.getString("message"), (Throwable) data.getSerializable("exception"));
                    default:
                        // Could change image here to a 'failed' image
                        // otherwise will just keep on spinning
                        break;
                }
            } catch (Exception ex) {
                mSpinner.setVisibility(View.GONE);
                Log.e(ImageViewActivity.this, "Ошибка загрузки изображения по адресу: " + mUrl, ex);
            }

            return true;
        }
    });

    /**
     * Set's the view's drawable, this uses the internet to retrieve the image
     * don't forget to add the correct permissions to your manifest
     *
     * @param imageUrl the url of the image you wish to load
     */
    private void setImageDrawable(final String imageUrl) {
        mDrawable = null;
        mSpinner.setVisibility(View.VISIBLE);
        mImage.setVisibility(View.GONE);
        new Thread() {
            public void run() {
                HttpHelper httpHelper = new HttpHelper();
                try {
                    BitmapFactory.Options o = new BitmapFactory.Options();
                    o.inJustDecodeBounds = true;
                    //mDrawable = Drawable.createFromStream(httpHelper.getImageStream(imageUrl), "name");
                    mBitmap = getBitmap(imageUrl);
                    imageLoadedHandler.sendEmptyMessage(COMPLETE);

                } catch (OutOfMemoryError e) {
                    Bundle data = new Bundle();
                    data.putSerializable("exception", e);
                    data.putString("message", "Нехватка памяти: " + mUrl);
                    Message message = new Message();
                    message.what = FAILED;
                    message.setData(data);
                    imageLoadedHandler.sendMessage(message);
                } catch (Exception e) {
                    Bundle data = new Bundle();
                    data.putSerializable("exception", e);
                    data.putString("message", "Ошибка загрузки изображения по адресу: " + mUrl);
                    Message message = new Message();
                    message.what = FAILED;
                    message.setData(data);
                    imageLoadedHandler.sendMessage(message);

                } finally {
                    httpHelper.close();
                }
            }


        }.start();
    }

    private String downloadImage(String imageUrl) throws Exception {
        HttpHelper httpHelper = new HttpHelper();
        try {
            File file = File.createTempFile("temp_image", ".tmp");


            long total = 0;


            String url = imageUrl;
            HttpEntity entity = httpHelper.getDownloadResponse(url, total);

            long fileLength = entity.getContentLength() + total;

            int count;
            int percent = 0;
            int prevPercent = 0;

            Date lastUpdateTime = new Date();
            Boolean first = true;

            InputStream in = entity.getContent();
            FileOutputStream output = new FileOutputStream(file, true);

            byte data[] = new byte[1024];
            try {
                while ((count = in.read(data)) != -1) {
                    output.write(data, 0, count);
                    total += count;

                    percent = (int) ((float) total / fileLength * 100);

                    long diffInMs = new Date().getTime() - lastUpdateTime.getTime();
                    long diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMs);

                    if ((percent != prevPercent && diffInSec > 1) || first) {
                        lastUpdateTime = new Date();
                        first = false;
                    }
                    prevPercent = percent;
                }

            } finally {
                output.flush();
                output.close();
                in.close();
            }
            return file.getPath();
        } finally {
            httpHelper.close();
        }

    }

    private Bitmap getBitmap(String imageUrl) throws Exception {

        HttpHelper httpHelper = new HttpHelper();
        String tempPath = downloadImage((imageUrl));
        try {


            InputStream in = new FileInputStream(tempPath);
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, o);

            int origWidth = o.outWidth; //исходная ширина
            int origHeight = o.outHeight; //исходная высота
            in.close();
            int bytesPerPixel = 2; //соответствует RGB_555 конфигурации
            int maxSize = 480 * 800 * bytesPerPixel; //Максимально разрешенный размер Bitmap
            int desiredWidth = 480; //Нужная ширина
            int desiredHeight = 800; //Нужная высота
            int desiredSize = desiredWidth * desiredHeight * bytesPerPixel; //Максимально разрешенный размер Bitmap для заданных width х height
            if (desiredSize < maxSize) maxSize = desiredSize;
            int scale = 1; //кратность уменьшения
            int origSize = origWidth * origHeight * bytesPerPixel;
//высчитываем кратность уменьшения
            if (origWidth > origHeight) {
                scale = Math.round((float) origHeight / (float) desiredHeight);
            } else {
                scale = Math.round((float) origWidth / (float) desiredWidth);
            }

            o = new BitmapFactory.Options();
            o.inSampleSize = scale;
            o.inPreferredConfig = Bitmap.Config.RGB_565;

            in = new FileInputStream(tempPath); //Ваш InputStream. Важно - открыть его нужно еще раз, т.к второй раз читать из одного и того же InputStream не разрешается (Проверено на ByteArrayInputStream и FileInputStream).
            return BitmapFactory.decodeStream(in, null, o); //Полученный Bitmap
        } finally {
            try {
                new File(tempPath).delete();
            } catch (Exception ignoredEx) {
                Log.e(null, ignoredEx);
            }
        }
    }

    private static Drawable getDrawableFromUrl(final String url) throws Throwable {
        HttpHelper httpHelper = new HttpHelper();
        return Drawable.createFromStream(httpHelper.getImageStream(url), "name");
    }

}

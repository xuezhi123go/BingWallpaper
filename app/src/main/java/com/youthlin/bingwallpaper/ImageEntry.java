package com.youthlin.bingwallpaper;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by lin on 2016-02-28-028.
 */
public class ImageEntry {
    public String mDate, mUrlBase, mCopyright, mLink, mFilePath;
    private Bitmap mbitmap;

    public ImageEntry(String date, String urlbase, String copyright, String link, String path) {
        mDate = date;
        mUrlBase = urlbase;
        mCopyright = copyright;
        mLink = link;
        mFilePath = path;
    }

    public Bitmap getBitmap() {
        return mbitmap;
    }

    public void setBitmap(Bitmap b) {
        mbitmap = b;
    }

    public static ArrayList<ImageEntry> getList(Context context) {
        File path = new File(ConstValues.savePath);
        if (!path.exists()) {
            boolean mkdirs = path.mkdirs();
            Log.d(ConstValues.TAG, "创建目录成功吗：" + mkdirs);
        }
        if (ConstValues.dbPath == null) {
            ConstValues.dbPath = context.getFilesDir().getAbsolutePath() + "/";
        }
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(
                ConstValues.dbPath + ConstValues.dbName, null);
        //创建表(已有则不创建)
        db.execSQL("create table if not exists " + ConstValues.tableName
                + "(date primary key,urlbase,copyright,copyrightlink,filepath)");

        Cursor c;
        c = db.rawQuery("SELECT * FROM " + ConstValues.tableName + " ORDER BY date DESC",
                new String[]{});
        String date, urlbase, copyright, link, filepath;
        ArrayList<ImageEntry> list = new ArrayList<>();
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        Date tmpDate;
        while (c.moveToNext()) {
            date = c.getString(c.getColumnIndex("date"));
            urlbase = c.getString(c.getColumnIndex("urlbase"));
            copyright = c.getString(c.getColumnIndex("copyright"));
            link = c.getString(c.getColumnIndex("copyrightlink"));
            filepath = c.getString(c.getColumnIndex("filepath"));
            try {
                tmpDate = sdf2.parse(date);
                date = sdf1.format(tmpDate);
            } catch (ParseException e) {
                e.printStackTrace();
                Log.d(ConstValues.TAG, "格式化日期出错,使用默认格式");
                date = c.getString(c.getColumnIndex("date"));
            }
            list.add(new ImageEntry(date, urlbase, copyright, link, filepath));
        }
        c.close();
        db.close();
        return list;
    }

    public static boolean downImg(Context context, String urlbase, File img) {
        try {
            URL url = new URL("http://www.bing.com/" + urlbase + "_1080x1920.jpg");
            InputStream is = url.openStream();
            OutputStream os = new FileOutputStream(img);
            byte[] buf = new byte[4096];
            int hasread;
            while ((hasread = is.read(buf)) > 0) {
                os.write(buf, 0, hasread);
            }
            ImageEntry.updateMediaFile(context, img.getAbsolutePath());
            is.close();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        Log.d(ConstValues.TAG, img.getPath() + "已下载");
        return true;
    }

    //通知媒体库更新文件
    //Android通过广播更新文件和文件夹到媒体库-2015年03月28日
    //http://zmywly8866.github.io/2015/03/28/user-toast-scan-mediafile.html
    public static void updateMediaFile(Context context, String filePath) {
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        scanIntent.setData(Uri.fromFile(new File(filePath)));
        context.sendBroadcast(scanIntent);
        //Log.d(ConstValues.TAG, "发送了广播-文件");
    }

}

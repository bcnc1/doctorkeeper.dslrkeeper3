package com.doctorkeeper.dslrkeeper2022.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.webkit.MimeTypeMap;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class DisplayUtil {

    private static final String TAG = "DisplayUtil";

    public static final float dpToPx(Context c, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, c.getResources().getDisplayMetrics());
    }

    public static boolean saveBitmap(Bitmap bitmap, String destination, boolean recyle){
        if(bitmap == null || TextUtils.isEmpty(destination)){
            return false;
        }

        BufferedOutputStream bos = null;
        try{
            FileOutputStream fos = new FileOutputStream(destination);
            bos = new BufferedOutputStream(fos);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false ;
        }finally {
            if (bos != null) {
                try {
                    bos.flush();
                    bos.close();
                } catch (IOException e) {
                }
            }
            if (recyle) {
                bitmap.recycle();
            }
        }
    }

    static final int THUMB_WIDTH = 255;
    static final int THUMB_HEIGHT = 170;

    public static final Bitmap getThumbBitMapImage(String filePath){
        Bitmap source = BitmapFactory.decodeFile(filePath);

        return ThumbnailUtils.extractThumbnail(source, THUMB_WIDTH, THUMB_HEIGHT, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
    }


    public static String storeThumbPtictureImage(String sourcePath, File storePath, String fileName){
        String path = null;

        Bitmap source = BitmapFactory.decodeFile(sourcePath);


        Bitmap bitmapThumb = ThumbnailUtils.extractThumbnail(source, THUMB_WIDTH, THUMB_HEIGHT,ThumbnailUtils.OPTIONS_RECYCLE_INPUT);


       // Bitmap rotateThumb = rotate(bitmapThumb, 90);//90도회전


        File file = new File(storePath, "/thumbnail/");

        if (!file.isDirectory()) {
            file.mkdir();
        }

        try {
            //Log.i(TAG, "파일패스 = "+file.getAbsolutePath());
            FileOutputStream outStream = new FileOutputStream(file.getAbsolutePath()+ File.separator +fileName); //파일저장

            bitmapThumb.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            //rotateThumb.compress(Bitmap.CompressFormat.JPEG, 100, outStream); //
            outStream.close();

            path = file.getAbsolutePath()+ File.separator +fileName;
            return path;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return path;
        } catch (IOException e) {
            e.printStackTrace();
            return path;
        }


    }

    public static String storePtictureNThumbImage(String storeOriPath, File storeThumbPath, String fileName, Bitmap ori){
        String path = null;

        File file = new File(storeThumbPath, "/thumbnail/");

        if (!file.isDirectory()) {
            file.mkdir();
        }

        //store source image
        try{
            FileOutputStream outPicStream = new FileOutputStream(storeOriPath);
            ori.compress(Bitmap.CompressFormat.JPEG, 100, outPicStream);
            outPicStream.close();

        }catch (FileNotFoundException e){
            e.printStackTrace();
            return path;
        }catch (IOException e){
            e.printStackTrace();
            return path;
        }

        //store thumb image
        Bitmap source = BitmapFactory.decodeFile(storeOriPath);

        Bitmap bitmapThumb = ThumbnailUtils.extractThumbnail(source, THUMB_WIDTH, THUMB_HEIGHT,ThumbnailUtils.OPTIONS_RECYCLE_INPUT);

       // Bitmap rotateThumb = rotate(bitmapThumb, 90);//90도회전

        try {

            FileOutputStream outThumbStream = new FileOutputStream(file.getAbsolutePath()+ File.separator +fileName); //파일저장

          //  rotateThumb.compress(Bitmap.CompressFormat.JPEG, 100, outThumbStream); //
            bitmapThumb.compress(Bitmap.CompressFormat.JPEG, 100, outThumbStream);

            outThumbStream.close();

            path = file.getAbsolutePath()+ File.separator +fileName;
            return path;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return path;
        } catch (IOException e) {
            e.printStackTrace();
            return path;
        }
    }

    public static String storeDslrImage(String storeOriPath, File storeThumbPath, String fileName, Bitmap ori, Bitmap thumb){
        String path = null;

        File file = new File(storeThumbPath, "/thumbnail/");

        if (!file.isDirectory()) {
            file.mkdir();
        }

        try{
            FileOutputStream outSource = new FileOutputStream(storeOriPath); //파일저장
            ori.compress(Bitmap.CompressFormat.JPEG, 100, outSource);
            outSource.close();

            FileOutputStream outThumb = new FileOutputStream(file.getAbsolutePath()+ File.separator +fileName); //파일저장
            thumb.compress(Bitmap.CompressFormat.JPEG, 100, outThumb);
            outThumb.close();

            path = file.getAbsolutePath()+ File.separator +fileName;
            return path;

        }catch (FileNotFoundException e) {
            e.printStackTrace();
            return path;
        } catch (IOException e) {
            e.printStackTrace();
            return path;
        }

    }

    public static String storeThumbVideoImage(String sourcePath, File storePath, String fileName){

        String path = null;

        File file = new File(storePath, "/thumbnail/");
        path = file.getAbsolutePath()+ File.separator +fileName;
        if (!file.isDirectory()) {
            file.mkdir();
        }

//        try {
//            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(sourcePath, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
//            Bitmap thumbnail = ThumbnailUtils.extractThumbnail(bitmap, 255, 170);
//
//            Log.i(TAG, "파일패스 = "+file.getAbsolutePath());
//            FileOutputStream outStream = new FileOutputStream(file.getAbsolutePath()+ File.separator +fileName); //파일저장
//            thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
//            outStream.close();
//
//            return path;
//        } catch (FileNotFoundException e) {
//            Log.d(TAG, e.toString());
//            return path;
//        } catch (IOException e) {
//            Log.d(TAG, e.toString());
//            return path;
//        }
        return path;
    }

    private static Bitmap rotate(Bitmap bitmap, int degrees)
    {
        if(degrees != 0 && bitmap != null)
        {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) bitmap.getWidth() / 2,
                    (float) bitmap.getHeight() / 2);

            try
            {
                Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
                if(bitmap != converted)
                {
                    bitmap.recycle();
                    bitmap = converted;
                }
            }
            catch(OutOfMemoryError ex)
            {

                // 메모리가 부족하여 회전을 시키지 못할 경우 그냥 원본을 반환합니다.
            }
        }
        return bitmap;
    }

    public static void storeExifThumb(String sourcePath, File storePath, String fileName){

        ExifInterface exif = null;

        try {
            exif = new ExifInterface(sourcePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] imageData=exif.getThumbnail();

        if (imageData!=null) //it can not able to get the thumbnail for very small images , so better to check null
        {
            Bitmap  thumbnail= BitmapFactory.decodeByteArray(imageData,0,imageData.length);
            FileOutputStream outStream = null; //파일저장
            try {
                outStream = new FileOutputStream(storePath+ File.separator +fileName);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, outStream);
            try {
                outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void makeStatusNotification(Context con, String msg){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

        }
    }

    public static final String getMimeType(String path) {

        String extension = MimeTypeMap.getFileExtensionFromUrl(path);

        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
    }

}

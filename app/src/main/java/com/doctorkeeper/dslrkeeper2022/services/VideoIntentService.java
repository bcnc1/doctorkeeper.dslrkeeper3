package com.doctorkeeper.dslrkeeper2022.services;

import android.app.Activity;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.doctorkeeper.dslrkeeper2022.Constants;
import com.doctorkeeper.dslrkeeper2022.R;
import com.doctorkeeper.dslrkeeper2022.activities.FileExploreActivity;
import com.doctorkeeper.dslrkeeper2022.madamfive.MadamfiveAPI;
import com.doctorkeeper.dslrkeeper2022.models.PhotoModel;
import com.doctorkeeper.dslrkeeper2022.util.SmartFiPreference;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VideoIntentService extends IntentService {

    private static final String TAG = "VideoIntentService";
    private static final String EXTRA_VIDEO_ID = "com.thinoo.drcamlink.services.extra.video.id";
    private static String mAcccessToken = null;
    private static String mPatientId = null;
    private static String mHospitalId = null;
    private static String mDate =  null;
    private static String mMediaType = null;
    //private static int mNotiId = Constants.Notification.NOTIFICATION_VIDEO_ID;
    private static Context mCon;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     *  name Used to name the worker thread, important only for debugging.
     */
    public VideoIntentService() {
        super("VideoIntentService");
    }


    public static void startUploadVideo(Context context, long id) {
        mCon = context;
        Intent intent = new Intent(context, VideoIntentService.class);
        intent.putExtra(EXTRA_VIDEO_ID, id);
        context.startService(intent);

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

//        Log.d(TAG,"onHandleIntent 호출");
        mAcccessToken = SmartFiPreference.getSfToken(getApplicationContext());
        mDate = new SimpleDateFormat("yyyyMM").format(new Date());
        mHospitalId = SmartFiPreference.getHospitalId(getApplicationContext());

        if (intent != null) {
            Bundle extras = intent.getExtras();
            if(extras != null){
                long id = extras.getLong(EXTRA_VIDEO_ID);
//                Log.d(TAG,"id = "+id);
                PhotoModel photoModel = PhotoModelService.getPhotoModel(id);
                mPatientId = photoModel.getCustNo();
                makeNoti("비디오 업로드 시작", 0);
//                uploadThumbnail(photoModel, false);
                photoModel.setThumbUploading(2);
                uploadVideo(photoModel);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "비디오 인텐트서비스 종료");
    }

    private void uploadVideo(final PhotoModel pm) {
        final String filePath = pm.getFullpath();
        final Long photoModelId = pm.getId();
        Log.i(TAG, "filePath>>>"+filePath);
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {

                File file  = new File(filePath);
                byte[] bytes = null;
                try{
                    FileInputStream fis = new FileInputStream(file);
                    int nCount = fis.available();
                    if(nCount > 0){
                        bytes = new byte[nCount];
                        fis.read(bytes);
                    }
                    if(fis != null){
                        fis.close();
                    }
                }catch(Exception e){
                    Log.i(TAG,e.toString());
                }
                Log.i(TAG,"upload Video => Read MP4");

                MadamfiveAPI.createPost(bytes, "Video", photoModelId, new JsonHttpResponseHandler() {
                    @Override
                    public void onStart() {
                        Log.i("AsyncTask", "Uploading");
                    }
                    @Override
                    public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                        Log.d("AsyncTask", "HTTP21:" + statusCode + responseString);
                        makeNoti("비디오 업로드 성공",0);
//                        PhotoModelService.deleteFileNPhotoModel(pm);
                    }
                    @Override
                    public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
                        Log.d("AsyncTask", "HTTP22:" + statusCode + response.toString());
                        makeNoti("비디오 업로드 성공",0);
//                        PhotoModelService.deleteFileNPhotoModel(pm);
                    }
                });
//                Log.i(TAG,"uploadImage => Finished");
            }
        });
        t1.start();
    }

    private void makeNoti(final String message, int id) {

        NotificationCompat.Builder builder;
        String CHANNEL_ID = "video_upload_channel";

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            CharSequence name = "Video Upload";
            //String description = Constants.VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION;
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel =
                    new NotificationChannel(CHANNEL_ID, name, importance);
            //channel.setDescription(description);
            // Add the channel
            NotificationManager notificationManager =
                    (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }else{
            Activity activity = (Activity) mCon;
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mCon, message, Toast.LENGTH_SHORT).show();
                }
            });
        }

        if(id == 1){
            Intent intent= new Intent(this, FileExploreActivity.class);
            PendingIntent pending= PendingIntent.getActivity(mCon, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            // Create the notification
            builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                    .setSmallIcon(R.drawable.dslr_keeper_icon)
                    .setContentTitle(Constants.Notification.NOTIFICATION_TITLE)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pending)
                    .setAutoCancel(true)
                    .setWhen(System.currentTimeMillis());
        } else{
            // Create the notification
            builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                    .setSmallIcon(R.drawable.dslr_keeper_icon)
                    .setContentTitle(Constants.Notification.NOTIFICATION_TITLE)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setWhen(System.currentTimeMillis());
        }
        // Show the notification
        NotificationManagerCompat.from(getApplicationContext()).notify(Constants.Notification.NOTIFICATION_VIDEO_ID, builder.build());
    }


}

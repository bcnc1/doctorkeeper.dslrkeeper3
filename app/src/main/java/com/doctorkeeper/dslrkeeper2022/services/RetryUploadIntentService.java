package com.doctorkeeper.dslrkeeper2022.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.util.Log;

import com.doctorkeeper.dslrkeeper2022.Constants;
import com.doctorkeeper.dslrkeeper2022.models.PhotoModel;
import com.doctorkeeper.dslrkeeper2022.util.DisplayUtil;
import com.doctorkeeper.dslrkeeper2022.util.SmartFiPreference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class RetryUploadIntentService extends IntentService {

    private static final String TAG = "RetryUpload";
    private static final String EXTRA_RETRY_UPLOAD_ID = "com.thinoo.drcamlink.services.extra.retry.upload.id";

    private static String mAcccessToken = null;
    private static String mPatientId = null;
    private static String mHospitalId = null;
    private static String mDate =  null;
    private static String mMediaType = null;
    private static Context mCon;

    //private static int mNotiId = Constants.Notification.NOTIFICATION_RETRY_ID;
    private Messenger mMessenger = null;

    public RetryUploadIntentService() {
        super("RetryUploadIntentService");
    }

    public static void startRetryUpload(Context context, long id, Parcelable value) {
        mCon = context;
        Intent intent = new Intent(context, RetryUploadIntentService.class);
        intent.putExtra(EXTRA_RETRY_UPLOAD_ID, id);
        intent.putExtra(Constants.MESSENGER_RETRY, value);
        mCon.startService(intent);
        Log.w(TAG,"startRetryUpload 호출");
    }


    @Override
    protected void onHandleIntent(Intent intent) {

        mAcccessToken = SmartFiPreference.getSfToken(getApplicationContext());
        mHospitalId = SmartFiPreference.getHospitalId(getApplicationContext());


        if (intent != null) {
            Bundle extras = intent.getExtras();
            if(extras != null){
                long id = extras.getLong(EXTRA_RETRY_UPLOAD_ID);
                mMessenger = (Messenger) extras.get(Constants.MESSENGER_RETRY);
                Log.d(TAG,"id = "+id);
                PhotoModel photoModel = PhotoModelService.getPhotoModel(id);

                //makeNoti("Retry uploading...", 1);

                mPatientId = photoModel.getCustNo();
                Log.w(TAG, "mPatientId = "+mPatientId);
                uploadThumbnail(photoModel);


            }

        }
    }

    private void uploadThumbnail(final PhotoModel pm) {


        final String filePath = pm.getThumbpath();

        if(pm.getMode() == 0 || pm.getMode() == 1){
            mMediaType = "pictures";
        }else if(pm.getMode() == 2){
            mMediaType = "videos";
        }

        final String fileName = pm.getFilename();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                pm.setThumbUploading(1);

                File f  = new File(filePath);
                String content_type  = DisplayUtil.getMimeType(filePath);

                OkHttpClient client = new OkHttpClient();
                RequestBody file_body = RequestBody.create(MediaType.parse(content_type),f);

                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url( getAbsoluteUrl(mHospitalId+"$"+ mPatientId+
                                "/thumbnail"+"/"+mHospitalId+"/"+ mPatientId + "/pictures/"+ mDate+"/"+ fileName))
                        .put(file_body)
                        .addHeader("X-Auth-Token",mAcccessToken)
                        .build();


                try {
                    okhttp3.Response response = client.newCall(request).execute();


                    if(!response.isSuccessful()){
                        // throw new IOException("Error : "+response);
                        Log.w(TAG, " 썸네일, response = "+response.code());

                        pm.setThumbUploading(3);

                        //makeNoti("uploading fail", 0);

                        Log.w(TAG," mMessenger =  "+mMessenger);
                        if(mMessenger != null){
                            Message msg = Message.obtain();
                            msg.obj = Constants.Upload.FILE_UPLOAD_FAIL;

                            try {
                                mMessenger.send(msg);
                            } catch (android.os.RemoteException e1) {
                                Log.w(getClass().getName(), "Exception sending message", e1);
                            }

                        }

                    }else{

                        Log.d(TAG, " 썸네일, 업로드 성공 ");
                        pm.setThumbUploading(2);

                        uploadFile(pm);

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    pm.setThumbUploading(3); //업로드실패
                   // makeNoti("uploading fail. please check network", 0);

                    Log.w(TAG," mMessenger =  "+mMessenger);
                    if(mMessenger != null){
                        Message msg = Message.obtain();
                        msg.obj = Constants.Upload.FILE_UPLOAD_FAIL;

                        try {
                            mMessenger.send(msg);
                        } catch (android.os.RemoteException e1) {
                            Log.w(getClass().getName(), "Exception sending message", e1);
                        }

                    }
                }
            }
        });

        t.start();
    }


    private void uploadFile(final PhotoModel pm) {
        final String filePath = pm.getFullpath();

        if(pm.getMode() == 0 || pm.getMode() == 1){
            mMediaType = "pictures";
        }else if(pm.getMode() == 2){
            mMediaType = "videos";
        }

        final String fileName = pm.getFilename();

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {

                pm.setThumbUploading(1);

                File f  = new File(filePath);
                String content_type  = DisplayUtil.getMimeType(filePath);

                OkHttpClient client = new OkHttpClient();
                RequestBody file_body = RequestBody.create(MediaType.parse(content_type),f);

                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url( getAbsoluteUrl(mHospitalId+"$"+ mPatientId+
                                "/"+mHospitalId+"/"+ mPatientId + "/"+mMediaType+"/"+ mDate+"/"+ fileName))
                        .put(file_body)
                        .addHeader("X-Auth-Token",mAcccessToken)
                        .build();


                try {
                    okhttp3.Response response = client.newCall(request).execute();


                    if(!response.isSuccessful()){
                        Log.d(TAG," 원본, response code = "+response.code());

                        pm.setUploading(3);//업로드실패
                       // makeNoti("uploading fail",0);

                        Log.w(TAG," mMessenger =  "+mMessenger);
                        if(mMessenger != null){
                            Message msg = Message.obtain();
                            msg.obj = Constants.Upload.FILE_UPLOAD_FAIL;

                            try {
                                mMessenger.send(msg);
                            } catch (android.os.RemoteException e1) {
                                Log.w(getClass().getName(), "Exception sending message", e1);
                            }

                        }

                    }else{
                        Log.d(TAG," 원본 업로드 성공 ");
                        pm.setUploading(2);
                        //uploadChain(pm);
                        regEMR(pm);

                    }


                } catch (IOException e) {
                    e.printStackTrace();
                    pm.setUploading(3); //업로드실패
                   // makeNoti("uploading fail",0);

                    Log.w(TAG," mMessenger =  "+mMessenger);
                    if(mMessenger != null){
                        Message msg = Message.obtain();
                        msg.obj = Constants.Upload.FILE_UPLOAD_FAIL;

                        try {
                            mMessenger.send(msg);
                        } catch (android.os.RemoteException e1) {
                            Log.w(getClass().getName(), "Exception sending message", e1);
                        }

                    }
                }
            }
        });

        t1.start();
    }


    private void regEMR(final PhotoModel pm){
        Log.w(TAG,"regEMR");
        final String fileName = pm.getFilename();
        final long fileSize = pm.getFilesize();
        pm.setChainUploading(1);

        Thread t2 = new Thread(new Runnable() {

            String url = Constants.EMRAPI.BASE_URL + Constants.EMRAPI.REG_PHOTO;
            String filepath = "/"+mHospitalId+"/"+ mPatientId + "/"+ mMediaType+"/"+ mDate+"/"+ fileName;

            @Override
            public void run() {

                JSONObject jsonObject = new JSONObject();
                JSONObject data = new JSONObject();
                JSONArray req_arry = new JSONArray();

                try {
                    jsonObject.put("userId", SmartFiPreference.getDoctorId(getApplicationContext()));
                    jsonObject.put("custNo", SmartFiPreference.getSfPatientCustNo(getApplicationContext()));
                    data.put("phtoFileNm", fileName);
                    data.put("phtoFilePath", filepath);
                    data.put("imgSize",fileSize);
                    req_arry.put(data);
                    jsonObject.put("photoList", req_arry);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                OkHttpClient client = new OkHttpClient();


                RequestBody reqBody = RequestBody.create(
                        MediaType.parse("application/json; charset=utf-8"),
                        jsonObject.toString()
                );

                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(url)
                        .addHeader("Accept","application/json")
                        .addHeader("X-Auth-Token",mAcccessToken)
                        .addHeader("Content-Type", "application/json")
                        .post(reqBody)
                        .build();

                try {
                    okhttp3.Response response = client.newCall(request).execute();


                    if(!response.isSuccessful()){
                        Log.w(TAG," regEMR 싪패 , response code = "+response.code());

                        pm.setChainUploading(3);//업로드실패
                        //makeNoti("uploading fail",0);

                        if(mMessenger != null){
                            Message msg = Message.obtain();
                            msg.obj = Constants.Upload.FILE_UPLOAD_FAIL;

                            try {
                                mMessenger.send(msg);
                            } catch (android.os.RemoteException e1) {
                                Log.w(getClass().getName(), "Exception sending message", e1);
                            }

                        }
                    }else{
                        Log.w(TAG," regEMR 성공 ");


                        //makeNoti("uploading success",0);
                        if(Constants.FILE_N_DB_DELETE){
                            PhotoModelService.deleteFileNPhotoModel(pm);
                        }else{
                            pm.setChainUploading(2);
                        }

                        Log.w(TAG," mMessenger =  "+mMessenger);
                        if(mMessenger != null){
                            Message msg = Message.obtain();
                            msg.obj = Constants.Upload.FILE_UPLOAD_SUCCESS;

                            try {
                                mMessenger.send(msg);
                            } catch (android.os.RemoteException e1) {
                                Log.w(getClass().getName(), "Exception sending message", e1);
                            }

                        }
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                    pm.setUploading(3); //업로드실패
                    //makeNoti("uploading fail",0);

                    if(mMessenger != null){
                        Message msg = Message.obtain();
                        msg.obj = Constants.Upload.FILE_UPLOAD_FAIL;

                        try {
                            mMessenger.send(msg);
                        } catch (android.os.RemoteException e1) {
                            Log.w(getClass().getName(), "Exception sending message", e1);
                        }

                    }
                }

            }
        });

        t2.start();
    }
    private static String getAbsoluteUrl(String relativeUrl) {

        return Constants.Storage.BASE_URL + "/" + relativeUrl;
    }
}

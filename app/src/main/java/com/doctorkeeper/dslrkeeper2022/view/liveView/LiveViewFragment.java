/**
 * Copyright 2013 Nils Assbeck, Guersel Ayaz and Michael Zoech
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.doctorkeeper.dslrkeeper2022.view.liveView;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.doctorkeeper.dslrkeeper2022.PictureView;
import com.doctorkeeper.dslrkeeper2022.R;
import com.doctorkeeper.dslrkeeper2022.madamfive.MadamfiveAPI;
import com.doctorkeeper.dslrkeeper2022.models.PhotoModel;
import com.doctorkeeper.dslrkeeper2022.ptp.Camera;
import com.doctorkeeper.dslrkeeper2022.ptp.PtpConstants;
import com.doctorkeeper.dslrkeeper2022.ptp.model.LiveViewData;
import com.doctorkeeper.dslrkeeper2022.ptp.model.ObjectInfo;
import com.doctorkeeper.dslrkeeper2022.services.PhotoModelService;
import com.doctorkeeper.dslrkeeper2022.view.SessionActivity;
import com.doctorkeeper.dslrkeeper2022.view.SessionFragment;
import com.doctorkeeper.dslrkeeper2022.view.dslr.DSLRPhotoAdapter;
import com.doctorkeeper.dslrkeeper2022.view.phone_camera.PhoneCameraFragment;
import com.doctorkeeper.dslrkeeper2022.view.sdcard.StorageAdapter;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LiveViewFragment extends SessionFragment implements
        Camera.RetrieveImageListener,
        Camera.RetrieveImageInfoListener {

    private final Handler handler = new Handler();

    private DSLRPhotoAdapter galleryAdapter;
    private ArrayList<PhotoModel> photoList;
    private SimpleDateFormat formatParser;

    private Spinner storageSpinner;
    private StorageAdapter storageAdapter;
    private TextView emptyView;

    @BindView(R.id.liveview_btn_usb_linked)
    ImageView connectedImageView;

    @BindView(R.id.liveview_description_notice)
    TextView dslrTextView;

    @BindView(R.id.liveview_btn_back)
    Button backBtn;

//    @BindView(R.id.liveview_btn_liveview)
//    Button liveViewBtn;

//    @BindView(R.id.liveview_upload_Notice)
//    TextView upload_Notice;

//    @BindView(R.id.liveview_camera_ready_Notice)
//    TextView camera_ready_Notice;

    @BindView(R.id.liveview_read_Image)
    ImageView readImage;

    private int currentScrollState;
    private int currentObjectHandle;
    private Bitmap currentBitmap;

    private final String TAG = LiveViewFragment.class.getSimpleName();

    private boolean storageRead = false;

    private Fragment displayPictureFragment;
    private Fragment galleryFragment;

//    private MyAsyncTask myAsyncTask;
    private Handler uploadHandler;
    private HandlerThread uploadHandlerThread;

    private int objectHandleNumber = 0;
//    private boolean isD5500=false;


    private Toast focusToast;
    @BindView(R.id.liveview_btn_focus)
    Button focusBtn;
    @BindView(R.id.liveview_btn_shoot)
    Button takePictureBtn;
    private PictureView liveView;
    private LiveViewData currentLiveViewData;
    private LiveViewData currentLiveViewData2;
    private Runnable liveViewRestarterRunner;
    private boolean showsCapturedPicture;
    private View pictureStreamContainer;
    private Bitmap currentCapturedBitmap;
    private boolean justCaptured;
    private final Runnable justCapturedResetRunner = new Runnable() {
        @Override
        public void run() {
            justCaptured = false;
        }
    };

    public static LiveViewFragment newInstance() {
        LiveViewFragment f = new LiveViewFragment();
        return f;
    }

    private long startTime=1*1000; // 3 MINS IDLE TIME
    private final long interval = 1 * 1000;
    private LiveViewCountDownTimer countDownTimer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        formatParser = new SimpleDateFormat("yyyyMMdd'T'HHmmss.S");
        currentScrollState = OnScrollListener.SCROLL_STATE_IDLE;

        View view = inflater.inflate(R.layout.fragment_liveview, container, false);
        ButterKnife.bind(this, view);

        ((SessionActivity) getActivity()).setSessionView(this);

        liveView = (PictureView) view.findViewById(R.id.liveView);

//        liveViewBtn.setVisibility(View.GONE);
        storageAdapter = new StorageAdapter(getActivity());
//        galleryAdapter = new DSLRPhotoAdapter(getActivity());

        enableUi(false);
//        enableUi(true);

        focusToast = Toast.makeText(getActivity(), "초점 조정 완료", Toast.LENGTH_SHORT);
        startLiveViewAgain();

        pictureStreamContainer = view.findViewById(R.id.picture_stream_container);
//        thumbnailAdapter = new ThumbnailAdapter(getActivity());
        final ListView pictureStream = (ListView) view.findViewById(R.id.picture_stream);
//        pictureStream.setOnItemClickListener(new OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                if (camera() == null) {
//                    return;
//                }
//                liveView.setPicture(null);
////                camera().retrievePicture(thumbnailAdapter.getItemHandle(position));
//            }
//        });
        pictureStream.setVisibility(View.GONE);
//        pictureStream.setAdapter(thumbnailAdapter);
//        streamToggle = (ImageView) view.findViewById(R.id.picture_stream_toggle);
//        streamToggle.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                boolean showStream = pictureStream.getVisibility() == View.GONE;
//                pictureStream.setVisibility(showStream ? View.VISIBLE : View.GONE);
//                streamToggle.setRotation(showStream ? 180f : 0f);
//            }
//        });

        liveViewRestarterRunner = new Runnable() {
            @Override
            public void run() {
                startLiveViewAgain();
            }
        };


// HandlerThread를 이용하여 업로드를 별도 thread에서 처리
        uploadHandlerThread = new HandlerThread("imageUploadThread");
        uploadHandlerThread.start();
        uploadHandler = new Handler(uploadHandlerThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                HashMap<String,Object> hashMap = (HashMap<String, Object>) msg.obj;
                String filename = hashMap.get("filename").toString();
                PhotoModel photoModel = (PhotoModel) hashMap.get("photoModel");

                uploadImage(filename);

                photoModel.setUploaded(true);
                photoModel.save();
            }
        };

        return view;
    }

    private void startLiveViewAgain() {
        showsCapturedPicture = false;
        if (currentCapturedBitmap != null) {
            liveView.setPicture(null);
            currentCapturedBitmap.recycle();
            currentCapturedBitmap = null;
        }
        if (camera() != null && camera().isLiveViewOpen()) {
            liveView.setLiveViewData(null);
            currentLiveViewData = null;
            currentLiveViewData2 = null;
            camera().getLiveViewPicture(currentLiveViewData2);
        }
    }



    @Override
    public void onStart() {
        super.onStart();
        if (camera() != null) {
            cameraStarted(camera());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
//        getSettings().setGalleryOrderReversed(orderCheckbox.isChecked());
    }

    @Override
    public void onResume() {
        super.onResume();
//            cameraStarted(camera());
//        Log.i(TAG,"DSLR fragment onResume");
//        enableUi(false);
        ((SessionActivity) getActivity()).setSessionView(this);
    }

    @Override
    public void onPause() {
        super.onPause();
//        Log.i(TAG,"DSLR fragment onPause");
//        cameraStopped(camera());
    }

    @Override
    public void enableUi(final boolean enabled) {
        //galleryView.setEnabled(enabled);
//        Log.i(TAG, "LiveView EnableUi..." + enabled);

        if (getActivity()==null)
            return;

        focusBtn.setEnabled(enabled);
        takePictureBtn.setEnabled(enabled);

        (getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (enabled) {

                    dslrTextView.setText("카메라를 연결해 주세요3");
                    connectedImageView.setImageDrawable(getResources().getDrawable(R.drawable.conn_after));
//                    camera_ready_Notice.setVisibility(View.VISIBLE);
                } else {
                    dslrTextView.setText("촬영 가능 합니다3");
                    connectedImageView.setImageDrawable(getResources().getDrawable(R.drawable.conn_before));
//                    camera_ready_Notice.setVisibility(View.GONE);
                    readImage.setVisibility(View.GONE);
                }
            }
        });

    }

    @OnClick(R.id.liveview_btn_back)
    public  void backBtnClicked(){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, PhoneCameraFragment.newInstance(), null);
        ft.addToBackStack(null);
        ft.commit();
    }

    @OnClick(R.id.liveview_btn_focus)
    public void focusBtnClick(){
        camera().focus();
//        Log.i(TAG,"focusBtnClick");
    }

    @OnClick(R.id.liveview_btn_shoot)
    public void shootBtnClick(){
        camera().capture();
        justCaptured = true;
        handler.postDelayed(justCapturedResetRunner, 500);
//        Log.i(TAG,"shootBtnClick");
    }


    @Override
    public void cameraStarted(Camera camera) {
        enableUi(true);
        //camera.retrieveStorages(this);
        //emptyView.setText(getString(R.string.gallery_loading));
//        Log.i(TAG, "Loading..."+camera.getDeviceInfo());
        camera().setLiveView(true);
//        if (camera.isLiveViewSupported()) {
//            liveViewToggle.setEnabled(camera.isLiveViewSupported());
//        }
        Log.i(TAG,"=======>>>>>>cameraStarted");

        Log.i(TAG,"=======>>>>>>isLiveViewOpen ::: "+camera.isLiveViewOpen()+"");

        if (camera.isLiveViewOpen()) {
            liveViewStarted();
            Log.i(TAG,"=======>>>>>>liveViewStarted111");
        }

//        else if (camera.isSettingPropertyPossible(Camera.Property.FocusPoints)) {
//            focusPointsToggle.setEnabled(true);
//        }

    }



    @Override
    public void cameraStopped(Camera camera) {
        enableUi(false);
        camera().setLiveView(false);
//        galleryAdapter.setItems(null);
    }

    @Override
    public void propertyChanged(int property, int value) {
        Log.i(TAG, "propertyChanged " + property + ":" + value);
        if (property == 7) {
            //camera().retrieveStorages(this);
        }
    }

    @Override
    public void propertyDescChanged(int property, int[] values) {
    }

    @Override
    public void setCaptureBtnText(String text) {
    }

    @Override
    public void focusStarted() {
        focusToast.cancel();
        focusBtn.setEnabled(false);
        takePictureBtn.setEnabled(false);
    }

    @Override
    public void focusEnded(boolean hasFocused) {
        if (hasFocused) {
            focusToast.show();
        }
        focusBtn.setEnabled(true);
        takePictureBtn.setEnabled(true);
    }

    @Override
    public void liveViewStarted() {

        Log.i(TAG,"=======>>>>>>liveViewStarted222");

        if (!inStart || camera() == null) {
            return;
        }
//        liveViewToggle.setChecked(true);
//        if (camera().isDriveLensSupported()) {
//            driveLensToggle.setEnabled(true);
//        }
//        if (camera().isHistogramSupported()) {
//            histogramToggle.setEnabled(true);
//        }
//        focusPointsToggle.setEnabled(false);
        liveView.setLiveViewData(null);
        showsCapturedPicture = false;
        currentLiveViewData = null;
        currentLiveViewData2 = null;
        camera().getLiveViewPicture(null);
    }

    @Override
    public void liveViewStopped() {
        if (!inStart || camera() == null) {
            return;
        }
//        liveViewToggle.setChecked(false);
//        histogramToggle.setEnabled(false);
//        driveLensToggle.setEnabled(false);
//        focusPointsToggle.setEnabled(camera().isSettingPropertyPossible(Camera.Property.FocusPoints));
    }

    @Override
    public void liveViewData(LiveViewData data) {
        if (!inStart || camera() == null) {
            return;
        }
//        if (justCaptured || showsCapturedPicture || !liveViewToggle.isChecked()) {
        if (justCaptured || showsCapturedPicture ) {
            return;
        }
        if (data == null) {
            camera().getLiveViewPicture(null);
            return;
        }

//        data.hasHistogram &= histogramToggle.isChecked();

        liveView.setLiveViewData(data);
        currentLiveViewData2 = currentLiveViewData;
        this.currentLiveViewData = data;
        camera().getLiveViewPicture(currentLiveViewData2);
        //        ++fps;
        //        if (last + 1000 < System.currentTimeMillis()) {
        //            Log.i(TAG, "fps " + fps);
        //            last = System.currentTimeMillis();
        //            fps = 0;
        //        }

    }

    @Override
    public void capturedPictureReceived(int objectHandle, String filename, Bitmap thumbnail, Bitmap bitmap) {
        Log.i(TAG, "BITMAP:capturedPictureReceived:" + bitmap.getWidth() + "x" + bitmap.getHeight());

        if (!inStart) {
            bitmap.recycle();
            return;
        }
        showsCapturedPicture = true;
//        if (isPro && liveViewToggle.isChecked()) {
//            if (!showCapturedPictureDurationManual && !showCapturedPictureNever) {
//                handler.postDelayed(liveViewRestarterRunner, showCapturedPictureDuration);
//            } else {
//                btnLiveview.setVisibility(View.VISIBLE);
//            }
//        }
//        thumbnailAdapter.addFront(objectHandle, filename, thumbnail);
        liveView.setPicture(bitmap);
        Toast.makeText(getActivity(), filename, Toast.LENGTH_SHORT).show();
        if (currentCapturedBitmap != null) {
            currentCapturedBitmap.recycle();
        }
        currentCapturedBitmap = bitmap;
        if (bitmap == null) {
            Toast.makeText(getActivity(), "Error decoding picture. Try to reduce picture size in settings!",
                    Toast.LENGTH_LONG).show();
        }


    }

    @Override
    public void objectAdded(int handle, int format) {
        Log.i(TAG, "OBJECT:Added:" + handle + ":" + format);

        if (camera() != null) {
            if (format == PtpConstants.ObjectFormat.EXIF_JPEG) {
                Log.i(TAG, "OBJECT:retrieveImage:");
                camera().retrieveImage(this, handle);
            }
        }
        if (camera() == null) {
            return;
        }

        if (format == PtpConstants.ObjectFormat.EXIF_JPEG) {
            camera().retrieveImageInfo(this, handle);
        }

    }

    @Override
    public void onImageInfoRetrieved(final int objectHandle, final ObjectInfo objectInfo, final Bitmap thumbnail) {

        Log.d(TAG,"onImageInfoRetrieved");

        handler.post(new Runnable() {
            @Override
            public void run() {
                Camera camera = camera();
                if (!inStart || camera == null) {
                    return;
                }

                if (currentObjectHandle == objectHandle) {

                    Log.i(TAG, "1:onImageInfoRetrieved ###### [" + objectHandle + "] " + objectInfo.filename + "#####");

                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    if(displayPictureFragment!=null){
                        ft.remove(displayPictureFragment).commit();
                    }
                    if(galleryFragment != null){
                        ft.remove(galleryFragment).commit();
                        storageRead = false;
                    }
                    if(readImage.isEnabled())  readImage.setVisibility(View.GONE);

                    sendPhoto(currentObjectHandle, objectInfo, thumbnail, currentBitmap);
                    displayPhoto(objectHandle,currentBitmap);

                }

            }
        });

    }

    private void getImage(int objectHandle){
        camera().retrieveImage(this, objectHandle);
    }

//    @Override
//    public void onScrollStateChanged(AbsListView view, int scrollState) {
//        currentScrollState = scrollState;
//        switch (scrollState) {
//            case OnScrollListener.SCROLL_STATE_IDLE: {
//                Camera camera = camera();
//                if (!inStart || camera == null) {
//                    break;
//                }
//                break;
//            }
//        }
//    }
//
//    @Override
//    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//    }
//
//    @Override
//    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//    }

    /**
     * Camera.RetrieveImageListener
     *
     * @param objectHandle
     * @param image
     */
    @Override
    public void onImageRetrieved(int objectHandle, Bitmap image) {

        Camera camera = camera();
        if (camera == null) {
            return;
        }

        currentObjectHandle = objectHandle;
        currentBitmap = image;

        camera.retrieveImageInfo(this, objectHandle);

    }

    private void sendPhoto(int objectHandle, ObjectInfo info, Bitmap thumb, Bitmap bitmap) {

        camera().setLiveView(false);

        currentObjectHandle = 0;
        Log.i("sendPhoto","Started");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        }catch(Exception e){

            // if error due to memory problem, scale down bitmap image  ============================
            // Scaled Down Bitmap   /////////////////
            Log.d(TAG,e.toString());
            int nh = (int) ( bitmap.getHeight() * (3072.0 / bitmap.getWidth()) );
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 3072, nh, true);
            scaled.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        }
        Log.i("sendPhoto","COMPRESSED");
        final byte[] bytes = baos.toByteArray();

        final PhotoModel photoModel = PhotoModelService.savePhoto(bytes, info.filename, 1);

        Log.i("sendPhoto","SAVED");
//        photoList.add(0, photoModel);
//        galleryAdapter.notifyDataSetChanged();

        HashMap<String,Object> taskInfo = new HashMap<>();
        taskInfo.put("filename",info.filename);
        taskInfo.put("photoModel",photoModel);
//        taskInfo.put("bitmap",bitmap);
        Message msg = uploadHandler.obtainMessage();
        msg.obj = taskInfo;
        uploadHandler.sendMessage(msg);
        Log.i("sendPhoto","Finished");
//        myAsyncTask = new MyAsyncTask();
//        myAsyncTask.execute(info.filename);

    }

    private void uploadImage(String filename){
        Log.i(TAG,"uploadImage => Started");
//        String imagePath = Environment.getExternalStorageDirectory() + "/drcam/" + filename;

        File file = new File(getActivity().getExternalFilesDir(Environment.getExternalStorageState()), "/drcam/");
//        Bitmap bitmap = null;
//        File f = new File(imagePath);
        byte[] bytes = null;
        try{
            FileInputStream fis = new FileInputStream(file.getAbsolutePath()+filename);
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
        Log.i(TAG,"uploadImage =>  Read Bitmap");

        MadamfiveAPI.createPost(bytes, "DSLR", 0L, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                Log.i("AsyncTask", "Uploading");
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                Log.d("AsyncTask", "HTTP21:" + statusCode + responseString);
//                        photoModel.setUploaded(true);
//                        photoModel.save();
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.d("AsyncTask", "HTTP22:" + statusCode + response.toString());
            }
        });
        Log.i(TAG,"uploadImage => Finished");
    }

    private void displayPhoto(int objectHandle,Bitmap currentBitmap){

//        upload_Notice.setVisibility(View.GONE);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        Bitmap resized = Bitmap.createScaledBitmap(currentBitmap, 300, 200, true);

        readImage.setImageBitmap(resized);
        readImage.setEnabled(true);
        readImage.setVisibility(View.VISIBLE);

        countDownTimer = new LiveViewCountDownTimer(startTime, interval);
        countDownTimer.start();

    }

    private class LiveViewCountDownTimer extends CountDownTimer {
        public LiveViewCountDownTimer(long startTime, long interval) {
            super(startTime, interval);
        }

        @Override
        public void onFinish() {
//            Log.i(TAG,"Timer Completed");
            readImage.setVisibility(View.INVISIBLE);
            camera().setLiveView(true);
        }

        @Override
        public void onTick(long millisUntilFinished) {
        }
    }

}

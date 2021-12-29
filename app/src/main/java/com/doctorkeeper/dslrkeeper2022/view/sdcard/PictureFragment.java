/**
 * Copyright 2013 Nils Assbeck, Guersel Ayaz and Michael Zoech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.doctorkeeper.dslrkeeper2022.view.sdcard;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.doctorkeeper.dslrkeeper2022.GestureDetector;
import com.doctorkeeper.dslrkeeper2022.GestureDetector.GestureHandler;
import com.doctorkeeper.dslrkeeper2022.PictureView;
import com.doctorkeeper.dslrkeeper2022.R;
import com.doctorkeeper.dslrkeeper2022.madamfive.MadamfiveAPI;
import com.doctorkeeper.dslrkeeper2022.models.PhotoModel;
import com.doctorkeeper.dslrkeeper2022.ptp.Camera;
import com.doctorkeeper.dslrkeeper2022.ptp.model.LiveViewData;
import com.doctorkeeper.dslrkeeper2022.services.PhotoModelService;
import com.doctorkeeper.dslrkeeper2022.view.SessionFragment;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

public class PictureFragment extends SessionFragment
        implements
        Camera.RetrieveImageListener,
        GestureHandler {

    private final String TAG = PictureFragment.class.getSimpleName();

    public static PictureFragment newInstance(int objectHandle, String filename) {
        Bundle args = new Bundle();
        args.putInt("handle", objectHandle);
        args.putString("filename", filename);
        PictureFragment f = new PictureFragment();
        f.setArguments(args);
        return f;
    }

    private Handler handler;
    private PictureView pictureView;
    private int objectHandle;
    private Bitmap picture;
    private GestureDetector gestureDetector;
    private ProgressBar progressBar;

    private Button btnUpload;
    private Button btnBack;
    private int currentObjectHandle;
    private Bitmap currentImage;
    private String filename;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.i(TAG,"Loading... =========>");

        handler = new Handler();
        objectHandle = getArguments().getInt("handle");
        filename = getArguments().getString("filename");

        View view = inflater.inflate(R.layout.picture_frag, container, false);
        pictureView = (PictureView) view.findViewById(R.id.image1);
        progressBar = (ProgressBar) view.findViewById(R.id.progress);

        btnUpload = (Button) view.findViewById(R.id.btn_upload);
        btnBack = (Button) view.findViewById(R.id.btn_back);

        gestureDetector = new GestureDetector(getActivity(), this);
        pictureView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouch(event);
                return true;
            }
        });

        enableUi(true);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
//        getActivity().getActionBar().hide();
        if (camera() == null) {

        } else if (picture == null) {
            camera().retrieveImage(this, objectHandle);
//            camera().retrieveImageInfo(this,objectHandle);
            Log.i(TAG,"Start Image DATA");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isRemoving()) {
//            getActivity().getActionBar().show();
        }
    }

    @Override
    public void enableUi(boolean enabled) {

        btnBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                getActivity().getFragmentManager().popBackStack();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
            btnUpload.setText("WAIT");
            sendPhoto(filename,currentImage);
            }
        });

    }

    @Override
    public void cameraStarted(Camera camera) {
    }

    @Override
    public void cameraStopped(Camera camera) {
    }

    @Override
    public void propertyChanged(int property, int value) {
    }

    @Override
    public void propertyDescChanged(int property, int[] values) {
    }

    @Override
    public void setCaptureBtnText(String text) {
    }

    @Override
    public void focusStarted() {
    }

    @Override
    public void focusEnded(boolean hasFocused) {
    }

    @Override
    public void liveViewStarted() {
    }

    @Override
    public void liveViewStopped() {
    }

    @Override
    public void liveViewData(LiveViewData data) {
    }

    @Override
    public void capturedPictureReceived(int objectHandle, String filename, Bitmap thumbnail, Bitmap bitmap) {
    }

    @Override
    public void objectAdded(int handle, int format) {
    }

    @Override
    public void onImageRetrieved(final int objectHandle, final Bitmap image) {

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (image == null) {
                    if (inStart) {
                        Toast.makeText(getActivity(), getString(R.string.error_loading_image), Toast.LENGTH_LONG).show();
                    }
                    if (isAdded()) {
                        getFragmentManager().popBackStack();
                    }
                } else {
                    //MadamfiveAPI.createPost(image, "Picture");
                    Log.i(TAG, "PictureFragment " + image.getWidth() + "x" + image.getHeight());
                    progressBar.setVisibility(View.GONE);
                    pictureView.setPicture(image);
                    currentImage = image;
//                    uploadNotice.setVisibility(View.VISIBLE);

                }
            }
        });

        Log.i(TAG,"END Image DATA");
    }

    @Override
    public void onLongTouch(float posx, float posy) {

    }

    @Override
    public void onPinchZoom(float pX, float pY, float distInPixel) {
        pictureView.zoomAt(pX, pY, distInPixel);
    }

    @Override
    public void onTouchMove(float dx, float dy) {
        pictureView.pan(dx, dy);
    }

    @Override
    public void onFling(float velx, float vely) {
        pictureView.fling(velx, vely);
    }

    @Override
    public void onStopFling() {
        pictureView.stopFling();
    }

    private void sendPhoto(String filename, Bitmap bitmap) {

        currentObjectHandle = 0;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        }catch(Exception e){
// if error due to memory problem, scale down bitmap image
// Scaled Down Bitmap   /////////////////
            Log.d(TAG,e.toString());
            int nh = (int) ( bitmap.getHeight() * (3072.0 / bitmap.getWidth()) );
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 3072, nh, true);
            scaled.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        }

        final byte[] bytes = baos.toByteArray();

        final PhotoModel photoModel = PhotoModelService.savePhoto(bytes, filename, 1);

        MadamfiveAPI.createPost(bytes, "DSLR", 0L, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                Log.i("CAMERA", "onStart2:");
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                Log.d("CAMERA", "HTTP21:" + statusCode + responseString);
                photoModel.setUploaded(true);
                photoModel.save();
//                galleryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.d("CAMERA", "HTTP22:" + statusCode + response.toString());
//                galleryAdapter.notifyDataSetChanged();
            }
        });

        btnUpload.setText("DONE");
        getActivity().getFragmentManager().popBackStack();

    }

}

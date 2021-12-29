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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.doctorkeeper.dslrkeeper2022.R;
import com.doctorkeeper.dslrkeeper2022.madamfive.MadamfiveAPI;
import com.doctorkeeper.dslrkeeper2022.models.PhotoModel;
import com.doctorkeeper.dslrkeeper2022.ptp.Camera;
import com.doctorkeeper.dslrkeeper2022.ptp.model.LiveViewData;
import com.doctorkeeper.dslrkeeper2022.ptp.model.ObjectInfo;
import com.doctorkeeper.dslrkeeper2022.services.PhotoModelService;
import com.doctorkeeper.dslrkeeper2022.view.SessionFragment;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Iterator;

public class SDcardPictureFragment extends SessionFragment
        implements
        Camera.RetrieveImageListener,
        Camera.RetrieveImageInfoListener
//        GestureHandler
{

    private final String TAG = SDcardPictureFragment.class.getSimpleName();

    public static SDcardPictureFragment newInstance(int objectHandle, String filename, byte[] bytes) {
        Bundle args = new Bundle();
        args.putInt("handle", objectHandle);
        args.putString("filename", filename);
        args.putByteArray("imageData",bytes);
        SDcardPictureFragment f = new SDcardPictureFragment();
        f.setArguments(args);
        return f;
    }

    private Handler handler;
    private ImageView pictureView;
    private int thisObjectHandle;
    private Bitmap picture;
    private ProgressBar progressBar;

    private Button btnUpload;
    private Button btnBack;
    private int currentObjectHandle;
    private Bitmap currentImage;
    private String filename;
    private Bitmap thumbnailImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.i(TAG,"Loading... =========>");

        handler = new Handler();
        currentObjectHandle = getArguments().getInt("handle");
        filename = getArguments().getString("filename");
        byte[] byteArray = getArguments().getByteArray("imageData");
        thumbnailImage = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        View view = inflater.inflate(R.layout.sdcard_picture_frag, container, false);
        pictureView = (ImageView) view.findViewById(R.id.sdcard_image1);
        progressBar = (ProgressBar) view.findViewById(R.id.sdcard_progress);

        btnUpload = (Button) view.findViewById(R.id.sdcard_btn_upload);
        btnBack = (Button) view.findViewById(R.id.sdcard_btn_back);

        btnUpload.setVisibility(View.INVISIBLE);
        btnBack.setVisibility(View.INVISIBLE);
        enableUi(true);

//        getDetail();
//        getImage();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
//        getActivity().getActionBar().hide();
        if (camera() == null) {

        } else if (picture == null) {
//            camera().retrieveImage(this, objectHandle);
//            camera().retrieveImageInfo(this,currentObjectHandle);
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

        progressBar.setVisibility(View.VISIBLE);

///
///     === Using static Hashmap including bitmap
///     start
        progressBar.setVisibility(View.GONE);

//        Bitmap image = (Bitmap) GalleryFragment.pictureMap.get(currentObjectHandle+"_data");
        Bitmap resized = Bitmap.createScaledBitmap(thumbnailImage,(int)(thumbnailImage.getWidth()*7), (int)(thumbnailImage.getHeight()*7), true);
        pictureView.setImageBitmap(resized);

        btnUpload.setVisibility(View.VISIBLE);
        btnBack.setVisibility(View.VISIBLE);
///     end

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
            progressBar.setVisibility(View.VISIBLE);
            getImage();
//            sendPhoto(filename,currentImage);
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

                    sendPhoto(filename,image);
//                    progressBar.setVisibility(View.GONE);
//                    Log.i(TAG, "SDcardPictureFragment " + image.getWidth() + "x" + image.getHeight());
//
//                    pictureView.setImageBitmap(image);
//                    currentImage = image;
//
//                    btnUpload.setVisibility(View.VISIBLE);
//                    btnBack.setVisibility(View.VISIBLE);
                }
            }
        });

        Log.i(TAG,"END Image DATA");
    }

    @Override
    public void onImageInfoRetrieved(final int objectHandle, final ObjectInfo objectInfo, final Bitmap thumbnail) {
        Log.d(TAG,"onImageInfoRetrieved");

        handler.post(new Runnable() {
            @Override
            public void run() {

                final Camera camera = camera();

                Log.i(TAG,"currentObjectHandle::::>>>"+currentObjectHandle+"objectHandle:::>>>"+objectHandle);
                if(currentObjectHandle==objectHandle){

                    progressBar.setVisibility(View.GONE);
                    Bitmap resized = Bitmap.createScaledBitmap(thumbnail,(int)(thumbnail.getWidth()*4), (int)(thumbnail.getHeight()*4), true);
                    pictureView.setImageBitmap(resized);

                    btnUpload.setVisibility(View.VISIBLE);
                    btnBack.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void getDetail() {

        UsbManager manager = (UsbManager) getActivity().getSystemService(Context.USB_SERVICE);

        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();

//            manager.requestPermission(device, mPermissionIntent);
            String Model = device.getDeviceName();

            int DeviceID = device.getDeviceId();
            int Vendor = device.getVendorId();
            int Product = device.getProductId();
            int Class = device.getDeviceClass();
            int Subclass = device.getDeviceSubclass();

            Log.i(TAG,":::"+Model+":::"+DeviceID+":::"+Vendor+":::"+Product+":::"+Class+":::"+Subclass+":::");

        }
    }

    private void getImage() {

        camera().retrieveImage(this, currentObjectHandle);

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

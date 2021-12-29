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
package com.doctorkeeper.dslrkeeper2022.view.sdcard;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.doctorkeeper.dslrkeeper2022.R;
import com.doctorkeeper.dslrkeeper2022.models.PhotoModel;
import com.doctorkeeper.dslrkeeper2022.ptp.Camera;
import com.doctorkeeper.dslrkeeper2022.ptp.model.LiveViewData;
import com.doctorkeeper.dslrkeeper2022.view.dslr.DSLRFragment;
import com.doctorkeeper.dslrkeeper2022.view.SessionActivity;
import com.doctorkeeper.dslrkeeper2022.view.SessionFragment;
import com.doctorkeeper.dslrkeeper2022.view.dslr.DSLRPhotoAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SDCardFragment extends SessionFragment
//        implements
//        Camera.RetrieveImageListener,
//        Camera.WorkerListener,
//        Camera.RetrieveImageInfoListener,
//        Camera.StorageInfoListener,
//        OnScrollListener,
//        OnItemClickListener
{

    private final Handler handler = new Handler();

    private DSLRPhotoAdapter galleryAdapter;
    private ArrayList<PhotoModel> photoList;
    private SimpleDateFormat formatParser;

    private Spinner storageSpinner;
    private StorageAdapter storageAdapter;
    private TextView emptyView;

    @BindView(R.id.btn_usb_linked)
    ImageView connectedImageView;

    @BindView(R.id.dslr_description)
    TextView dslrTextView;

    @BindView(R.id.btn_back)
    Button backBtn;

    @BindView(R.id.btn_display_Storage)
    Button StorageBtn;

    @BindView(R.id.upload_Notice)
    TextView upload_Notice;

//    @BindView(R.id.camera_ready_Notice)
//    TextView camera_ready_Notice;

    @BindView(R.id.read_Image)
    ImageView readImage;

//    private int currentScrollState;
//    private int currentObjectHandle;
//    private Bitmap currentBitmap;

    private final String TAG = SDCardFragment.class.getSimpleName();

    private boolean storageRead = false;

//    private Fragment displayPictureFragment;
    private Fragment galleryFragment;

//    private MyAsyncTask myAsyncTask;
    private Handler uploadHandler;
    private HandlerThread uploadHandlerThread;

    public static SDCardFragment newInstance() {
        SDCardFragment f = new SDCardFragment();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        formatParser = new SimpleDateFormat("yyyyMMdd'T'HHmmss.S");
//        currentScrollState = OnScrollListener.SCROLL_STATE_IDLE;

        View view = inflater.inflate(R.layout.dslr_frag, container, false);
        ButterKnife.bind(this, view);
        ((SessionActivity) getActivity()).setSessionView(this);
//        galleryAdapter = new DSLRPhotoAdapter(getActivity());
        enableUi(false);
        //enableUi(true);

        return view;
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
        Log.i(TAG,"DSLR fragment onResume");
//        enableUi(false);
//        ((SessionActivity) getActivity()).setSessionView(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG,"DSLR fragment onPause");
//        cameraStopped(camera());
    }

    @Override
    public void enableUi(final boolean enabled) {
        //galleryView.setEnabled(enabled);
        Log.i(TAG, "DSLR EnableUi..." + enabled);

        if (getActivity()==null)
            return;

        (getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (enabled) {
                    dslrTextView.setText("");
//                    connectedImageView.setImageDrawable(getResources().getDrawable(R.drawable.conn_after));
                } else {
                    dslrTextView.setText("");
//                    connectedImageView.setImageDrawable(getResources().getDrawable(R.drawable.conn_before));
                }
            }
        });

    }

    @OnClick(R.id.btn_back)
    public  void backBtnClicked(){
//        getFragmentManager().popBackStackImmediate();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, DSLRFragment.newInstance(), null);
        ft.addToBackStack(null);
        ft.commit();
    }

    @OnClick(R.id.btn_display_Storage)
    public  void storageBtnClicked(){

        readImage.setVisibility(View.GONE);

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if(!storageRead) {
//            camera_ready_Notice.setVisibility(View.GONE);

            galleryFragment = GalleryFragment.newInstance();
            ft.replace(R.id.storage_container, galleryFragment, "galleryFragment");
            ft.addToBackStack(null);
            storageRead = true;
        }else{
            ft.remove(galleryFragment);
            ft.replace(R.id.fragment_container, SDCardFragment.newInstance(), null);
            ft.addToBackStack(null);
            storageRead = false;
        }
        ft.commit();

    }

    @Override
    public void cameraStarted(Camera camera) {
        enableUi(true);
        //camera.retrieveStorages(this);
        //emptyView.setText(getString(R.string.gallery_loading));
        Log.i(TAG, "Loading...");//+camera.getDeviceInfo());
    }

    @Override
    public void cameraStopped(Camera camera) {
        enableUi(false);
//        galleryAdapter.setItems(null);
    }

    @Override
    public void propertyChanged(int property, int value) {
        //Log.i(TAG, "propertyChanged " + property + ":" + value);
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
//        Log.i(TAG, "BITMAP:capturedPictureReceived:" + bitmap.getWidth() + "x" + bitmap.getHeight());
    }

    @Override
    public void objectAdded(int handle, int format) {
    }

}

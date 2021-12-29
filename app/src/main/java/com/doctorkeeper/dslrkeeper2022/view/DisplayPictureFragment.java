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
package com.doctorkeeper.dslrkeeper2022.view;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.Picasso;
import com.doctorkeeper.dslrkeeper2022.GestureDetector;
import com.doctorkeeper.dslrkeeper2022.R;
import com.doctorkeeper.dslrkeeper2022.ptp.Camera;
import com.doctorkeeper.dslrkeeper2022.ptp.model.LiveViewData;

import java.io.File;

public class DisplayPictureFragment extends SessionFragment {

    private final String TAG = DisplayPictureFragment.class.getSimpleName();

    public static DisplayPictureFragment newInstance(int objectHandle,byte[] byteArray) {
        Bundle args = new Bundle();
        args.putInt("handle", objectHandle);
        args.putByteArray("image",byteArray);
        DisplayPictureFragment f = new DisplayPictureFragment();
        f.setArguments(args);
        return f;
    }

    private Handler handler;
    private ImageView pictureView;
    private int objectHandle;
    private Bitmap picture;
    private GestureDetector gestureDetector;
    private ProgressBar progressBar;
    private byte[] byteArray;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        handler = new Handler();
        objectHandle = getArguments().getInt("handle");
        byteArray = getArguments().getByteArray("image");

        View view = inflater.inflate(R.layout.display_picture, container, false);
        pictureView = (ImageView) view.findViewById(R.id.image1);
//        progressBar = (ProgressBar) view.findViewById(R.id.progress);
//        progressBar.setVisibility(View.GONE);

//        pictureView.setOnTouchListener(new OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                gestureDetector.onTouch(event);
//                return true;
//            }
//        });

        enableUi(true);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void enableUi(final boolean enabled) {

        Log.i(TAG, "DSLR Storage EnableUi..." + enabled);

        if (getActivity()==null)
            return;

        (getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {

            Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            pictureView.setImageBitmap(bmp);

            File f = new File("path-to-file/file.png");
            Picasso.get().load(f).into(pictureView);

            }
        });

//        progressBar.setVisibility(View.GONE);

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

}

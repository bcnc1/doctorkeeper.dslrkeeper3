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
package com.doctorkeeper.dslrkeeper2022.view.phone_camera;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.doctorkeeper.dslrkeeper2022.R;
import com.doctorkeeper.dslrkeeper2022.madamfive.MadamfiveAPI;
import com.doctorkeeper.dslrkeeper2022.models.PhotoModel;
import com.doctorkeeper.dslrkeeper2022.services.PictureIntentService;
import com.doctorkeeper.dslrkeeper2022.services.VideoIntentService;
import com.doctorkeeper.dslrkeeper2022.view.BaseFragment;

import java.io.File;

public class PhonePictureFragment extends BaseFragment
{

    private final String TAG = PhonePictureFragment.class.getSimpleName();

    public static PhonePictureFragment newInstance(int objectHandle, String fullPath, PhotoModel model) {
        Bundle args = new Bundle();
        args.putInt("handle", objectHandle);
        args.putString("fullPath", fullPath);

        Log.i("phonePicturFragment","CustNo:"+model.getCustNo());
        Log.i("phonePicturFragment","CustName:"+model.getCustName());
        Log.i("phonePicturFragment","Id:"+model.getId().toString());

        args.putString("custNo", model.getCustNo());
        args.putString("custName", model.getCustName());
        args.putString("photoModelId", model.getId().toString());
        PhonePictureFragment f = new PhonePictureFragment();
        f.setArguments(args);
        return f;
    }

    private ProgressBar progressBar;
    private Button phoneViewPictureBtnBack;
    private Button phoneViewPictureBtnUpload;
    private TextView phoneViewPictureType;

    private String fullPath;
    private String custNo;
    private String custName;
    private String photoModelId;

    private ImageView phone_view_picture_image;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        fullPath = getArguments().getString("fullPath");
        custNo = getArguments().getString("custNo");
        custName = getArguments().getString("custName");
        photoModelId = getArguments().getString("photoModelId");

        View view = inflater.inflate(R.layout.phone_picture_frag, container, false);

        phone_view_picture_image = (ImageView) view.findViewById(R.id.phone_view_picture_image);
//        phone_view_picture_image.setVisibility(View.VISIBLE);
        phoneViewPictureType = (TextView) view.findViewById(R.id.phone_view_picture_type);
        progressBar = (ProgressBar) view.findViewById(R.id.phone_view_picture_progress);

        phoneViewPictureBtnBack = (Button) view.findViewById(R.id.phone_view_picture_btn_back);
        phoneViewPictureBtnBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                getActivity().getFragmentManager().popBackStack();
            }
        });

        phoneViewPictureBtnUpload = (Button) view.findViewById(R.id.phone_view_picture_btn_upload);
        phoneViewPictureBtnUpload.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(fullPath.contains("jpg")){
//                    PictureIntentService.startUploadPicture(MadamfiveAPI.getActivity(), Long.parseLong(photoModelId));
                }else{
                    VideoIntentService.startUploadVideo(MadamfiveAPI.getActivity(),Long.parseLong(photoModelId));
                }
                getActivity().getFragmentManager().popBackStack();
            }
        });


        if(fullPath.contains("mp4")){
            phoneViewPictureType.setText("VIDEO");
        }else{
            phoneViewPictureType.setText("IMAGE");
        }

        File f = new File(fullPath);

        Glide.with(MadamfiveAPI.getActivity()).load(f)
            .listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    return false;
                }
                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    progressBar.setVisibility(View.GONE);
                    return false;
                }
            }).centerCrop().into(phone_view_picture_image);

        return view;
    }


}

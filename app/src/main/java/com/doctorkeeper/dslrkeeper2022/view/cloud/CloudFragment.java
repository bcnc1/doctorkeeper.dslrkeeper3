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
package com.doctorkeeper.dslrkeeper2022.view.cloud;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.doctorkeeper.dslrkeeper2022.Constants;
import com.doctorkeeper.dslrkeeper2022.R;
import com.doctorkeeper.dslrkeeper2022.util.SmartFiPreference;
import com.doctorkeeper.dslrkeeper2022.view.BaseFragment;
import com.doctorkeeper.dslrkeeper2022.view.dslr.DSLRFragment;
import com.doctorkeeper.dslrkeeper2022.view.log_in.LoginDialogFragment;
import com.doctorkeeper.dslrkeeper2022.view.options.OptionsDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class CloudFragment extends BaseFragment{

    private Fragment cloudGalleryFragment;
    private final String TAG = CloudFragment.class.getSimpleName();
    public static CloudFragment newInstance() {
        CloudFragment f = new CloudFragment();
        return f;
    }

    @BindView(R.id.btn_back)
    Button backBtn;

    @BindView(R.id.btn_logout)
    Button logoutBtn;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

//        currentScrollState = OnScrollListener.SCROLL_STATE_IDLE;

        View view = inflater.inflate(R.layout.cloud_frag, container, false);
        ButterKnife.bind(this, view);
//        Log.i(TAG, "CloudFragment START");
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        cloudGalleryFragment = CloudGalleryFragment.newInstance();
        ft.replace(R.id.cloud_detail_container, cloudGalleryFragment, null);
        ft.addToBackStack(null);
        ft.commit();

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

    @OnClick(R.id.btn_back)
    public  void backBtnClicked(){
//        getFragmentManager().popBackStackImmediate();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, DSLRFragment.newInstance(), null);
        ft.addToBackStack(null);
        ft.commit();
    }

    @OnClick(R.id.btn_logout)
    public void logoutBtnClicked(){
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_DeviceDefault_Light_Dialog);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.MyDialogTheme);
        builder.setTitle("로그아웃 하시겠습니까?");
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                Log.w(TAG,"login OK click");
                SmartFiPreference.setDoctorId(getActivity(), Constants.EMRAPI.UNDEFINED);
                SmartFiPreference.setSfDoctorPw(getActivity(),Constants.EMRAPI.UNDEFINED);
                SmartFiPreference.setSfToken(getActivity(), Constants.EMRAPI.UNDEFINED);
                SmartFiPreference.setSfPatientName(getActivity(),"");

                FragmentTransaction changelogTx = getFragmentManager().beginTransaction();
                LoginDialogFragment loginDialogFragment = LoginDialogFragment.newInstance();
                changelogTx.add(loginDialogFragment, "Login");
                changelogTx.commit();
            }
        });
        builder.setNeutralButton("아니요", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                Log.w(TAG,"login No click");
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();



    }

    @OnClick(R.id.btn_setup)
    public void setupBtnClicked(){
        FragmentTransaction changelogTx = getFragmentManager().beginTransaction();
        OptionsDialogFragment opt = OptionsDialogFragment.newInstance();
        changelogTx.add(opt, "Options");
        changelogTx.commit();
    }


}

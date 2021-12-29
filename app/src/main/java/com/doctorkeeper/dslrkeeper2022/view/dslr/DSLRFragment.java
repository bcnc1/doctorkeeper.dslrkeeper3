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
package com.doctorkeeper.dslrkeeper2022.view.dslr;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.doctorkeeper.dslrkeeper2022.R;
import com.doctorkeeper.dslrkeeper2022.madamfive.BlabAPI;
import com.doctorkeeper.dslrkeeper2022.madamfive.MadamfiveAPI;
import com.doctorkeeper.dslrkeeper2022.models.PhotoModel;
import com.doctorkeeper.dslrkeeper2022.ptp.Camera;
import com.doctorkeeper.dslrkeeper2022.ptp.PtpConstants;
import com.doctorkeeper.dslrkeeper2022.ptp.model.LiveViewData;
import com.doctorkeeper.dslrkeeper2022.ptp.model.ObjectInfo;
import com.doctorkeeper.dslrkeeper2022.services.PhotoModelService;
import com.doctorkeeper.dslrkeeper2022.services.PictureIntentService;
import com.doctorkeeper.dslrkeeper2022.util.DisplayUtil;
import com.doctorkeeper.dslrkeeper2022.util.SmartFiPreference;
import com.doctorkeeper.dslrkeeper2022.view.SessionActivity;
import com.doctorkeeper.dslrkeeper2022.view.SessionFragment;
import com.doctorkeeper.dslrkeeper2022.view.cloud.CloudFragment;
import com.doctorkeeper.dslrkeeper2022.view.patient.PatientDialogFragment;
import com.doctorkeeper.dslrkeeper2022.view.doctor.DoctorDialogFragment;
import com.doctorkeeper.dslrkeeper2022.view.phone_camera.PhoneCameraPhotoAdapter;
import com.doctorkeeper.dslrkeeper2022.view.sdcard.SDCardFragment;
import com.doctorkeeper.dslrkeeper2022.view.sdcard.StorageAdapter;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.doctorkeeper.dslrkeeper2022.madamfive.BlabAPI.shootingImageDisplayExtraOption;
import static com.doctorkeeper.dslrkeeper2022.madamfive.MadamfiveAPI.selectedDoctor;

public class DSLRFragment extends SessionFragment implements
        Camera.RetrieveImageListener,
//        Camera.WorkerListener,
        Camera.RetrieveImageInfoListener
//        Camera.StorageInfoListener,
//        OnScrollListener,
//        OnItemClickListener
{

    private final Handler handler = new Handler();

    private DSLRPhotoAdapter galleryAdapter;
    private ArrayList<PhotoModel> photoList;
    private SimpleDateFormat formatParser;
    private RelativeLayout photo_container;

    private Spinner storageSpinner;
    private StorageAdapter storageAdapter;
    private TextView emptyView;

    private TextView patient_name_dslr;
    private TextView doctor_name_dslr;

    @BindView(R.id.dslr_btn_usb_linked)
    ImageView connectedImageView;

    @BindView(R.id.dslr_description_notice)
    TextView dslrTextView;

//    @BindView(R.id.dslr_btn_back)
//    Button backBtn;
    @BindView(R.id.dslr_read_Image)
    ImageView readImage;

    @BindView(R.id.btn_cloud)
    ImageButton btn_cloud;

    @BindView(R.id.btn_sdcard)
    ImageButton btn_sdcard;

    @BindView(R.id.btn_search_patient_dslr)
    ImageButton btn_search_patient_dslr;

    @BindView(R.id.dslr_upload_Notice)
    TextView upload_Notice;

    @BindView(R.id.dslr_camera_ready_Notice)
    TextView camera_ready_Notice;

    @BindView(R.id.btn_search_doctor_dslr)
    ImageButton btn_search_doctor_dslr;


    private RecyclerView listviewPhoto;
    private PhoneCameraPhotoAdapter phonePhotoAdapter;
    private int currentScrollState;
    private int currentObjectHandle;
    private Bitmap currentBitmap;

    private final String TAG = DSLRFragment.class.getSimpleName();

    private boolean storageRead = false;
    public static Boolean doctorSelectExtraOption;
    private Fragment displayPictureFragment;
    private Fragment galleryFragment;

    private Handler uploadHandler;
    private HandlerThread uploadHandlerThread;

    private int objectHandleNumber = 0;

    public static DSLRFragment newInstance() {
        DSLRFragment f = new DSLRFragment();
        return f;
    }

    private String mFileName;
    private File mFile;
    private final String  DEVICE = "dslr";

    private final BroadcastReceiver usbOnReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.w(TAG,"usbOnReciever === "+intent);
            new android.os.Handler().postDelayed(
                    () -> BlabAPI.isCameraOn = true,
                    2000);
        }
    };

    private final BroadcastReceiver usbOffReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.w(TAG,"usbOffReciever === "+intent);
            BlabAPI.isCameraOn = false;
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        formatParser = new SimpleDateFormat("yyyyMMdd'T'HHmmss.S");
        currentScrollState = OnScrollListener.SCROLL_STATE_IDLE;

        View view = inflater.inflate(R.layout.fragment_dslr, container, false);
        ButterKnife.bind(this, view);

        ((SessionActivity) getActivity()).setSessionView(this);

//        liveViewBtn.setVisibility(View.GONE);
        storageAdapter = new StorageAdapter(getActivity());
//        galleryAdapter = new DSLRPhotoAdapter(getActivity());

        patient_name_dslr = (TextView)view.findViewById(R.id.patient_name_dslr);
        Log.i(TAG,"초기이름 = "+ SmartFiPreference.getSfPatientName(MadamfiveAPI.getActivity()));

        IntentFilter on = new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        IntentFilter off = new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED);
        MadamfiveAPI.getContext().registerReceiver(usbOnReciever,on);
        MadamfiveAPI.getContext().registerReceiver(usbOffReciever,off);
        Log.i(TAG,"isCameraOn = "+ BlabAPI.isCameraOn);

        patient_name_dslr.setText(SmartFiPreference.getSfPatientName(MadamfiveAPI.getActivity()));
        // Display Doctor info : OPTION
        doctorSelectExtraOption = SmartFiPreference.getSfInsertDoctorOpt(MadamfiveAPI.getActivity());
        doctor_name_dslr = (TextView)view.findViewById(R.id.doctor_name_dslr);
        if(!doctorSelectExtraOption){
            btn_search_doctor_dslr.setVisibility(View.GONE);
            doctor_name_dslr.setVisibility(View.GONE);
        }else{
            HashMap<String,String> doctor = new HashMap<>();
            String name = SmartFiPreference.getSfDoctorName(MadamfiveAPI.getActivity());
            String number =SmartFiPreference.getSfDoctorNumber(MadamfiveAPI.getActivity());
            doctor.put("name", name);
            doctor.put("doctorNumber", number);
            selectedDoctor = doctor;
            if(selectedDoctor!=null){
                doctor_name_dslr.setText(selectedDoctor.get("name"));
            }
        }




        photoList = new ArrayList<>();
//        listviewPhoto = (RecyclerView)view.findViewById(R.id.dslr_listview_photo);
//
        photo_container = (RelativeLayout) view.findViewById(R.id.dslr_photo_container);
        photo_container.setVisibility(View.INVISIBLE);
//
        LinearLayoutManager horizontalLayoutManagaer = new LinearLayoutManager(MadamfiveAPI.getActivity().getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
//        listviewPhoto.setLayoutManager(horizontalLayoutManagaer);
//
        phonePhotoAdapter = new PhoneCameraPhotoAdapter(photoList);
//        listviewPhoto.setAdapter(phonePhotoAdapter);

        enableUi(false);
//        enableUi(true);

        shootingImageDisplayExtraOption = SmartFiPreference.getSfShootDisplayOpt(MadamfiveAPI.getActivity());
        if(shootingImageDisplayExtraOption){
            photo_container.setVisibility(View.VISIBLE);
        }

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

//        photoList.clear();
        List<PhotoModel> cameraPhotoList = PhotoModelService.findAll();
        for (PhotoModel photo : cameraPhotoList) {
            String mode = "DLSR";
            if (photo.getMode() == 0) {
                mode = "CAM";
            }
            photoList.add(photo);
            Log.d("#F", photo.getFilename() + "(" + photo.getUploaded() + "/" + mode + ")");
        }
//        phoneCameraPhotoAdapter = new PhoneCameraPhotoAdapter(photoList);
//        listviewPhoto.setAdapter(phoneCameraPhotoAdapter);

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

        (getActivity()).runOnUiThread(() -> {
            if (enabled) {
                dslrTextView.setText("촬영 가능 합니다 ");
                connectedImageView.setImageDrawable(getResources().getDrawable(R.drawable.conn_after));
                camera_ready_Notice.setVisibility(View.VISIBLE);
            } else {
                dslrTextView.setText("카메라를 연결해 주세요 ");
                connectedImageView.setImageDrawable(getResources().getDrawable(R.drawable.conn_before));
                camera_ready_Notice.setVisibility(View.GONE);
                readImage.setVisibility(View.GONE);
            }
        });

    }

    @OnClick(R.id.btn_cloud)
    public void cloudBtnClicked(){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, CloudFragment.newInstance(), null);
        ft.addToBackStack(null);
        ft.commit();
    }

    @OnClick(R.id.btn_sdcard)
    public void sdcardBtnClicked(){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, SDCardFragment.newInstance(), null);
        ft.addToBackStack(null);
        ft.commit();
    }

    @OnClick(R.id.btn_search_patient_dslr)
    public void searchPatientDslrBtnClicked(){
        FragmentTransaction changelogTx = getFragmentManager().beginTransaction();
        PatientDialogFragment patientDialogFragment = PatientDialogFragment.newInstance();
        changelogTx.add(patientDialogFragment, "환자검색");
        changelogTx.commit();
    }
    @OnClick(R.id.btn_search_doctor_dslr)
    public void searchDoctorDslrBtnClicked(){
        FragmentTransaction changelogTx = getFragmentManager().beginTransaction();
        DoctorDialogFragment doctorDialogFragment = DoctorDialogFragment.newInstance();
        changelogTx.add(doctorDialogFragment, "의사검색");
        changelogTx.commit();
    }
    @Override
    public void cameraStarted(Camera camera) {
        enableUi(true);
        //camera.retrieveStorages(this);
        //emptyView.setText(getString(R.string.gallery_loading));
        Log.i(TAG, "Loading...");
//        Log.i(TAG,camera.getDeviceInfo()+"");
    }

    @Override
    public void cameraStopped(Camera camera) {
        enableUi(false);
//        galleryAdapter.setItems(null);
//        MadamfiveAPI.isCameraOn = false;
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
        Log.i(TAG, "OBJECT:Added:" + handle + ":" + format);

        if (camera() != null) {
            if (format == PtpConstants.ObjectFormat.EXIF_JPEG) {
//                Log.i(TAG, "OBJECT:retrieveImage:");
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

        Log.d(TAG,"dslr받음 => onImageInfoRetrieved");
        handler.post(new Runnable() {
            @Override
            public void run() {
                Camera camera = camera();
                if (!inStart || camera == null) {
                    return;
                }

                if (currentObjectHandle == objectHandle) {
                    //Log.i(TAG, "1:onImageInfoRetrieved ###### [" + objectHandle + "] " + objectInfo.filename + "#####");
                    camera_ready_Notice.setVisibility(View.GONE);
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    if(displayPictureFragment!=null){
                        ft.remove(displayPictureFragment).commit();
                    }
                    if(galleryFragment != null){
                        ft.remove(galleryFragment).commit();
                        storageRead = false;
                    }
                    if(readImage.isEnabled())  readImage.setVisibility(View.GONE);
                    upload_Notice.setVisibility(View.VISIBLE);

                    try {
                        sendPhoto(currentObjectHandle, objectInfo, thumbnail, currentBitmap);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
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
//
//        currentScrollState = scrollState;
//
//        switch (scrollState) {
//            case OnScrollListener.SCROLL_STATE_IDLE: {
//                Camera camera = camera();
//                if (!inStart || camera == null) {
//                    break;
//                }
//                break;
//            }
//        }
//
//    }

//    @Override
//    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//    }

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

    private void sendPhoto(int objectHandle, ObjectInfo info, Bitmap thumb, Bitmap bitmap) throws UnsupportedEncodingException {
//        Log.d(TAG, "sendPhoto");
        currentObjectHandle = 0;
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HHmmssSSS").format(new Date());
        String HospitalId = SmartFiPreference.getHospitalId(MadamfiveAPI.getActivity());
        String PatientName = SmartFiPreference.getSfPatientName(MadamfiveAPI.getActivity());
        String PatientChart = SmartFiPreference.getPatientChart(MadamfiveAPI.getActivity());
        String DoctorName = SmartFiPreference.getSfDoctorName(MadamfiveAPI.getActivity());
        String DoctorNumber = SmartFiPreference.getSfDoctorNumber(MadamfiveAPI.getActivity());

        if (doctorSelectExtraOption && DoctorName != null && DoctorName.length() != 0) {
            mFileName = URLEncoder.encode(HospitalId+"_"+PatientName+"_"+PatientChart+"_"+DoctorName+"_"+DoctorNumber+"_"+timeStamp+".jpg", "UTF-8");
        } else {
            mFileName = URLEncoder.encode(HospitalId+"_"+PatientName+"_"+PatientChart+"_"+timeStamp+".jpg", "UTF-8");
        }

        Log.i(TAG, "mFileName:" + mFileName);


        mFile = new File(getActivity().getExternalFilesDir(Environment.getExternalStorageState())  + File.separator + mFileName);

        //썸네일 만들고 db에 해당 정보 저장하고 업로드 매니저 호출
        String path = DisplayUtil.storeDslrImage(mFile.toString(),
                getActivity().getExternalFilesDir(Environment.getExternalStorageState()),mFileName, bitmap, thumb);
        Log.i(TAG, "sendPhoto path:" + path);
        if(path != null){
            PhotoModel photoModel = PhotoModelService.addPhotoModel(getActivity(), mFile.toString(),path, mFileName, 1);
            Long id = photoModel.getId();
            PictureIntentService.startUploadPicture(getActivity(), path);
        }else{
            Toast.makeText(getActivity(), R.string.make_error_thumbnail, Toast.LENGTH_SHORT);
        }

    }

//    @Override
//    public void onWorkerStarted() {
//    }

//    @Override
//    public void onWorkerEnded() {
//    }

    ////////////////////////////////////////////////////////////////////
    // Camera.StorageInfoListener Override Methods
    ///////////////////////////////////////////////////////////////////

//    @Override
//    public void onImageHandlesRetrieved(final int[] handles) {
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                if (!inStart) {
//                    return;
//                }
//                if (handles.length == 0) {
////                    emptyView.setText(getString(R.string.gallery_empty));
//                }
//                Log.i(TAG, "onImageHandlesRetrieved:" + handles.length);
////                galleryAdapter.setHandles(handles);
//                if(objectHandleNumber == 0){
//                    objectHandleNumber = handles.length;
//                }
//
//                if(objectHandleNumber!=handles.length) {
//
//                    int end = handles.length;
//                    if(end!=0)  getImage(handles[end - 1]);
//                }
////                Log.i(TAG,"objectHandleNumber:::>>>"+objectHandleNumber);
//            }
//        });
//    }

//    @Override
//    public void onAllStoragesFound() {
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                if (!inStart || camera() == null) {
//                    return;
//                }
//                if (storageAdapter.getCount() == 0) {
////                    emptyView.setText(getString(R.string.gallery_empty));
//                    return;
//                } else if (storageAdapter.getCount() == 1) {
////                    storageSpinner.setEnabled(false);
//                }
////                storageSpinner.setSelection(0);
//                camera().retrieveImageHandles(DSLRFragment.this, storageAdapter.getItemHandle(0),
//                        PtpConstants.ObjectFormat.EXIF_JPEG);
//            }
//        });
//    }

//    @Override
//    public void onStorageFound(final int handle, final String label) {
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                if (!inStart) {
//                    return;
//                }
//                storageAdapter.add(handle, label);
//            }
//        });
//    }

    private void displayPhoto(int objectHandle,Bitmap currentBitmap){

        upload_Notice.setVisibility(View.GONE);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        Bitmap resized = Bitmap.createScaledBitmap(currentBitmap, 300, 200, true);

        readImage.setImageBitmap(resized);
        readImage.setEnabled(true);
        readImage.setVisibility(View.VISIBLE);

    }


}

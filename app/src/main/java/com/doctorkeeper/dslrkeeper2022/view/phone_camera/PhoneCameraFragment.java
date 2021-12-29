package com.doctorkeeper.dslrkeeper2022.view.phone_camera;

import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.usb.UsbManager;
import android.media.MediaActionSound;
import android.os.Bundle;
import android.os.Environment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.doctorkeeper.dslrkeeper2022.R;
//import com.doctorkeeper.dslrkeeper.activities.LaunchCameraActivity;
//import com.doctorkeeper.dslrkeeper.activities.LaunchVrecordActivity;
import com.doctorkeeper.dslrkeeper2022.madamfive.BlabAPI;
import com.doctorkeeper.dslrkeeper2022.madamfive.MadamfiveAPI;
import com.doctorkeeper.dslrkeeper2022.models.PhotoModel;
import com.doctorkeeper.dslrkeeper2022.services.PhotoModelService;
import com.doctorkeeper.dslrkeeper2022.services.PictureIntentService;
import com.doctorkeeper.dslrkeeper2022.util.DisplayUtil;
import com.doctorkeeper.dslrkeeper2022.util.SmartFiPreference;
import com.doctorkeeper.dslrkeeper2022.view.BaseFragment;
import com.doctorkeeper.dslrkeeper2022.view.cloud.CloudFragment;
import com.doctorkeeper.dslrkeeper2022.view.doctor.DoctorDialogFragment;
import com.doctorkeeper.dslrkeeper2022.view.dslr.DSLRFragment;
import com.doctorkeeper.dslrkeeper2022.view.patient.PatientDialogFragment;
import com.doctorkeeper.dslrkeeper2022.view.sdcard.SDCardFragment;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.doctorkeeper.dslrkeeper2022.madamfive.MadamfiveAPI.selectedDoctor;


public class PhoneCameraFragment extends BaseFragment {

    private final String TAG = PhoneCameraFragment.class.getSimpleName();

    private CameraView cameraView;
    private ArrayList<PhotoModel> photoList;
    private PhoneCameraPhotoAdapter phoneCameraPhotoAdapter;
    private RelativeLayout photo_container;

    private Animation rotate1Animation;
    private Animation rotate2Animation;
    private Animation rotate3Animation;
    private Animation rotate4Animation;

    private boolean cameraIsReady;
    private TextView patient_name;
    private TextView doctor_name;

    private VrecordInterface mVrecInterface;

    private final String  DEVICE = "phone";
    private String mFileName;

    private MediaActionSound mSound;
    private int android_ver = android.os.Build.VERSION.SDK_INT;

    private Boolean doctorSelectExtraOption;
    private Boolean shootingImageDisplayExtraOption;
    private Boolean fixedPortraitExtraOption;
    private Boolean fixedLandscapeExtraOption;

    public interface VrecordInterface{
        public void startRecord();
    }

    RecyclerView listviewPhoto;

    @BindView(R.id.button_list)
    ImageButton btnList;

    @BindView(R.id.button_dslr)
    ImageButton btnDslr;

    @BindView(R.id.button_patient)
    ImageButton btnPatient;

    @BindView(R.id.button_sdcard)
    ImageButton btnSDCard;

    @BindView(R.id.btn_launch_cameraApp)
    Button btnLaunchCameraApp;

    //kimcy: add video
//    @BindView(R.id.btn_launch_videoApp)
//    Button btnLaunchVideoApp;

    @BindView(R.id.button_capture)
    ImageButton btnCamera;

    @BindView(R.id.button_doctor)
    ImageButton btnDoctor;

    private OrientationListener orientationListener;

    public static PhoneCameraFragment newInstance() {
        PhoneCameraFragment f = new PhoneCameraFragment();
        return f;
    }

//    private final BroadcastReceiver usbOnReciever = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Log.w(TAG,"usbOnReciever === "+intent);
//            new android.os.Handler().postDelayed(
//                    new Runnable() {
//                        public void run() {
//                            btnDslr.setVisibility(View.VISIBLE);
//                            btnSDCard.setVisibility(View.VISIBLE);
//                            MadamfiveAPI.isCameraOn = true;
//                        }
//                    },
//                    2000);
//        }
//    };
//
//    private final BroadcastReceiver usbOffReciever = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Log.w(TAG,"usbOffReciever === "+intent);
//            btnDslr.setVisibility(View.INVISIBLE);
//            btnSDCard.setVisibility(View.INVISIBLE);
//            MadamfiveAPI.isCameraOn = true;
//        }
//    };
private final BroadcastReceiver usbOnReciever = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.w(TAG,"usbOnReciever === "+intent);
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        btnDslr.setVisibility(View.VISIBLE);
                        btnSDCard.setVisibility(View.VISIBLE);
                        BlabAPI.isCameraOn = true;
                    }
                },
                2000);
    }
};

    private final BroadcastReceiver usbOffReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.w(TAG,"usbOffReciever === "+intent);
            btnDslr.setVisibility(View.INVISIBLE);
            btnSDCard.setVisibility(View.INVISIBLE);
            BlabAPI.isCameraOn = false;
        }
    };
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.phone_camera_frag, container, false);
        ButterKnife.bind(this, view);

        cameraIsReady = true;
        photoList = new ArrayList<>();
        listviewPhoto = (RecyclerView)view.findViewById(R.id.listview_photo);

        photo_container = (RelativeLayout) view.findViewById(R.id.photo_container);
        photo_container.setVisibility(View.INVISIBLE);

        btnDslr.setVisibility(View.INVISIBLE);
        btnSDCard.setVisibility(View.INVISIBLE);
        if(MadamfiveAPI.isCameraOn == true){
            btnDslr.setVisibility(View.VISIBLE);
            btnSDCard.setVisibility(View.VISIBLE);
        }

        LinearLayoutManager horizontalLayoutManagaer = new LinearLayoutManager(MadamfiveAPI.getActivity().getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        listviewPhoto.setLayoutManager(horizontalLayoutManagaer);

        fixedPortraitExtraOption = SmartFiPreference.getSfShootPortraitOpt(MadamfiveAPI.getActivity());

        cameraView = (CameraView) view.findViewById(R.id.camera);
        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {
                switch (cameraKitEvent.getType()) {
                    case CameraKitEvent.TYPE_CAMERA_OPEN:
                        break;

                    case CameraKitEvent.TYPE_CAMERA_CLOSE:
                        break;
                }
            }
            @Override
            public void onError(CameraKitError cameraKitError) {
            }
            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                Log.w(TAG,"onImage phone cam");
                mSound.release();
                Bitmap picture = cameraKitImage.getBitmap();
                String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HHmmssSSS").format(new Date());
                mFileName = DEVICE + "_" + timeStamp+".jpg";
                if(fixedPortraitExtraOption){
                    int orientationValue = orientationListener.rotation;
                    Bitmap picture2 = rotateImage(picture,orientationValue);
                    savePhotoNUpload(picture2, "phone", mFileName);
                }else {
                    savePhotoNUpload(picture, "phone", mFileName);
                }
            }
            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {
            }
        });

        List<PhotoModel> cameraPhotoList = PhotoModelService.findAll();
        for (PhotoModel photo : cameraPhotoList) {
            String mode = "DLSR";
            if (photo.getMode() == 0) {
                mode = "CAM";
            }
            photoList.add(photo);
            Log.d("#F", photo.getFilename() + "(" + photo.getUploaded() + "/" + mode + ")");
        }

        phoneCameraPhotoAdapter = new PhoneCameraPhotoAdapter(photoList);
        listviewPhoto.setAdapter(phoneCameraPhotoAdapter);

        fixedLandscapeExtraOption = SmartFiPreference.getSfDisplayLandscapeOpt(MadamfiveAPI.getActivity());
        if(!fixedLandscapeExtraOption){
//            rotate1Animation = AnimationUtils.loadAnimation(MadamfiveAPI.getContext(), R.anim.rotate_1);
//            rotate2Animation = AnimationUtils.loadAnimation(MadamfiveAPI.getContext(), R.anim.rotate_2);
//            rotate3Animation = AnimationUtils.loadAnimation(MadamfiveAPI.getContext(), R.anim.rotate_3);
//            rotate4Animation = AnimationUtils.loadAnimation(MadamfiveAPI.getContext(), R.anim.rotate_4);
//
//            orientationListener = new OrientationListener(MadamfiveAPI.getContext());
//            orientationListener.enable();
        }

        patient_name = (TextView)view.findViewById(R.id.patient_name);

        Log.w(TAG,"초기이름 = "+SmartFiPreference.getSfPatientName(MadamfiveAPI.getActivity()));
        patient_name.setText(SmartFiPreference.getSfPatientName(MadamfiveAPI.getActivity()));

        IntentFilter on = new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        IntentFilter off = new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED);
        MadamfiveAPI.getContext().registerReceiver(usbOnReciever,on);
        MadamfiveAPI.getContext().registerReceiver(usbOffReciever,off);

        // Display Doctor info : OPTION
        doctorSelectExtraOption = SmartFiPreference.getSfInsertDoctorOpt(MadamfiveAPI.getActivity());
        doctor_name = (TextView)view.findViewById(R.id.doctor_name);
        if(!doctorSelectExtraOption){
            btnDoctor.setVisibility(View.INVISIBLE);
            doctor_name.setVisibility(View.INVISIBLE);
        }else{
            HashMap<String,String> doctor = new HashMap<>();
            String name = SmartFiPreference.getSfDoctorName(MadamfiveAPI.getActivity());
            String number =SmartFiPreference.getSfDoctorNumber(MadamfiveAPI.getActivity());
            doctor.put("name", name);
            doctor.put("doctorNumber", number);
            selectedDoctor = doctor;
            if(selectedDoctor!=null){
                doctor_name.setText(selectedDoctor.get("name"));
            }
        }

        shootingImageDisplayExtraOption = SmartFiPreference.getSfShootDisplayOpt(MadamfiveAPI.getActivity());
        if(shootingImageDisplayExtraOption){
            photo_container.setVisibility(View.VISIBLE);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        cameraView.start();

        photoList.clear();
        List<PhotoModel> cameraPhotoList = PhotoModelService.findAll();
        for (PhotoModel photo : cameraPhotoList) {
            String mode = "DLSR";
            if (photo.getMode() == 0) {
                mode = "CAM";
            }
            photoList.add(photo);
            Log.d("#F", photo.getFilename() + "(" + photo.getUploaded() + "/" + mode + ")");
        }
        phoneCameraPhotoAdapter = new PhoneCameraPhotoAdapter(photoList);
        listviewPhoto.setAdapter(phoneCameraPhotoAdapter);

        Log.i(TAG, "PhoneCameraFragment onResume >>>>>>>>>>");
//        Log.i(TAG,"MadamfiveAPI.isCameraOn ::: "+MadamfiveAPI.isCameraOn);

    }

    @Override
    public void onPause() {
        cameraView.stop();
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
//        cameraView.stop();
    }

    private void savePhotoNUpload(Bitmap picture, String phone, String mFileName) {
        Log.w(TAG,"savePhotoNUpload");
        File file = new File(MadamfiveAPI.getActivity().getExternalFilesDir(Environment.getExternalStorageState())  + File.separator + mFileName);

        String srcPath = file.toString();
        String path = DisplayUtil.storePtictureNThumbImage(srcPath,
                MadamfiveAPI.getActivity().getExternalFilesDir(Environment.getExternalStorageState()), mFileName, picture);

        if(path != null){
            PhotoModel photoModel = PhotoModelService.addPhotoModel(MadamfiveAPI.getActivity(), srcPath,path, mFileName, 0);
            Long id = photoModel.getId();
//            Log.i("phone",id.toString());
            PictureIntentService.startUploadPicture(MadamfiveAPI.getActivity(), path);
            photoList.add(0, photoModel);
            phoneCameraPhotoAdapter.notifyDataSetChanged();
        }else{
            Toast.makeText(MadamfiveAPI.getActivity(), R.string.error_upload_image, Toast.LENGTH_SHORT);
        }
    }

    @OnClick(R.id.button_capture)
    public void onTakePhoto(View view) {
        if(cameraIsReady) {
            Log.w(TAG,"카메라촬영, 셔터음");

            if(isInsertPatient()){
                cameraView.captureImage();
                mSound = new MediaActionSound();
                if (android_ver <= 28) {
                    mSound.play(MediaActionSound.SHUTTER_CLICK);
                }else {
                }
            }else{

                Toast.makeText(MadamfiveAPI.getActivity(),getString(R.string.p_insert_patient),Toast.LENGTH_SHORT).show();
            }
//            mSound = new MediaActionSound();
//            mSound.play(MediaActionSound.SHUTTER_CLICK);
//            cameraView.captureImage();

//            cameraIsReady = false;
//            btnCamera.setClickable(false);
        }
    }

    private boolean isInsertPatient() {
        if(SmartFiPreference.getSfPatientName(MadamfiveAPI.getActivity()).equals("")){
            return false;
        }else{
            return true;
        }
    }

    @OnClick(R.id.button_dslr)
    public void onShowDslr(View view) {

        try {
            cameraView.stop();
        }catch(Exception e){
            Log.i(TAG,"ERROR~~~"+e);
        }

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, DSLRFragment.newInstance(), null);
        ft.addToBackStack(null);
        ft.commit();
    }

    @OnClick(R.id.button_list)
    public void onToggleList(View view) {
        if(isInsertPatient()){
            try {
                cameraView.stop();
            }catch(Exception e){
                Log.e(TAG,"ERROR~~~"+e);
            }
//            Log.i(TAG, "CLoud Btn Clicked");
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, CloudFragment.newInstance(), null);
            ft.addToBackStack(null);
            ft.commit();
        }else{
            Toast.makeText(MadamfiveAPI.getActivity(),getString(R.string.p_insert_patient),Toast.LENGTH_SHORT).show();
        }
    }

//    @OnClick(R.id.btn_launch_cameraApp)
//    public void launchCameraApp(View view) {
//        if(isInsertPatient()){
//            try {
//                cameraView.stop();
//            }catch(Exception e){
//                Log.e(TAG,"ERROR~~~"+e);
//            }
//
//            Intent intent = new Intent(MadamfiveAPI.getActivity(), LaunchCameraActivity.class);
//            startActivity(intent);
//        }else{
//            Toast.makeText(MadamfiveAPI.getActivity(),getString(R.string.p_insert_patient),Toast.LENGTH_SHORT).show();
//        }
//
//    }

//    @OnClick(R.id.btn_launch_videoApp)
//    public void launchVideoApp(View view) {
//        if(isInsertPatient()){
//            try {
//                cameraView.stop();
//            }catch(Exception e){
//                Log.e(TAG,"ERROR~~~"+e);
//            }
//
//            Intent intent = new Intent(getActivity(), LaunchVrecordActivity.class);
//            startActivity(intent);
//
//            mVrecInterface.startRecord();
//        }else{
//            Toast.makeText(getActivity(),getString(R.string.p_insert_patient),Toast.LENGTH_SHORT).show();
//        }
//
//    }

    @OnClick(R.id.button_patient)
    public void onSearchPatient(View veiw){
        //로그아웃 후에는 로그인 화면부터 나와야 함.
        FragmentTransaction changelogTx = getFragmentManager().beginTransaction();
        PatientDialogFragment patientDialogFragment = PatientDialogFragment.newInstance();
        changelogTx.add(patientDialogFragment, "환자검색");
        changelogTx.commit();
    }

    @OnClick(R.id.button_doctor)
    public void onSearchDoctor(View veiw){
        FragmentTransaction changelogTx = getFragmentManager().beginTransaction();
        DoctorDialogFragment doctorDialogFragment = DoctorDialogFragment.newInstance();
        changelogTx.add(doctorDialogFragment, "의사검색");
        changelogTx.commit();
    }

    @OnClick(R.id.button_sdcard)
    public void onReadSDCard(View veiw){

        try {
            cameraView.stop();
        }catch(Exception e){
            Log.i(TAG,"ERROR~~~"+e);
        }

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, SDCardFragment.newInstance(), null);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(MadamfiveAPI.getActivity() !=null && MadamfiveAPI.getActivity() instanceof VrecordInterface){
            mVrecInterface = (VrecordInterface)MadamfiveAPI.getActivity();
        }
    }

    private class OrientationListener extends OrientationEventListener {

        final int ROTATION_O = 1;
        final int ROTATION_90 = 2;
        final int ROTATION_180 = 3;
        final int ROTATION_270 = 4;

        private int rotation = 0;

        public OrientationListener(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if ((orientation < 35 || orientation > 325) && rotation != ROTATION_O) { // PORTRAIT
                rotation = ROTATION_O;
//                btnDslr.startAnimation(rotate1Animation);
                btnList.startAnimation(rotate1Animation);
                btnPatient.startAnimation(rotate1Animation);
            } else if (orientation > 145 && orientation < 215 && rotation != ROTATION_180) { // REVERSE PORTRAIT
                rotation = ROTATION_180;
//                btnDslr.startAnimation(rotate3Animation);
                btnList.startAnimation(rotate3Animation);
                btnPatient.startAnimation(rotate3Animation);
            } else if (orientation > 55 && orientation < 125 && rotation != ROTATION_270) { // REVERSE LANDSCAPE
                rotation = ROTATION_270;
//                btnDslr.startAnimation(rotate4Animation);
                btnList.startAnimation(rotate4Animation);
                btnPatient.startAnimation(rotate4Animation);
            } else if (orientation > 235 && orientation < 305 && rotation != ROTATION_90) { //LANDSCAPE
                rotation = ROTATION_90;
//                btnDslr.startAnimation(rotate2Animation);
                btnList.startAnimation(rotate2Animation);
                btnPatient.startAnimation(rotate2Animation);
            }
        }
    }

    private Bitmap rotateImage(Bitmap image,int orientationValue){
            if(fixedPortraitExtraOption) {
                if (orientationValue == 2) {
                    image = rotate(image, 90);
                } else if (orientationValue == 3) {
                    image = rotate(image, 180);
                } else if (orientationValue == 4) {
                    image = rotate(image, 270);
                }
            }else{
                if (orientationValue == 6) {
                    image = rotate(image, 90);
                } else if (orientationValue == 8) {
                    image = rotate(image, 180);
                }
            }

        return image;
    }

    public Bitmap rotate(Bitmap bitmap, int degrees) {
        if(degrees != 0 && bitmap != null)
        {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) bitmap.getWidth() / 2,
                    (float) bitmap.getHeight() / 2);

            try{
                Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
                if(bitmap != converted)
                {
                    bitmap.recycle();
                    bitmap = converted;
                }
            } catch(OutOfMemoryError ex) {
                // 메모리가 부족하여 회전을 시키지 못할 경우 그냥 원본을 반환합니다.
            }
        }
        return bitmap;
    }


}
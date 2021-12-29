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
import android.content.DialogInterface;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.doctorkeeper.dslrkeeper2022.R;
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
import com.doctorkeeper.dslrkeeper2022.view.dslr.DSLRFragment;
import com.doctorkeeper.dslrkeeper2022.view.sdcard.GalleryAdapter.ViewHolder;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * dslr로 부터 이미지를 읽어서 grid view 형태로 보여주기 위해..
 */
public class GalleryFragment extends SessionFragment
        implements Camera.StorageInfoListener,
        Camera.RetrieveImageListener,
        Camera.RetrieveImageInfoListener,
        OnScrollListener,
        OnItemClickListener {

    private final Handler handler = new Handler();

    @BindView(R.id.storage_spinner)
    Spinner storageSpinner;

    private StorageAdapter storageAdapter;

    @BindView(android.R.id.list)
    GridView galleryView;

    private GalleryAdapter galleryAdapter;

    @BindView(R.id.reverve_order_checkbox)
    CheckBox orderCheckbox;

    private SimpleDateFormat formatParser;
    boolean gotThumbWidth;
    private int currentScrollState;
    public HashMap<String,Object> pictureMap;

    private int currentObjectHandle;
    private Bitmap currentBitmap;
    private final String TAG = GalleryFragment.class.getSimpleName();

    private ArrayList<PhotoModel> photoModelLists;

    private ArrayList<Integer> selectedObjectHandles;
    private int selectedImageNumber;

    private Handler saveHandler;
    private HandlerThread saveHandlerThread;

    private ProgressBar multi_image_uploading_progressbar;
    private TextView multi_image_uploading_message;
    private int numberOfSendPhoto;
    private int numberOfUploaded;
    private int progressBarPortionSum;
    private static GalleryFragment mGalleryFragment;

    private String mFileName;
    private File mFile;
    private final String  DEVICE = "dslr";

    public static GalleryFragment newInstance() {
        mGalleryFragment = new GalleryFragment();
        return mGalleryFragment;
    }

    public static GalleryFragment getInstance(){
        return mGalleryFragment;
    }

    private Handler msgHandler = new Handler(new Handler.Callback() {
        int uploadCount = 0;
        @Override
        public boolean handleMessage(Message msg) {
//            Object path = msg.obj;
            return true;
        }
    });


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //formatParser = new SimpleDateFormat("yyyyMMdd'T'HHmmss.S");
        formatParser = new SimpleDateFormat("yyyy-MM-dd-HHmmss.S");
        currentScrollState = OnScrollListener.SCROLL_STATE_IDLE;

        View view = inflater.inflate(R.layout.gallery_frag, container, false);
        ButterKnife.bind(this, view);

        ((SessionActivity) getActivity()).setSessionView(this);

        //storageSpinner = (Spinner) view.findViewById(R.id.storage_spinner);
        storageAdapter = new StorageAdapter(getActivity());
        storageSpinner.setAdapter(storageAdapter);
        storageSpinner.setVisibility(View.GONE);   //만들고 여기서는 사용하지 않음

//        emptyView.setText(getString(R.string.gallery_loading));
        //galleryView = (GridView) view.findViewById(android.R.id.list);
        galleryAdapter = new GalleryAdapter(getActivity(), this);
        galleryAdapter.setReverseOrder(getSettings().isGalleryOrderReversed());
        galleryView.setAdapter(galleryAdapter);
        galleryView.setOnScrollListener(this);
//        galleryView.setEmptyView(emptyView);
        galleryView.setOnItemClickListener(this);

        //orderCheckbox = (CheckBox) view.findViewById(R.id.reverve_order_checkbox);
        orderCheckbox.setChecked(getSettings().isGalleryOrderReversed());
        orderCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onReverseOrderStateChanged(isChecked);
            }
        });
        orderCheckbox.setVisibility(View.GONE);

        enableUi(false);
        pictureMap = new HashMap<>();
        selectedImageNumber=0;
        selectedObjectHandles = new ArrayList<>();
//        selectedToast=false;

        saveHandlerThread = new HandlerThread("imageUploadThread_SDcard");
        saveHandlerThread.start();
        saveHandler = new Handler(saveHandlerThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                ArrayList<Integer> tempList = (ArrayList<Integer>) msg.obj;
                retrieveDirection(tempList);
            }
        };

        multi_image_uploading_progressbar = (ProgressBar)view.findViewById(R.id.multi_image_uploading_progressbar);
        multi_image_uploading_progressbar.setVisibility(View.INVISIBLE);
        multi_image_uploading_message = view.findViewById(R.id.multi_image_uploading_message);
        multi_image_uploading_message.setVisibility(View.INVISIBLE);
//        mProgressTask = new ProgressTask(multi_image_uploading_progressbar,MadamfiveAPI.getContext());

        return view;
    }

    private void retrieveDirection(ArrayList<Integer> list){
        numberOfSendPhoto = list.size();
        numberOfUploaded = 0;
        for(int h : list) {
            camera().retrieveImage(this, h);
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
        getSettings().setGalleryOrderReversed(orderCheckbox.isChecked());
    }

    protected void onReverseOrderStateChanged(boolean isChecked) {
        galleryAdapter.setReverseOrder(isChecked);
    }

    @Override
    public void enableUi(boolean enabled) {

        Log.i(TAG, "READ Storage EnableUi..." + enabled);

        storageSpinner.setEnabled(enabled);
        galleryView.setEnabled(enabled);
        orderCheckbox.setEnabled(enabled);
        photoModelLists = PhotoModelService.findImageListOld();

        numberOfSendPhoto=0;
        progressBarPortionSum=0;

    }

    @Override
    public void cameraStarted(Camera camera) {
        enableUi(true);
        camera.retrieveStorages(this);
//        emptyView.setText(getString(R.string.gallery_loading));
//        Log.i(TAG, "Reading Storage...");
    }

    @Override
    public void cameraStopped(Camera camera) {
//        Log.d(TAG,"cameraStopped");
        enableUi(false);
        galleryAdapter.setHandles(new int[0]);
    }

    @Override
    public void propertyChanged(int property, int value) {
//        if (property == 7) {
//            camera().retrieveStorages(this);
//        }
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
        Log.w(TAG, "BITMAP:capturedPictureReceived:" + bitmap.getWidth() + "x" + bitmap.getHeight());
    }

    @Override
    public void objectAdded(int handle, int format) {
//        Log.i(TAG, "OBJECT:Added:" + handle + ":" + format);

        if (camera() != null) {
            if (format == PtpConstants.ObjectFormat.EXIF_JPEG) {
                Log.w(TAG, "OBJECT:retrieveImage:");
//                camera().retrieveImage(this, handle);
            }
        }
        if (camera() == null) {
            return;
        }

        if (format == PtpConstants.ObjectFormat.EXIF_JPEG) {
//            camera().retrieveImageInfo(this, handle);
        }

        enableUi(false);

//        if (format == PtpConstants.ObjectFormat.EXIF_JPEG) {
//            if (isPro && liveViewToggle.isChecked() && showCapturedPictureNever) {
//                camera().retrieveImageInfo(this, handle);
//                handler.post(liveViewRestarterRunner);
//            } else {
//                camera().retrievePicture(handle);
//            }
//        }
    }

    @Override
    public void onStorageFound(final int handle, final String label) {
        Log.d(TAG, "onStorageFound");
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!inStart) {
                    return;
                }
                storageAdapter.add(handle, label);
            }
        });
    }

    @Override
    public void onAllStoragesFound() {
        Log.d(TAG, "onAllStoragesFound");
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!inStart || camera() == null) {
                    return;
                }
                if (storageAdapter.getCount() == 0) {
//                    emptyView.setText(getString(R.string.gallery_empty));
                    return;
                } else if (storageAdapter.getCount() == 1) {
                    storageSpinner.setEnabled(false);
                }
                storageSpinner.setSelection(0);
                camera().retrieveImageHandles(GalleryFragment.this, storageAdapter.getItemHandle(0),
                        PtpConstants.ObjectFormat.EXIF_JPEG);
            }
        });
    }

    @Override
    public void onImageHandlesRetrieved(final int[] handles) {
        Log.w(TAG, "onImageHandlesRetrieved");
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!inStart) {
                    return;
                }
                if (handles.length == 0) {
//                    emptyView.setText(getString(R.string.gallery_empty));
                }
                Log.w(TAG, "onImageHandlesRetrieved:" + handles.length);

                galleryAdapter.setHandles(handles);

//                for(int k=0;k<handles.length;k++) {
//                    multiSelectionMap.put(handles[k], false);
//                }
            }
        });
    }

    @Override
    public void onImageInfoRetrieved(final int objectHandle, final ObjectInfo objectInfo, final Bitmap thumbnail) {

       // Log.w(TAG, "갤러러, 이미지 정보  ==> onImageInfoRetrieved");
        handler.post(new Runnable() {

            @Override
            public void run() {
                final Camera camera = camera();
                if (!inStart || camera == null) {
                    return;
                }
                if (currentObjectHandle == objectHandle) {
                    Log.w(TAG, "1:onImageInfoRetrieved ###### [" + objectHandle + "] " + objectInfo.filename + "#####");
                }
                if (!gotThumbWidth && thumbnail != null) {
                    gotThumbWidth = true;
                    galleryAdapter.setThumbDimensions(thumbnail.getWidth(), thumbnail.getHeight());
                }
                for (int i = 0; i < galleryView.getChildCount(); ++i) {
                    //View child = galleryView.getChildAt(i );
                    View child = galleryView.getChildAt(i );

                    Log.d(TAG,"child :"+child);
                    if (child == null) {
                        Log.w(TAG,"child is null !!");
                        continue;
                    }
                    final ViewHolder holder = (ViewHolder) child.getTag();
                    if (holder.objectHandle == objectHandle) {
//                        Log.w(TAG,"정보저장 :");
                        pictureMap.put(objectHandle+"",objectInfo.filename); //업로드할 파일들..
                        pictureMap.put(objectHandle+"_data",thumbnail);

                        holder.image1.setImageBitmap(thumbnail);  //썸네일

                        Boolean uploadCheck=false;
                        for(int d = 0 ;d < photoModelLists.size();d++){
                            PhotoModel p = photoModelLists.get(d);

                            if(objectInfo.filename.equals(p.getRawfileName())){
//                                Log.w(TAG,"업로드 상태 체크 :");
                                uploadCheck = p.getUploaded();
                                break;
                            }
                        }

                        if (!"".equals(objectInfo.captureDate)) {
                            try {
                                Date date = formatParser.parse(objectInfo.captureDate);
                                DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                                String createdDate = df.format(date);
                                holder.date.setText(createdDate);
//                                holder.date.setText(date.toLocaleString());
                            } catch (ParseException e) {
                            }
                        }
                        break;
                    }
                }
            }
        });
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
//        Log.d(TAG,"onScrollStateChanged = "+scrollState);
        currentScrollState = scrollState;
        switch (scrollState) {
            case OnScrollListener.SCROLL_STATE_IDLE: {
                Camera camera = camera();   //연결된 카메라를 말함..
                if (!inStart || camera == null) {
                    break;
                }
                for (int i = 0; i < galleryView.getChildCount(); ++i) {

                    View child = view.getChildAt(i);
                    if (child == null) {
                        continue;
                    }
                    ViewHolder holder = (ViewHolder) child.getTag();
                    Log.d(TAG,"holder.done = "+holder.done);
                    if (!holder.done) {
                        Log.d(TAG,"holder.done => false 신규요청??? ");
                        holder.done = true;
                        camera.retrieveImageInfo(this, holder.objectHandle);
                    }
                }
                break;
            }
        }

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    }

    public void onNewListItemCreated(ViewHolder holder) {
        Log.d(TAG,"onNewListItemCreated");
        if (currentScrollState == SCROLL_STATE_IDLE) {
            Camera camera = camera();
            if (camera == null) {
                return;
            }
            holder.done = true;
            //Log.i(TAG, "1:onNewListItemCreated ###### [" + holder.objectHandle + "] #####");
            camera.retrieveImageInfo(this, holder.objectHandle);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

//        Log.w(TAG,"item selected");
//        View child = galleryView.getChildAt(position);
        View child = galleryView.getChildAt(position - parent.getFirstVisiblePosition());
        final ViewHolder holder = (ViewHolder) child.getTag();

        if(selectedObjectHandles.contains(galleryAdapter.getItemHandle(position))){
            int remove = selectedObjectHandles.indexOf(galleryAdapter.getItemHandle(position));
            selectedObjectHandles.remove(remove);
            holder.sdcard_image_need_upload.setVisibility(View.INVISIBLE);
            selectedImageNumber--;
        }else{
            selectedObjectHandles.add(galleryAdapter.getItemHandle(position));
            holder.sdcard_image_need_upload.setVisibility(View.VISIBLE);
            selectedImageNumber++;
        }
////          toast Window-----
//        LayoutInflater inflater =  (LayoutInflater) MadamfiveAPI.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View layout = inflater.inflate(R.layout.toast_multiple_selection_sdcard,  (ViewGroup) view.findViewById(R.id.toast_container));
//
//        TextView text = (TextView) layout.findViewById(R.id.sdcard_toast_textview);
//        Button sdcard_toast_select = (Button) layout.findViewById(R.id.sdcard_toast_select);
//        Button sdcard_toast_upload = (Button) layout.findViewById(R.id.sdcard_toast_upload);
//
//        final Toast toast = new Toast(MadamfiveAPI.getContext());
//        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 500);
//        toast.setDuration(Toast.LENGTH_LONG);
//        text.setText(selectedImageNumber+"개 이미지 선택");
//        toast.setView(layout);
//        toast.show();
//        sdcard_toast_select.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                Log.w(TAG,"계속선택 클릭");
//                toast.cancel();
//            }
//        });
//
//        sdcard_toast_upload.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                Log.w(TAG,"업로드 클릭");
//                toast.cancel();
//
//                multi_image_uploading_progressbar.setVisibility(View.VISIBLE);
//                multi_image_uploading_message.setVisibility(View.VISIBLE);
//                int[] tempHandles = new int[0];
//                galleryAdapter.setHandles(tempHandles);
//
//                multi_image_uploading_progressbar.setMax(selectedImageNumber);
//                uploadSelectedHandles();
//            }
//        });



        //Custom Dialog------
//        LayoutInflater inflater =  (LayoutInflater) MadamfiveAPI.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View layout = inflater.inflate(R.layout.alert_multiple_selection_sdcard,  (ViewGroup) view.findViewById(R.id.alert_container));
//
//        final Dialog dia = new Dialog (MadamfiveAPI.getContext());
//        TextView text = (TextView) dia.findViewById(R.id.sdcard_alert_textview);
//        Button sdcard_alert_select = (Button) dia.findViewById(R.id.sdcard_alert_select);
//        Button sdcard_alert_upload = (Button) dia.findViewById(R.id.sdcard_alert_upload);
//        Window window = dia.getWindow();
//        window.setGravity((Gravity.CENTER));
//        window.setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//        dia.setTitle(null);
//        text.setText(selectedImageNumber+"개 이미지 선택");
//        dia.show();
//
//        sdcard_alert_select.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.w(TAG,"계속선택 클릭");
//                dia.cancel();
//            }
//        });
//
//        sdcard_alert_upload.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.w(TAG,"업로드 클릭");
//                dia.cancel();
//
//                multi_image_uploading_progressbar.setVisibility(View.VISIBLE);
//                multi_image_uploading_message.setVisibility(View.VISIBLE);
//                int[] tempHandles = new int[0];
//                galleryAdapter.setHandles(tempHandles);
//
//                multi_image_uploading_progressbar.setMax(selectedImageNumber);
//                uploadSelectedHandles();
//            }
//        });

////before
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        builder.setMessage(selectedImageNumber+"개 이미지 선택");
//        builder.setPositiveButton(R.string.sdcard_toast_select_upload, new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//                // User clicked OK button
//                Log.w(TAG,"업로드 클릭");
//                multi_image_uploading_progressbar.setVisibility(View.VISIBLE);
//                multi_image_uploading_message.setVisibility(View.VISIBLE);
//                int[] tempHandles = new int[0];
//                galleryAdapter.setHandles(tempHandles);
//
//                multi_image_uploading_progressbar.setMax(selectedImageNumber);
//                uploadSelectedHandles();
//            }
//        });
//        builder.setNegativeButton(R.string.sdcard_toast_select_continue, new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//                // User cancelled the dialog
//                Log.w(TAG,"계속선택 클릭");
//            }
//        });
//
//        AlertDialog dialog = builder.create();
//        dialog.show();

// DeviceDefault_Light_Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.MyDialogTheme);
        builder.setTitle(selectedImageNumber+"개 이미지 선택");
        builder.setMessage("");
        builder.setPositiveButton("업로드", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // User clicked OK button
                Log.w(TAG,"업로드 클릭");
                multi_image_uploading_progressbar.setVisibility(View.VISIBLE);
                multi_image_uploading_message.setVisibility(View.VISIBLE);
                int[] tempHandles = new int[0];
                galleryAdapter.setHandles(tempHandles);

                multi_image_uploading_progressbar.setMax(selectedImageNumber);
                uploadSelectedHandles();
            }
        });
        builder.setNeutralButton("계속 선택", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // User cancelled the dialog
                Log.w(TAG,"계속선택 클릭");
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();


    }


    /**
     * Camera.RetrieveImageListener
     *
     * @param objectHandle
     * @param image
     */
    @Override
    public void onImageRetrieved(int objectHandle, Bitmap image) {
//        Log.w(TAG,"onImageRetrieved, 카메라로부터 이미지 받음");
        Camera camera = camera();
        if (camera == null) {
            return;
        }
        currentObjectHandle = objectHandle;
        currentBitmap = image;
        String currentObjectInfo = pictureMap.get(currentObjectHandle+"")+"";
        Bitmap thumb = (Bitmap) pictureMap.get(objectHandle+"_data");
//        Log.e(TAG,"retrieveImage+++Done"+currentObjectInfo);
        //삭제 예정
       //sendPhoto(currentObjectInfo, currentBitmap);
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HHmmssSSS").format(new Date());
        String HospitalId = SmartFiPreference.getHospitalId(MadamfiveAPI.getActivity());
        String PatientName = SmartFiPreference.getSfPatientName(MadamfiveAPI.getActivity());
        String PatientChart = SmartFiPreference.getPatientChart(MadamfiveAPI.getActivity());
        String DoctorName = SmartFiPreference.getSfDoctorName(MadamfiveAPI.getActivity());
        String DoctorNumber = SmartFiPreference.getSfDoctorNumber(MadamfiveAPI.getActivity());

        if (DSLRFragment.doctorSelectExtraOption && DoctorName != null && DoctorName.length() != 0) {
            try {
                mFileName = URLEncoder.encode(HospitalId+"_"+PatientName+"_"+PatientChart+"_"+DoctorName+"_"+DoctorNumber+"_"+timeStamp+".jpg", "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            try {
                mFileName = URLEncoder.encode(HospitalId+"_"+PatientName+"_"+PatientChart+"_"+timeStamp+".jpg", "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        mFile = new File(getActivity().getExternalFilesDir(Environment.getExternalStorageState())  + File.separator + mFileName);
        String path = DisplayUtil.storeDslrImage(mFile.toString(),
                getActivity().getExternalFilesDir(Environment.getExternalStorageState()),mFileName, currentBitmap, thumb);

        if(path != null){
//            PhotoModel photoModel = PhotoModelService.addPhotoModelWithRawName(getActivity(), mFile.toString(),path, mFileName, currentObjectInfo, 1);
//            Long id = photoModel.getId();
            Messenger messenger = new Messenger(msgHandler);
            PictureIntentService.startUploadPicture(getActivity(), path);
            numberOfUploaded++;
            multi_image_uploading_progressbar.setProgress(numberOfUploaded);

            if(numberOfSendPhoto == numberOfUploaded){
//                multi_image_uploading_progressbar.setVisibility(View.INVISIBLE);

                Handler mHandler = new Handler(Looper.getMainLooper());
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        backToPhotoCameraFragment();
                    }
                }, 0);

//                backToPhotoCameraFragment();
            }

        }else{
            Toast.makeText(getActivity(), R.string.error_read_image, Toast.LENGTH_SHORT);
        }

    }

    private void uploadSelectedHandles(){
//        Log.w(TAG,"uploadSelectedHandles");
        Message msg = saveHandler.obtainMessage();
        msg.obj = selectedObjectHandles;
        saveHandler.sendMessage(msg);
    }

    private void backToPhotoCameraFragment(){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, DSLRFragment.newInstance(), null);
        ft.addToBackStack(null);
        ft.commit();
    }


}

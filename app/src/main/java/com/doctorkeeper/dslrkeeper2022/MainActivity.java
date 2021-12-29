package com.doctorkeeper.dslrkeeper2022;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.doctorkeeper.dslrkeeper2022.activities.AppSettingsActivity;
import com.doctorkeeper.dslrkeeper2022.madamfive.MadamfiveAPI;
import com.doctorkeeper.dslrkeeper2022.ptp.Camera;
import com.doctorkeeper.dslrkeeper2022.ptp.Camera.CameraListener;
import com.doctorkeeper.dslrkeeper2022.ptp.PtpService;
import com.doctorkeeper.dslrkeeper2022.ptp.model.LiveViewData;
import com.doctorkeeper.dslrkeeper2022.util.PackageUtil;
import com.doctorkeeper.dslrkeeper2022.util.SmartFiPreference;
import com.doctorkeeper.dslrkeeper2022.view.SessionActivity;
import com.doctorkeeper.dslrkeeper2022.view.SessionView;
import com.doctorkeeper.dslrkeeper2022.view.WebViewDialogFragment;
import com.doctorkeeper.dslrkeeper2022.view.dslr.DSLRFragment;
import com.doctorkeeper.dslrkeeper2022.view.log_in.LoginDialogFragment;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import java.io.File;


public class MainActivity extends SessionActivity implements CameraListener {

    private static final int DIALOG_PROGRESS = 1;
    private static final int DIALOG_NO_CAMERA = 2;

    private final String TAG = MainActivity.class.getSimpleName();

    private final Handler handler = new Handler();

    private PtpService ptp;
    private Camera camera;

    private boolean isInStart;
    private boolean isInResume;
    private SessionView sessionFrag;
    private boolean isLarge;
    private AppSettings settings;

    private int backButtonCount;

    private boolean cameraListenerInitialized = false;

    private long startTime=5*60*1000;
    private final long interval = 1 * 1000;
    public static MyCountDownTimer countDownTimer;
    private boolean isVrecording;
    private Context mCon;

    private Boolean fixedLandscapeExtraOption;

    @Override
    public Camera getCamera() {        return camera;    }

    @Override
    public void setSessionView(SessionView view) {
        sessionFrag = view;

        if (cameraListenerInitialized == false) {
            Log.i(TAG,"Camera Initialized ==== ");
            ptp.setCameraListener(this);
            ptp.initialize(this, getIntent());
        }
    }

    @Override
    public AppSettings getSettings() {
        return settings;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppConfig.LOG) {
            Log.i(TAG, "onCreate");
        }

        mCon = this;
        backButtonCount=0;

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (!getResources().getConfiguration().isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_LARGE)) {
            getWindow()
                    .setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            isLarge = true;
        }
        setContentView(R.layout.main);

        MadamfiveAPI.setContext(this, getApplicationContext());

        settings = new AppSettings(this);

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, DSLRFragment.newInstance(), null);
        ft.addToBackStack(null);
        ft.commit();

        int appVersionCode = -1;
        try {
            appVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            // nop
        }

        if (settings.showChangelog(appVersionCode)) {
            showChangelog();
        }

        ptp = PtpService.Singleton.getInstance(this);

        if(SmartFiPreference.getDoctorId(this).equals(Constants.EMRAPI.UNDEFINED)
            || SmartFiPreference.getSfToken(this).equals(Constants.EMRAPI.UNDEFINED)){
            Log.w(TAG,"로그인되어 있지 않음");
            showLoginDialog();
        }else {
            Log.w(TAG,"자동로그인");
            MadamfiveAPI.loginDoctorKeeper(SmartFiPreference.getDoctorId(this), SmartFiPreference.getSfDoctorPw(this), new JsonHttpResponseHandler() {
                @Override
                public void onStart() {
                    Log.i(TAG, "onStart:");
                }
                @Override
                public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                    Log.i(TAG, "HTTPa:" + statusCode + responseString);
                    if(statusCode == 400 || statusCode == 401 || statusCode == 204 || statusCode == 403) {
                        Log.i(TAG, "auto login id/pw not matched.");
                    }else{
                        Log.i(TAG, "responseString:::"+responseString);
//                        SmartFiPreference.setSfToken(getActivity(),obj.getString("token"));
//                        Log.i(TAG,"token:::"+obj.getString("token"));
                    }
                }
                @Override
                public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    Log.w(TAG,"failed statusCode : " + statusCode);
                    Log.w(TAG,"failed headers : " + headers);
                    Log.w(TAG,"failed responseString : " + responseString);
                }
            });
        }

        countDownTimer = new MyCountDownTimer(startTime, interval);
        countDownTimer.start();

        fixedLandscapeExtraOption = SmartFiPreference.getSfDisplayLandscapeOpt(MadamfiveAPI.getActivity());
        if(fixedLandscapeExtraOption){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        }
    }

    public void showLoginDialog() {

        FragmentTransaction changelogTx = getFragmentManager().beginTransaction();
        LoginDialogFragment loginDialogFragment = LoginDialogFragment.newInstance();
        changelogTx.add(loginDialogFragment, "Login");
        changelogTx.commit();

    }

    private void showChangelog() {
        FragmentTransaction changelogTx = getFragmentManager().beginTransaction();
        WebViewDialogFragment changelogFragment = WebViewDialogFragment.newInstance(R.string.whats_new,
                "file:///android_asset/changelog/changelog.html");
        changelogTx.add(changelogFragment, "changelog");
        changelogTx.commit();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (AppConfig.LOG) {
            Log.i(TAG, "onNewIntent " + intent.getAction());
        }
        this.setIntent(intent);

        try {
            if (isInStart) {
                ptp.initialize(this, intent);
            }
        }catch(Exception e){
            Log.i(TAG,"ptp error"+e);
        }
//        isVrecording = intent.getBooleanExtra(VIDEO_RECORD, false);
//        Log.i(TAG,"isVrecording = "+isVrecording);

    }

    @Override
    public void onStart() {
        super.onStart();
        if (AppConfig.LOG) {
            Log.i(TAG, "onStart");
        }
        isInStart = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isInResume = true;
        Log.i(TAG, "MainActivity onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        isInResume = false;
//        Log.i(TAG, "onPause");
//        ptp.setCameraListener(null);
//        if (isFinishing()) {
//            ptp.shutdown();
//        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (AppConfig.LOG) {
            Log.i(TAG, "onStop");
        }
        isInStart = false;
        ptp.setCameraListener(null);
        if (isFinishing()) {
            ptp.shutdown();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (AppConfig.LOG) {
            Log.i(TAG, "onDestroy");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.main_menu, menu);
        return false;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_PROGRESS:
                return ProgressDialog.show(this, "", "Generating information. Please wait...", true);
            case DIALOG_NO_CAMERA:
                AlertDialog.Builder b = new AlertDialog.Builder(this);
                b.setTitle(R.string.dialog_no_camera_title);
                b.setMessage(R.string.dialog_no_camera_message);
                b.setNeutralButton(R.string.ok, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                return b.create();
        }
        return super.onCreateDialog(id);
    }

    public void onMenuFeedbackClicked(MenuItem item) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setPositiveButton(R.string.ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendDeviceInformation();
            }
        });
        b.setNegativeButton(R.string.cancel, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        b.setTitle(R.string.feedback_dialog_title);
        b.setMessage(R.string.feedback_dialog_message);
        b.show();
    }

    private void sendDeviceInformation() {
        showDialog(DIALOG_PROGRESS);
        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                File dir = getExternalCacheDir();
                final File out = dir != null ? new File(dir, "deviceinfo.txt") : null;

                if (camera != null) {
                    camera.writeDebugInfo(out);
                }

                final String shortDeviceInfo = out == null && camera != null ? camera.getDeviceInfo() : "unknown";

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.this.dismissDialog(DIALOG_PROGRESS);
                        Intent sendIntent = new Intent(Intent.ACTION_SEND);
                        sendIntent.setType("text/plain");
                        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "RYC USB Feedback");
                        sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"PUT_EMAIL_HERE"});
                        if (out != null && camera != null) {
                            sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + out.toString()));
                            sendIntent.putExtra(Intent.EXTRA_TEXT, "Any problems or feature whishes? Let us know: ");
                        } else {
                            sendIntent.putExtra(Intent.EXTRA_TEXT,
                                    "Any problems or feature whishes? Let us know: \n\n\n" + shortDeviceInfo);
                        }
                        startActivity(Intent.createChooser(sendIntent, "Email:"));
                    }
                });
            }
        });
        th.start();
    }

    public void onMenuChangelogClicked(MenuItem item) {
        showChangelog();
    }

    public void onMenuSettingsClicked(MenuItem item) {
        startActivity(new Intent(this, AppSettingsActivity.class));
    }

    public void onMenuAboutClicked(MenuItem item) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setNeutralButton(R.string.ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        View view = getLayoutInflater().inflate(R.layout.about_dialog, null);
        ((TextView) view.findViewById(R.id.about_dialog_version)).setText(getString(R.string.about_dialog_version,
                PackageUtil.getVersionName(this)));
        b.setView(view);
        b.show();
    }

    @Override
    public void onCameraStarted(Camera camera) {
        this.camera = camera;
//        if (AppConfig.LOG) {
            Log.i(TAG, "camera started");
//        }
        try {
            dismissDialog(DIALOG_NO_CAMERA);
        } catch (IllegalArgumentException e) {
        }
        camera.setCapturedPictureSampleSize(settings.getCapturedPictureSampleSize());
        sessionFrag.cameraStarted(camera);
    }

    @Override
    public void onCameraStopped(Camera camera) {
//        if (AppConfig.LOG) {
            Log.i(TAG, "camera stopped");
//        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.camera = null;
        sessionFrag.cameraStopped(camera);
    }

    @Override
    public void onNoCameraFound() {
        //showDialog(DIALOG_NO_CAMERA);
    }

    @Override
    public void onError(String message) {
        sessionFrag.enableUi(false);
        sessionFrag.cameraStopped(null);
//        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPropertyChanged(int property, int value) {
        //Log.i(TAG, "onPropertyChanged " + property + ":" + value);
        sessionFrag.propertyChanged(property, value);
    }

    @Override
    public void onPropertyStateChanged(int property, boolean enabled) {
        // TODO
    }

    @Override
    public void onPropertyDescChanged(int property, int[] values) {
        sessionFrag.propertyDescChanged(property, values);
    }

    @Override
    public void onLiveViewStarted() {
        sessionFrag.liveViewStarted();
//        Log.i(TAG,"=======>>>>>>sessionFrag.liveViewStarted()");
    }

    @Override
    public void onLiveViewStopped() {
        sessionFrag.liveViewStopped();
    }

    @Override
    public void onLiveViewData(LiveViewData data) {
        if (!isInResume) {
            return;
        }
        sessionFrag.liveViewData(data);
    }

    @Override
    public void onCapturedPictureReceived(int objectHandle, String filename, Bitmap thumbnail, Bitmap bitmap) {
//        Log.i(TAG, "onCapturedPictureReceived " + filename);
        if (thumbnail != null) {
            sessionFrag.capturedPictureReceived(objectHandle, filename, thumbnail, bitmap);
        } else {
            Toast.makeText(this, "No thumbnail available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBulbStarted() {
        sessionFrag.setCaptureBtnText("0");
    }

    @Override
    public void onBulbExposureTime(int seconds) {
        sessionFrag.setCaptureBtnText("" + seconds);
    }

    @Override
    public void onBulbStopped() {
        sessionFrag.setCaptureBtnText("Fire");
    }

    @Override
    public void onFocusStarted() {
        sessionFrag.focusStarted();
    }

    @Override
    public void onFocusEnded(boolean hasFocused) {
        sessionFrag.focusEnded(hasFocused);
    }

    @Override
    public void onFocusPointsChanged() {
        // TODO onFocusPointsToggleClicked(null);
    }

    @Override
    public void onObjectAdded(int handle, int format) {
        Log.i(TAG, "onObjectAdded " + handle + ":" + format);
        sessionFrag.objectAdded(handle, format);

//        if (camera == null) {
//            return;
//        }
//        if (format == PtpConstants.ObjectFormat.EXIF_JPEG) {
//            camera.retrieveImage(this, handle);
//        }


    }


//    @Override
//    public void onImageRetrieved(int objectHandle, Bitmap image) {
//        Log.i(TAG, "onImageRetrieved " + objectHandle);
//        Log.i(TAG, "onImageRetrieved " + image.getHeight());
//    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if(backButtonCount >= 1)
        {
//            Intent intent = new Intent(Intent.ACTION_MAIN);
//            intent.addCategory(Intent.CATEGORY_HOME);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent);
            finish();
            backButtonCount = 0;
        }
        else
        {
            Toast.makeText(this, "뒤로가기 버튼을 한번 더 누르면 종료됩니다", Toast.LENGTH_SHORT).show();
            backButtonCount++;
        }
    }


    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        Log.d(TAG,"isVrecording = "+isVrecording);
        if(!isVrecording){
            countDownTimer.cancel();
            countDownTimer.start();
        }

    }

    public class MyCountDownTimer extends CountDownTimer {
        public MyCountDownTimer(long startTime, long interval) {
            super(startTime, interval);
        }

        @Override
        public void onFinish() {
            Log.w(TAG,"Timer Completed");

            MadamfiveAPI.deleteImage();
            MadamfiveAPI.deletePhotoModelList();

            finish();
            System.exit(0);
        }

        @Override
        public void onTick(long millisUntilFinished) {
        }
    }



}

package com.doctorkeeper.dslrkeepervegas.services;

import android.content.Context;
import android.media.ExifInterface;
import android.util.Log;
import android.view.OrientationEventListener;

public class OrientationManager extends OrientationEventListener {

    public enum ScreenOrientation {
        REVERSED_LANDSCAPE, LANDSCAPE, PORTRAIT, REVERSED_PORTRAIT
    }

    private ScreenOrientation screenOrientation;
    private OrientationListener listener;

    public OrientationManager(Context context, int rate, OrientationListener listener) {
        super(context, rate);
        setListener(listener);

        // always init with rotate_1 orientation, because the main activity is actually fixed to
        // the rotate_1 view.
        // (without any initialization an error will occur if the device is not moved)
        screenOrientation = ScreenOrientation.PORTRAIT;
    }

    public OrientationManager(Context context, int rate) {
        super(context, rate);
    }

    public OrientationManager(Context context) {
        super(context);
    }

    @Override
    public void onOrientationChanged(int orientation) {
        if (orientation == -1){
            return;
        }
        ScreenOrientation newOrientation;
        if (orientation >= 60 && orientation <= 140){
            newOrientation = ScreenOrientation.REVERSED_LANDSCAPE;
        } else if (orientation >= 140 && orientation <= 220) {
            newOrientation = ScreenOrientation.REVERSED_PORTRAIT;
        } else if (orientation >= 220 && orientation <= 300) {
            newOrientation = ScreenOrientation.LANDSCAPE;
        } else {
            newOrientation = ScreenOrientation.PORTRAIT;
        }
        if(newOrientation != screenOrientation){
            screenOrientation = newOrientation;
            if(listener != null){
                listener.onOrientationChange(screenOrientation);
            }
        }
    }

    public void setListener(OrientationListener listener){
        this.listener = listener;
    }

    public ScreenOrientation getScreenOrientation(){
        return screenOrientation;
    }

    public interface OrientationListener {

        public void onOrientationChange(ScreenOrientation screenOrientation);
    }

    /**
     * Get the Exif version of getScreenOrientation.
     * @return
     */
    public int getExifOrientation(boolean frontCamera) {

        // the front camera rotates the landscape view by 180 degree
        if(frontCamera) {
            switch (screenOrientation) {
                case REVERSED_LANDSCAPE:
                    return ExifInterface.ORIENTATION_ROTATE_270;
                case REVERSED_PORTRAIT:
                    return ExifInterface.ORIENTATION_ROTATE_180;
                case LANDSCAPE:
                    return ExifInterface.ORIENTATION_ROTATE_90;
                case PORTRAIT:
                    return ExifInterface.ORIENTATION_NORMAL;
                default:
                    Log.w("ExifOrientation", "Invalid input");
                    return ExifInterface.ORIENTATION_NORMAL;
            }
        }
        else {
            switch (screenOrientation) {
                case REVERSED_LANDSCAPE:
                    return ExifInterface.ORIENTATION_ROTATE_90;
                case REVERSED_PORTRAIT:
                    return ExifInterface.ORIENTATION_ROTATE_180;
                case LANDSCAPE:
                    return ExifInterface.ORIENTATION_ROTATE_270;
                case PORTRAIT:
                    return ExifInterface.ORIENTATION_NORMAL;
                default:
                    Log.w("ExifOrientation", "Invalid input");
                    return ExifInterface.ORIENTATION_NORMAL;
            }
        }
    }
}
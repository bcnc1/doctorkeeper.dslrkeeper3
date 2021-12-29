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
package com.doctorkeeper.dslrkeepervegas.ptp.commands;

import android.graphics.Bitmap;
import android.util.Log;

import com.doctorkeeper.dslrkeepervegas.ptp.PtpAction;
import com.doctorkeeper.dslrkeepervegas.ptp.PtpCamera;
import com.doctorkeeper.dslrkeepervegas.ptp.PtpCamera.IO;
import com.doctorkeeper.dslrkeepervegas.ptp.PtpConstants;
import com.doctorkeeper.dslrkeepervegas.ptp.PtpConstants.Response;
import com.doctorkeeper.dslrkeepervegas.ptp.model.ObjectInfo;

public class RetrievePictureAction implements PtpAction {

    private final PtpCamera camera;
    private final int objectHandle;
    private final int sampleSize;
    private static final String TAG = RetrievePictureAction.class.getSimpleName();

    public RetrievePictureAction(PtpCamera camera, int objectHandle, int sampleSize) {
        this.camera = camera;
        this.objectHandle = objectHandle;
        this.sampleSize = sampleSize;
    }

    @Override
    public void exec(IO io) {
        GetObjectInfoCommand getInfo = new GetObjectInfoCommand(camera, objectHandle);
        io.handleCommand(getInfo);

        if (getInfo.getResponseCode() != Response.Ok) {
            return;
        }

        ObjectInfo objectInfo = getInfo.getObjectInfo();
        if (objectInfo == null) {
            return;
        }

        Bitmap thumbnail = null;
        if (objectInfo.thumbFormat == PtpConstants.ObjectFormat.JFIF
                || objectInfo.thumbFormat == PtpConstants.ObjectFormat.EXIF_JPEG) {
            Log.i(TAG, "BITMAP0:" + getInfo.getObjectInfo().filename);
            GetThumb getThumb = new GetThumb(camera, objectHandle);
            io.handleCommand(getThumb);
            if (getThumb.getResponseCode() == Response.Ok) {
                thumbnail = getThumb.getBitmap();
            }
        }

        GetObjectCommand getObject = new GetObjectCommand(camera, objectHandle, sampleSize);
        io.handleCommand(getObject);

        if (getObject.getResponseCode() != Response.Ok) {
            return;
        }
        if (getObject.getBitmap() == null) {

            if (getObject.isOutOfMemoryError()) {
                Log.i(TAG, "BITMAP1:" + getInfo.getObjectInfo().filename);
                camera.onPictureReceived(objectHandle, getInfo.getObjectInfo().filename, thumbnail, null);
            }
            return;
        }

        if (thumbnail == null) {
            // TODO resize real picture?
        }
        Log.i(TAG, "BITMAP2:" + getInfo.getObjectInfo().filename);
        camera.onPictureReceived(objectHandle, getInfo.getObjectInfo().filename, thumbnail, getObject.getBitmap());
    }

    @Override
    public void reset() {
    }
}

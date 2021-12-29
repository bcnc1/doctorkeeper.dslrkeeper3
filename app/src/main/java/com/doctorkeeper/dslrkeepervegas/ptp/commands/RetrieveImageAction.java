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
package com.doctorkeeper.dslrkeepervegas.ptp.commands;

import android.util.Log;

import com.doctorkeeper.dslrkeepervegas.ptp.Camera.RetrieveImageListener;
import com.doctorkeeper.dslrkeepervegas.ptp.PtpAction;
import com.doctorkeeper.dslrkeepervegas.ptp.PtpCamera;
import com.doctorkeeper.dslrkeepervegas.ptp.PtpCamera.IO;
import com.doctorkeeper.dslrkeepervegas.ptp.PtpConstants.Response;

public class RetrieveImageAction implements PtpAction {
    private final String TAG = RetrieveImageAction.class.getSimpleName();

    private final PtpCamera camera;
    private final int objectHandle;
    private final RetrieveImageListener listener;
    private final int sampleSize;

    public RetrieveImageAction(PtpCamera camera, RetrieveImageListener listener, int objectHandle, int sampleSize) {
        this.camera = camera;
        this.listener = listener;
        this.objectHandle = objectHandle;
        this.sampleSize = 1;
    }

    @Override
    public void exec(IO io) {
        Log.i(TAG, "sampleSize:"+sampleSize);
        GetObjectCommand getObject = new GetObjectCommand(camera, objectHandle, sampleSize);
        io.handleCommand(getObject);

        if (getObject.getResponseCode() != Response.Ok || getObject.getBitmap() == null) {
            listener.onImageRetrieved(0, null);
            return;
        }

        listener.onImageRetrieved(objectHandle, getObject.getBitmap());
    }

    @Override
    public void reset() {
    }
}

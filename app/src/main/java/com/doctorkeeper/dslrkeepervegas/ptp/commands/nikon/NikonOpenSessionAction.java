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
package com.doctorkeeper.dslrkeepervegas.ptp.commands.nikon;

import com.doctorkeeper.dslrkeepervegas.ptp.NikonCamera;
import com.doctorkeeper.dslrkeepervegas.ptp.PtpAction;
import com.doctorkeeper.dslrkeepervegas.ptp.PtpCamera.IO;
import com.doctorkeeper.dslrkeepervegas.ptp.PtpConstants;
import com.doctorkeeper.dslrkeepervegas.ptp.PtpConstants.Datatype;
import com.doctorkeeper.dslrkeepervegas.ptp.PtpConstants.Operation;
import com.doctorkeeper.dslrkeepervegas.ptp.PtpConstants.Property;
import com.doctorkeeper.dslrkeepervegas.ptp.commands.OpenSessionCommand;
import com.doctorkeeper.dslrkeepervegas.ptp.commands.SetDevicePropValueCommand;

public class NikonOpenSessionAction implements PtpAction {

    private final NikonCamera camera;

    public NikonOpenSessionAction(NikonCamera camera) {
        this.camera = camera;
    }

    @Override
    public void exec(IO io) {
        OpenSessionCommand openSession = new OpenSessionCommand(camera);
        io.handleCommand(openSession);
        if (openSession.getResponseCode() == PtpConstants.Response.Ok) {
            if (camera.hasSupportForOperation(Operation.NikonGetVendorPropCodes)) {
                NikonGetVendorPropCodesCommand getPropCodes = new NikonGetVendorPropCodesCommand(camera);
                io.handleCommand(getPropCodes);

                SetDevicePropValueCommand c;
                String deviceInfo = camera.getDeviceInfo();
                if(deviceInfo.contains("Nikon")&&(deviceInfo.contains("D5300")||deviceInfo.contains("D5500")||deviceInfo.contains("D5600")
                        ||deviceInfo.contains("D5100")||deviceInfo.contains("D5200")||deviceInfo.contains("D610")||deviceInfo.contains("D810")
                        ||deviceInfo.contains("D800")||deviceInfo.contains("D700")||deviceInfo.contains("D7200")
                        ||deviceInfo.contains("D3400")||deviceInfo.contains("D3500")
                )) {
                    c = new SetDevicePropValueCommand(camera, Property.NikonRecordingMedia, 2, Datatype.uint8);
//                } else if ( deviceInfo.contains("Nikon")&&(deviceInfo.contains("D3000"))) {
//                    c = new SetDevicePropValueCommand(camera, Property.NikonRecordingMedia, 3,Datatype.uint8);
                } else {
                    c = new SetDevicePropValueCommand(camera, Property.NikonRecordingMedia, 1,Datatype.uint8);
                }
//                // NikonRecordingMedia = 1 (in their original code)
//                SetDevicePropValueCommand c = new SetDevicePropValueCommand(camera, Property.NikonRecordingMedia, 2,Datatype.uint8);
                io.handleCommand(c);
                if (getPropCodes.getResponseCode() == PtpConstants.Response.Ok
                        && c.getResponseCode() == PtpConstants.Response.Ok) {
                    camera.setVendorPropCodes(getPropCodes.getPropertyCodes());
                    camera.onSessionOpened();
                } else {
                    camera.onPtpError(String.format(
                            "Couldn't read device property codes! Open main command failed with error code \"%s\"",
                            PtpConstants.responseToString(getPropCodes.getResponseCode())));
                }
            } else {
                camera.onSessionOpened();
            }
        } else {
            camera.onPtpError(String.format(
                    "Couldn't open main! Open main command failed with error code \"%s\"",
                    PtpConstants.responseToString(openSession.getResponseCode())));
        }
    }

    @Override
    public void reset() {
    }
}

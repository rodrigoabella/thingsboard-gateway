/**
 * Copyright © 2017 The Thingsboard Authors
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
package org.thingsboard.gateway.extensions.dlms;

import org.thingsboard.gateway.service.data.DeviceData;
import gurux.common.IGXMediaListener;
import gurux.common.MediaStateEventArgs;
import gurux.common.PropertyChangedEventArgs;
import gurux.common.ReceiveEventArgs;
import gurux.common.TraceEventArgs;
import gurux.common.enums.TraceLevel;
import gurux.dlms.enums.InterfaceType;
import gurux.dlms.secure.GXDLMSSecureNotify;
import gurux.net.GXNet;
import gurux.net.enums.NetworkType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DlmsServer extends GXDLMSSecureNotify implements IGXMediaListener, gurux.net.IGXNetListener, AutoCloseable {


    private GXNet media;
    private final DeviceDataController deviceDataController;

    public DlmsServer(DeviceDataController deviceDataController, int port) throws Exception {
        super(true, 1, 1, InterfaceType.WRAPPER);
        this.deviceDataController = deviceDataController;
        media = new gurux.net.GXNet(NetworkType.TCP, port);
        media.setTrace(TraceLevel.VERBOSE);
        media.addListener(this);
        media.open();
    }

    public void close() {
        media.close();
    }

    @Override
    public void onError(Object sender, Exception ex) {
        log.error("Error has occurred: {}", ex.getMessage());
    }

    @Override
    public void onReceived(Object sender, ReceiveEventArgs e) {
        
    }

    @Override
    public void onMediaStateChange(Object sender, MediaStateEventArgs e) {

    }

    @Override
    public void onClientConnected(Object sender, gurux.net.ConnectionEventArgs e) {
            log.info("Client Connected.");
            log.info("Client Info: {}", e.getInfo());
            
            DeviceData deviceData = deviceDataController.getDeviceData();
            try {
                deviceDataController.publishDeviceData(deviceData);
            } catch (Exception e1) {
                log.error("Device data could not be published");
            }
    }

    @Override
    public void onClientDisconnected(Object sender, gurux.net.ConnectionEventArgs e) {
        log.info("Client Disconnected.");
    }

    @Override
    public void onTrace(Object sender, TraceEventArgs e) {
    
    }

    @Override
    public void onPropertyChanged(Object sender, PropertyChangedEventArgs e) {

    }
}
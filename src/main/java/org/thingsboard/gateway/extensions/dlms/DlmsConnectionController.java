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

import lombok.extern.slf4j.Slf4j;
import org.thingsboard.gateway.extensions.dlms.conf.DlmsConfiguration;
import org.thingsboard.gateway.service.gateway.GatewayService;

@Slf4j
public class DlmsConnectionController {
    private final GatewayService gateway;
    private final DlmsConfiguration dlmsConfiguration;
    private DlmsServer dlmsServer;

    public DlmsConnectionController(GatewayService gateway, DlmsConfiguration dlmsConfiguration) {
        this.gateway = gateway;
        this.dlmsConfiguration = dlmsConfiguration;
    }

    public void createDlmsServer() {
        log.info("[{}] Creating Dlms server on port: [{}]", gateway.getTenantLabel(), dlmsConfiguration.getPortNumber());
        DeviceDataController deviceDataController = new DeviceDataController(gateway);
        try {
            this.dlmsServer = new DlmsServer(deviceDataController, dlmsConfiguration.getPortNumber());
            log.info("[{}] Dlms server created!", gateway.getTenantLabel());
        } catch (Exception e) {
            log.error(
                    "[{}] Dlms server could not be created, on port: [{}]!. Error: {}", 
                    gateway.getTenantLabel(),
                    dlmsConfiguration.getPortNumber(),
                    e.getMessage());
        }
    }
    
    public void closeDlmsServer() {
        this.dlmsServer.close();
        log.info("[{}] Dlms server on port: [{}] stopped!", gateway.getTenantLabel(), dlmsConfiguration.getPortNumber());
    }

}

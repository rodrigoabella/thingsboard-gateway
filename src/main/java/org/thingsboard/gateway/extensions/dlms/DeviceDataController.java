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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.thingsboard.gateway.service.MqttDeliveryFuture;
import org.thingsboard.gateway.service.data.DeviceData;
import org.thingsboard.gateway.service.gateway.GatewayService;
import org.thingsboard.server.common.data.kv.BasicTsKvEntry;
import org.thingsboard.server.common.data.kv.DoubleDataEntry;
import org.thingsboard.server.common.data.kv.KvEntry;
import org.thingsboard.server.common.data.kv.StringDataEntry;
import org.thingsboard.server.common.data.kv.TsKvEntry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DeviceDataController {
    
    private final GatewayService gateway;
    private static final int OPERATION_TIMEOUT_IN_SEC = 10;
    
    public DeviceDataController(GatewayService gateway) {
        this.gateway = gateway;
    }

    public void publishDeviceData(DeviceData deviceData) throws Exception {
        if (deviceData != null) {
            waitWithTimeout(gateway.onDeviceConnect(deviceData.getName(),
                    deviceData.getType()));
            List<MqttDeliveryFuture> futures = new ArrayList<>();
            if (!deviceData.getAttributes().isEmpty()) {
                futures.add(gateway.onDeviceAttributesUpdate(
                        deviceData.getName(), deviceData.getAttributes()));
            }
            if (!deviceData.getTelemetry().isEmpty()) {
                futures.add(gateway.onDeviceTelemetry(deviceData.getName(),
                        deviceData.getTelemetry()));
            }
            for (MqttDeliveryFuture future : futures) {
                waitWithTimeout(future);
            }
            Optional<MqttDeliveryFuture> future = gateway
                    .onDeviceDisconnect(deviceData.getName());
            if (future.isPresent()) {
                waitWithTimeout(future.get());
            }
        } else {
            log.error(
                    "[{}] DeviceData is null. it was not parsed successfully!",
                    gateway.getTenantLabel());
        }
    }

    public DeviceData getDeviceData() {
        String type = "Hexing";
        String serialNumber = "1";
        String name = type + ": " + serialNumber;

        List<KvEntry> attributes = new ArrayList<>();
        KvEntry kvEntry1 = new StringDataEntry("tipoMedidor", type);
        KvEntry kvEntry2 = new StringDataEntry("numeroDeSerie", serialNumber);
        attributes.add(kvEntry1);
        attributes.add(kvEntry2);

        List<TsKvEntry> telemetry = new ArrayList<>();
        KvEntry kvEntry3 = new DoubleDataEntry("energiaActiva", 3.9D);
        long ts = System.currentTimeMillis();
        TsKvEntry tsKvEntry1 = new BasicTsKvEntry(ts, kvEntry3);
        telemetry.add(tsKvEntry1);

        return new DeviceData(name, type, attributes, telemetry);
    }

    private void waitWithTimeout(MqttDeliveryFuture future) throws Exception {
        future.get(OPERATION_TIMEOUT_IN_SEC, TimeUnit.SECONDS);
    }

}
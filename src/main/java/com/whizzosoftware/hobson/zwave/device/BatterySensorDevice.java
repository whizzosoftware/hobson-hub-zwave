/*
 *******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.zwave.device;

import com.whizzosoftware.hobson.api.property.PropertyContainer;
import com.whizzosoftware.hobson.api.variable.HobsonVariable;
import com.whizzosoftware.hobson.api.variable.VariableContext;
import com.whizzosoftware.hobson.api.variable.VariableUpdate;
import com.whizzosoftware.hobson.zwave.ZWaveContext;
import com.whizzosoftware.hobson.zwave.ZWavePlugin;
import com.whizzosoftware.wzwave.commandclass.MultiInstanceCommandClass;
import com.whizzosoftware.wzwave.node.ZWaveEndpoint;
import com.whizzosoftware.wzwave.node.specific.RoutingBinarySensor;

import java.util.List;

/**
 * A battery-powered sensor device.
 *
 * @author Dan Noguerol
 */
public class BatterySensorDevice extends SensorDevice {
    public BatterySensorDevice(ZWavePlugin driver, String id, ZWaveEndpoint node, Byte endpointNumber, String name, MultiInstanceCommandClass micc) {
        super(driver, id, node, name, endpointNumber, micc);
        setDefaultName(createManufacturerDeviceName(node, name != null ? name : "Unknown Battery-powered Sensor"));
    }

    @Override
    public void onStartup(PropertyContainer config) {
        publishVariable("batteryLevel", getInitialValue("batteryLevel"), HobsonVariable.Mask.READ_ONLY, getInitialValueUpdateTime("batteryLevel"));
        super.onStartup(config);
    }

    @Override
    public void onShutdown() {}

    @Override
    public void onUpdate(ZWaveEndpoint endpoint, List<VariableUpdate> updates) {
        super.onUpdate(endpoint, updates);
        if (endpoint instanceof RoutingBinarySensor) {
            RoutingBinarySensor sensor = (RoutingBinarySensor) endpoint;
            updates.add(new VariableUpdate(VariableContext.create(getContext(), "batteryLevel"), sensor.getBatteryLevel()));
        }
    }
}

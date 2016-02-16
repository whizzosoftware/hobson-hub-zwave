/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.zwave.device;

import com.whizzosoftware.hobson.api.device.DeviceType;
import com.whizzosoftware.hobson.api.property.PropertyContainer;
import com.whizzosoftware.hobson.api.property.TypedProperty;
import com.whizzosoftware.hobson.api.variable.HobsonVariable;
import com.whizzosoftware.hobson.api.variable.VariableConstants;
import com.whizzosoftware.hobson.api.variable.VariableContext;
import com.whizzosoftware.hobson.api.variable.VariableUpdate;
import com.whizzosoftware.hobson.zwave.ZWavePlugin;
import com.whizzosoftware.wzwave.node.ZWaveEndpoint;
import com.whizzosoftware.wzwave.node.generic.BinarySensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * A sensor device.
 *
 * @author Dan Noguerol
 */
public class SensorDevice extends HobsonZWaveDevice {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public SensorDevice(ZWavePlugin driver, String id, ZWaveEndpoint node, Byte endpointNumber, String name) {
        super(driver, id, node.getNodeId(), endpointNumber);
        setDefaultName(createManufacturerDeviceName(node, name != null ? name : "Unknown Sensor"));
    }

    @Override
    public void onStartup(PropertyContainer config) {
        publishVariable(VariableConstants.ON, getInitialValue(VariableConstants.ON), HobsonVariable.Mask.READ_ONLY, getInitialValueUpdateTime(VariableConstants.ON));
        super.onStartup(config);
    }

    @Override
    public void onShutdown() {}

    @Override
    public void onUpdate(ZWaveEndpoint endpoint, List<VariableUpdate> updates) {
        logger.debug("Got Z-Wave device update: " + endpoint);
        if (endpoint instanceof BinarySensor) {
            BinarySensor sensor = (BinarySensor)endpoint;
            updates.add(new VariableUpdate(VariableContext.create(getContext(), VariableConstants.ON), !sensor.isSensorIdle()));
        }
    }

    @Override
    public String getPreferredVariableName() {
        return VariableConstants.ON;
    }

    @Override
    public DeviceType getType() {
        return DeviceType.SENSOR;
    }

    @Override
    public void onSetVariable(String name, Object value) {}

    @Override
    protected TypedProperty[] createSupportedProperties() {
        return null;
    }
}

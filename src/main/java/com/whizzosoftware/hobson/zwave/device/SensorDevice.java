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
import com.whizzosoftware.hobson.zwave.ZWaveContext;
import com.whizzosoftware.hobson.zwave.ZWavePlugin;
import com.whizzosoftware.wzwave.commandclass.MeterCommandClass;
import com.whizzosoftware.wzwave.node.ZWaveEndpoint;
import com.whizzosoftware.wzwave.node.generic.BinarySensor;
import com.whizzosoftware.wzwave.node.generic.Meter;
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

    public SensorDevice(ZWavePlugin plugin, String id, ZWaveEndpoint node, Byte endpointNumber, String name) {
        super(plugin, id, node, endpointNumber);
        setDefaultName(createManufacturerDeviceName(node, name != null ? name : "Unknown Sensor"));
    }

    @Override
    public void onStartup(PropertyContainer config) {
        publishVariable(getPrimaryVariable(), getInitialValue(getPrimaryVariable()), HobsonVariable.Mask.READ_ONLY, getInitialValueUpdateTime(getPrimaryVariable()));
        super.onStartup(config);
    }

    @Override
    public void onRefresh(ZWaveContext ctx) {
        if (getEndpoint() instanceof Meter) {
            MeterCommandClass mcc = (MeterCommandClass)getEndpoint().getCommandClass(MeterCommandClass.ID);
            if (mcc.getMeterType() == MeterCommandClass.MeterType.Electric) {
                ctx.sendDataFrame(mcc.createGet(getNodeId(), MeterCommandClass.SCALE_ELECTRIC_W));
            }
        }
    }

    @Override
    public void onShutdown() {}

    @Override
    public void onUpdate(ZWaveEndpoint endpoint, List<VariableUpdate> updates) {
        logger.debug("Got Z-Wave device update: " + endpoint);
        if (endpoint instanceof BinarySensor) {
            BinarySensor sensor = (BinarySensor)endpoint;
            updates.add(new VariableUpdate(VariableContext.create(getContext(), VariableConstants.ON), !sensor.isSensorIdle()));
        } else if (endpoint instanceof Meter && hasPrimaryVariable()) {
            Meter meter = (Meter)endpoint;
            if (meter.hasCommandClass(MeterCommandClass.ID)) {
                MeterCommandClass mcc = (MeterCommandClass)meter.getCommandClass(MeterCommandClass.ID);
                updates.add(new VariableUpdate(VariableContext.create(getContext(), getPrimaryVariable()), mcc.getCurrentValue()));
            }
        } else {
            logger.debug("Ignoring update for endpoint: {}", endpoint);
        }
    }

    @Override
    public String getPreferredVariableName() {
        return getPrimaryVariable();
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

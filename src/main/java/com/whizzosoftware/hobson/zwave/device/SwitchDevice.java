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

import com.whizzosoftware.hobson.api.device.DeviceType;
import com.whizzosoftware.hobson.api.property.PropertyContainer;
import com.whizzosoftware.hobson.api.property.TypedProperty;
import com.whizzosoftware.hobson.api.variable.HobsonVariable;
import com.whizzosoftware.hobson.api.variable.VariableConstants;
import com.whizzosoftware.hobson.api.variable.VariableContext;
import com.whizzosoftware.hobson.api.variable.VariableUpdate;
import com.whizzosoftware.hobson.zwave.ZWavePlugin;
import com.whizzosoftware.wzwave.commandclass.BinarySwitchCommandClass;
import com.whizzosoftware.wzwave.commandclass.MeterCommandClass;
import com.whizzosoftware.wzwave.commandclass.MultiInstanceCommandClass;
import com.whizzosoftware.wzwave.node.ZWaveEndpoint;
import com.whizzosoftware.wzwave.node.generic.BinarySwitch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * A switchable device.
 *
 * @author Dan Noguerol
 */
public class SwitchDevice extends HobsonZWaveDevice {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private boolean hasMeter = false;

    public SwitchDevice(ZWavePlugin zwavePlugin, String id, ZWaveEndpoint node, String name, Byte endpointNumber, MultiInstanceCommandClass micc) {
        super(zwavePlugin, id, node, endpointNumber, micc);
        setDefaultName(createManufacturerDeviceName(node, name != null ? name : "Unknown Switch"));
        hasMeter = node.hasCommandClass(MeterCommandClass.ID);
    }

    @Override
    public void onStartup(PropertyContainer config) {
        publishVariable(VariableConstants.ON, getInitialValue(VariableConstants.ON), HobsonVariable.Mask.READ_WRITE, getInitialValueUpdateTime(VariableConstants.ON));
        if (hasMeter) {
            publishVariable(VariableConstants.ENERGY_CONSUMPTION_WATTS, getInitialValue(VariableConstants.ENERGY_CONSUMPTION_WATTS), HobsonVariable.Mask.READ_ONLY, getInitialValueUpdateTime(VariableConstants.ENERGY_CONSUMPTION_WATTS));
        }
        super.onStartup(config);
    }

    @Override
    public void onShutdown() {}

    @Override
    public void onUpdate(ZWaveEndpoint endpoint, List<VariableUpdate> updates) {
        logger.debug("Got Z-Wave device update for {}: {}", endpoint, updates);

        // update on/off
        if (endpoint.getGenericDeviceClass() == BinarySwitch.ID) {
            Boolean isOn = BinarySwitch.isOn(endpoint);
            if (isOn != null) {
                updates.add(new VariableUpdate(VariableContext.create(getContext(), VariableConstants.ON), isOn));
            }
        }

        // if the device supports a meter, update it's energy consumption in watts
        if (hasMeter) {
            MeterCommandClass mcc = (MeterCommandClass)endpoint.getCommandClass(MeterCommandClass.ID);
            updates.add(new VariableUpdate(VariableContext.create(getContext(), VariableConstants.ENERGY_CONSUMPTION_WATTS), mcc.getCurrentValue()));
        }
    }

    @Override
    public DeviceType getType() {
        return DeviceType.SWITCH;
    }

    @Override
    public String getPreferredVariableName() {
        return VariableConstants.ON;
    }

    @Override
    protected TypedProperty[] createSupportedProperties() {
        return null;
    }

    @Override
    public void onSetVariable(String name, Object value) {
        BinarySwitchCommandClass bscc = (BinarySwitchCommandClass)getEndpoint().getCommandClass(BinarySwitchCommandClass.ID);
        if (VariableConstants.ON.equals(name) && ("true".equals(value) || value.equals(true))) {
            logger.debug("Sending switch on to " + getContext());
            if (isEndpoint()) {
                getZWaveDriver().getZWaveController().sendDataFrame(
                        getMultiInstanceCommandClass().createMultiChannelCommandEncapsulation(
                        (byte)0,
                        getEndpointNumber(),
                            bscc.createSet(getNodeId(), true),
                        false
                    )
                );
                getZWaveDriver().getZWaveController().sendDataFrame(
                    getMultiInstanceCommandClass().createMultiChannelCommandEncapsulation(
                        (byte)0,
                        getEndpointNumber(),
                            bscc.createGet(getNodeId()),
                        false
                    )
                );
            } else {
                getZWaveDriver().getZWaveController().sendDataFrame(bscc.createSet(getNodeId(), true));
                getZWaveDriver().getZWaveController().sendDataFrame(bscc.createGet(getNodeId()));
            }
        } else if (VariableConstants.ON.equals(name) && ("false".equals(value) || value.equals(false))) {
            logger.debug("Sending switch off to " + getContext());
            if (isEndpoint()) {
                getZWaveDriver().getZWaveController().sendDataFrame(
                    getMultiInstanceCommandClass().createMultiChannelCommandEncapsulation(
                        (byte)0,
                        getEndpointNumber(),
                        bscc.createSet(getNodeId(), false),
                        false
                    )
                );
                getZWaveDriver().getZWaveController().sendDataFrame(
                    getMultiInstanceCommandClass().createMultiChannelCommandEncapsulation(
                        (byte)0,
                        getEndpointNumber(),
                        bscc.createGet(getNodeId()),
                        false
                    )
                );
            } else {
                getZWaveDriver().getZWaveController().sendDataFrame(bscc.createSet(getNodeId(), false));
                getZWaveDriver().getZWaveController().sendDataFrame(bscc.createGet(getNodeId()));
            }
        }
    }
}

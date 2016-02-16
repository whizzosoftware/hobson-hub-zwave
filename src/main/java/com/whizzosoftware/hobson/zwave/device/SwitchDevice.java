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
import com.whizzosoftware.wzwave.commandclass.BinarySwitchCommandClass;
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

    public SwitchDevice(ZWavePlugin zwavePlugin, String id, ZWaveEndpoint node, Byte endpointNumber, String name) {
        super(zwavePlugin, id, node.getNodeId(), endpointNumber);
        setDefaultName(createManufacturerDeviceName(node, name != null ? name : "Unknown Switch"));
    }

    @Override
    public void onStartup(PropertyContainer config) {
        publishVariable(VariableConstants.ON, getInitialValue(VariableConstants.ON), HobsonVariable.Mask.READ_WRITE, getInitialValueUpdateTime(VariableConstants.ON));
        super.onStartup(config);
    }

    @Override
    public void onShutdown() {}

    @Override
    public void onUpdate(ZWaveEndpoint endpoint, List<VariableUpdate> updates) {
        logger.debug("Got Z-Wave device update: " + endpoint);
        if (endpoint.getGenericDeviceClass() == BinarySwitch.ID) {
            Boolean isOn = BinarySwitch.isOn(endpoint);
            if (isOn != null) {
                updates.add(new VariableUpdate(VariableContext.create(getContext(), VariableConstants.ON), isOn));
            }
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
        if (VariableConstants.ON.equals(name) && ("true".equals(value) || value.equals(true))) {
            logger.debug("Sending switch on to " + getContext());
            if (isEndpoint()) {
                getZWaveDriver().getZWaveController().sendDataFrame(
                    MultiInstanceCommandClass.createMultiChannelCmdEncapv2(
                        (byte)0,
                        getEndpointNumber(),
                        BinarySwitchCommandClass.createSetv1(getNodeId(), true),
                        false
                    )
                );
                getZWaveDriver().getZWaveController().sendDataFrame(
                    MultiInstanceCommandClass.createMultiChannelCmdEncapv2(
                        (byte)0,
                        getEndpointNumber(),
                        BinarySwitchCommandClass.createGetv1(getNodeId()),
                        false
                    )
                );
            } else {
                getZWaveDriver().getZWaveController().sendDataFrame(BinarySwitchCommandClass.createSetv1(getNodeId(), true));
                getZWaveDriver().getZWaveController().sendDataFrame(BinarySwitchCommandClass.createGetv1(getNodeId()));
            }
        } else if (VariableConstants.ON.equals(name) && ("false".equals(value) || value.equals(false))) {
            logger.debug("Sending switch off to " + getContext());
            if (isEndpoint()) {
                getZWaveDriver().getZWaveController().sendDataFrame(
                    MultiInstanceCommandClass.createMultiChannelCmdEncapv2(
                        (byte)0,
                        getEndpointNumber(),
                        BinarySwitchCommandClass.createSetv1(getNodeId(), false),
                        false
                    )
                );
                getZWaveDriver().getZWaveController().sendDataFrame(
                    MultiInstanceCommandClass.createMultiChannelCmdEncapv2(
                        (byte)0,
                        getEndpointNumber(),
                        BinarySwitchCommandClass.createGetv1(getNodeId()),
                        false
                    )
                );
            } else {
                getZWaveDriver().getZWaveController().sendDataFrame(BinarySwitchCommandClass.createSetv1(getNodeId(), false));
                getZWaveDriver().getZWaveController().sendDataFrame(BinarySwitchCommandClass.createGetv1(getNodeId()));
            }
        }
    }
}
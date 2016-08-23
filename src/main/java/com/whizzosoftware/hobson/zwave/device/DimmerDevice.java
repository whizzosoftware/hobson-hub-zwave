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
import com.whizzosoftware.hobson.zwave.ZWaveContext;
import com.whizzosoftware.hobson.zwave.ZWavePlugin;
import com.whizzosoftware.wzwave.commandclass.MultiInstanceCommandClass;
import com.whizzosoftware.wzwave.commandclass.MultilevelSwitchCommandClass;
import com.whizzosoftware.wzwave.node.ZWaveEndpoint;
import com.whizzosoftware.wzwave.node.generic.MultilevelSwitch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * A dimmer device.
 *
 * @author Dan Noguerol
 */
public class DimmerDevice extends HobsonZWaveDevice {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public DimmerDevice(ZWavePlugin zwavePlugin, String id, ZWaveEndpoint node, String name, Byte endpointNumber, MultiInstanceCommandClass micc) {
        super(zwavePlugin, id, node, endpointNumber, micc);
        setDefaultName(createManufacturerDeviceName(node, name != null ? name : "Unknown Dimmer"));
    }

    public void onStartup(PropertyContainer config) {
        logger.debug("Publishing level variable with initial value: {}", getInitialValue(VariableConstants.LEVEL));

        Byte initialLevel = null;
        Object o = getInitialValue(VariableConstants.LEVEL);
        if (o != null) {
            initialLevel = Byte.parseByte(o.toString());
        }

        publishVariable(VariableConstants.ON, (initialLevel != null && initialLevel > 0), HobsonVariable.Mask.READ_WRITE, getInitialValueUpdateTime(VariableConstants.LEVEL));
        publishVariable(VariableConstants.LEVEL, initialLevel, HobsonVariable.Mask.READ_WRITE, getInitialValueUpdateTime(VariableConstants.LEVEL));

        super.onStartup(config);
    }

    @Override
    protected TypedProperty[] createSupportedProperties() {
        return null;
    }

    public void onShutdown() {}

    @Override
    public void onUpdate(ZWaveEndpoint endpoint, List<VariableUpdate> updates) {
        logger.debug("Got Z-Wave device update for {}: {}", endpoint, updates);
        if (endpoint instanceof MultilevelSwitch) {
            MultilevelSwitch sw = (MultilevelSwitch) endpoint;
            updates.add(new VariableUpdate(VariableContext.create(getContext(), VariableConstants.LEVEL), sw.getLevel()));
            updates.add(new VariableUpdate(VariableContext.create(getContext(), VariableConstants.ON), (sw.getLevel() != null && sw.getLevel() > 0)));
        }
    }

    @Override
    public DeviceType getType() {
        return DeviceType.LIGHTBULB;
    }

    @Override
    public void onSetVariable(String name, Object value) {
        MultilevelSwitchCommandClass mscc = (MultilevelSwitchCommandClass)getEndpoint().getCommandClass(MultilevelSwitchCommandClass.ID);
        if (VariableConstants.LEVEL.equals(name)) {
            Byte level = Byte.parseByte(value.toString());
            getZWaveDriver().getZWaveController().sendDataFrame(mscc.createSet(getNodeId(), level));
            getZWaveDriver().getZWaveController().sendDataFrame(mscc.createGet(getNodeId()));
        }
    }
}

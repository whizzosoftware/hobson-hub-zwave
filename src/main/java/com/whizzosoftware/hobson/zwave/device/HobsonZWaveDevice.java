/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.zwave.device;

import com.whizzosoftware.hobson.api.device.AbstractHobsonDevice;
import com.whizzosoftware.hobson.api.property.PropertyContainer;
import com.whizzosoftware.hobson.api.variable.VariableUpdate;
import com.whizzosoftware.hobson.zwave.ZWavePlugin;
import com.whizzosoftware.wzwave.commandclass.ManufacturerSpecificCommandClass;
import com.whizzosoftware.wzwave.node.ZWaveEndpoint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract base class for all Hobson Z-Wave devices.
 *
 * @author Dan Noguerol
 */
abstract public class HobsonZWaveDevice extends AbstractHobsonDevice {
    private byte nodeId;
    private Byte endpointNumber;
    private Map<String,Object> initialValues;

    public HobsonZWaveDevice(ZWavePlugin driver, String id, byte nodeId, Byte endpointNumber) {
        super(driver, id);
        this.nodeId = nodeId;
        this.endpointNumber = endpointNumber;
    }

    @Override
    public void onStartup(PropertyContainer config) {
        super.onStartup(config);
        clearInitialValues();
    }

    public byte getNodeId() {
        return nodeId;
    }

    public Byte getEndpointNumber() {
        return endpointNumber;
    }

    public boolean isEndpoint() {
        return (endpointNumber != null);
    }

    public void processUpdate(ZWaveEndpoint endpoint, List<VariableUpdate> updates) {
        // if the device is started, we process the update;
        if (isStarted()) {
            onUpdate(endpoint, updates);
            setDeviceAvailability(true, System.currentTimeMillis());
        // otherwise, squirrel away the values and use as initial values when the device is finally started
        } else {
            if (initialValues == null) {
                initialValues = new HashMap<>();
            }
            for (VariableUpdate vu : updates) {
                initialValues.put(vu.getContext().getName(), vu.getValue());
            }
        }
    }

    abstract public void onUpdate(ZWaveEndpoint endpoint, List<VariableUpdate> updates);

    protected Object getInitialValue(String varName) {
        return initialValues != null ? initialValues.get(varName) : null;
    }

    protected Long getInitialValueUpdateTime(String varName) {
        return initialValues != null && initialValues.containsKey(varName) ? System.currentTimeMillis() : null;
    }

    protected void clearInitialValues() {
        initialValues = null;
    }

    protected ZWavePlugin getZWaveDriver() {
        return (ZWavePlugin)getPlugin();
    }

    protected String createManufacturerDeviceName(ZWaveEndpoint node, String defaultName) {
        ManufacturerSpecificCommandClass mscc = (ManufacturerSpecificCommandClass)node.getCommandClass(ManufacturerSpecificCommandClass.ID);
        if (mscc != null && mscc.getProductInfo() != null) {
            return mscc.getProductInfo().toString();
        } else {
            return defaultName;
        }
    }
}

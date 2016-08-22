/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.zwave.state;

import com.whizzosoftware.hobson.api.device.HobsonDevice;
import com.whizzosoftware.hobson.api.variable.VariableConstants;
import com.whizzosoftware.hobson.api.variable.VariableUpdate;
import com.whizzosoftware.hobson.zwave.device.*;
import com.whizzosoftware.hobson.zwave.util.DeviceUtil;
import com.whizzosoftware.wzwave.commandclass.MeterCommandClass;
import com.whizzosoftware.wzwave.commandclass.MultiInstanceCommandClass;
import com.whizzosoftware.wzwave.controller.ZWaveControllerListener;
import com.whizzosoftware.wzwave.node.ZWaveEndpoint;
import com.whizzosoftware.wzwave.node.ZWaveMultiChannelEndpoint;
import com.whizzosoftware.wzwave.node.generic.*;
import com.whizzosoftware.wzwave.node.specific.PCController;
import com.whizzosoftware.wzwave.node.specific.RoutingBinarySensor;
import com.whizzosoftware.wzwave.node.specific.SimpleMeter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class RunningState implements State {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ZWaveControllerListener listener;

    public RunningState(ZWaveControllerListener listener) {
        this.listener = listener;
    }

    @Override
    public void onRun(StateContext context) {
    }

    @Override
    public void onSerialPortUpdate(StateContext context) {
        context.setState(new InitializingState(listener));
    }

    @Override
    public void onZWaveNodeAdded(StateContext context, ZWaveEndpoint node) {
        if (node instanceof AlarmSensor) {
            logger.debug("Adding Alarm sensor");
            context.createZWaveDevice(SensorDevice.class, node, null, "Alarm Sensor", null);
        } else if (node instanceof RoutingBinarySensor) {
            logger.debug("Adding Battery-powered Sensor device");
            context.createZWaveDevice(BatterySensorDevice.class, node, null, null, null);
        } else if (node instanceof BinarySensor) {
            logger.debug("Adding Sensor device");
            context.createZWaveDevice(SensorDevice.class, node, null, null, null);
        } else if (node instanceof BinarySwitch) {
            logger.debug("Adding Switch device");
            context.createZWaveDevice(SwitchDevice.class, node, null, null, null);
        } else if (node instanceof Meter) {
            logger.debug("Adding Meter device");
            MeterCommandClass mcc = (MeterCommandClass)node.getCommandClass(MeterCommandClass.ID);
            switch (mcc.getMeterType()) {
                case Electric:
                    context.createZWaveDevice(SensorDevice.class, node, null, null, VariableConstants.ENERGY_CONSUMPTION_WATTS);
                    break;
                default:
                    logger.warn("Ignoring unsupported meter type: " + mcc.getMeterType());
                    break;
            }
        } else if (node instanceof MultilevelSwitch) {
            logger.debug("Adding Dimmer device");
            context.createZWaveDevice(DimmerDevice.class, node, null, null, null);
        } else if (node instanceof PCController) {
            logger.debug("Ignoring PC Controller");
        } else {
            logger.warn("Unsupported Z-Wave device found: " + node);
        }
    }

    @Override
    public void onZWaveNodeUpdated(StateContext context, ZWaveEndpoint node) {
        boolean checkEndpoints = false;
        String deviceId;
        if (node instanceof ZWaveMultiChannelEndpoint) {
            deviceId = DeviceUtil.getDeviceIdForNodeIdAndEndpoint(node.getNodeId(), ((ZWaveMultiChannelEndpoint) node).getNumber());
        } else {
            deviceId = DeviceUtil.getDeviceIdForNodeId(node.getNodeId());
            checkEndpoints = true;
        }
        try {
            HobsonDevice d = context.getZWaveDevice(deviceId);
            if (d != null && d instanceof HobsonZWaveDevice) {
                List<VariableUpdate> updates = new ArrayList<>();

                // update device
                ((HobsonZWaveDevice)d).processUpdate(node, updates);

                if (checkEndpoints) {
                    MultiInstanceCommandClass micc = (MultiInstanceCommandClass)node.getCommandClass(MultiInstanceCommandClass.ID);
                    if (micc != null) {
                        for (ZWaveMultiChannelEndpoint endpoint2 : micc.getEndpoints()) {
                            String deviceId2 = DeviceUtil.getDeviceIdForNodeIdAndEndpoint(node.getNodeId(), endpoint2.getNumber());
                            HobsonZWaveDevice d2 = (HobsonZWaveDevice)context.getZWaveDevice(deviceId2);
                            if (d2 != null) {
                                d2.processUpdate(endpoint2, updates);
                            }
                        }
                    }
                }

                // notify of variable updates
                if (updates.size() > 0) {
                    context.fireVariableUpdateNotifications(updates);
                }
            }
        } catch (Exception e) {
            logger.error("Error updating device", e);
        }
    }

    @Override
    public void stop(StateContext context) {
        if (context.getZWaveController() != null) {
            context.getZWaveController().stop();
        }
    }
}

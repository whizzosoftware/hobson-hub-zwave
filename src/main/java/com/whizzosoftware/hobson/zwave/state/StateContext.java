/*
 *******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.zwave.state;

import com.whizzosoftware.hobson.api.device.HobsonDevice;
import com.whizzosoftware.hobson.api.plugin.PluginStatus;
import com.whizzosoftware.hobson.api.variable.VariableUpdate;
import com.whizzosoftware.wzwave.commandclass.MultiInstanceCommandClass;
import com.whizzosoftware.wzwave.controller.ZWaveController;
import com.whizzosoftware.wzwave.controller.ZWaveControllerListener;
import com.whizzosoftware.wzwave.node.ZWaveEndpoint;

import java.util.List;

/**
 * Interface that represents all the actions that a state can perform.
 *
 * @author Dan Noguerol
 */
public interface StateContext extends ZWaveControllerListener {
    String getSerialPort();
    ZWaveController getZWaveController();
    void setZWaveController(ZWaveController controller);
    void setPluginStatus(PluginStatus status);
    void setState(State state);
    HobsonDevice getZWaveDevice(String deviceId);
    void createZWaveDevice(Class clazz, ZWaveEndpoint endpoint, String name, String primaryVariable);
    void createZWaveDevice(Class clazz, ZWaveEndpoint endpoint, String name, String primaryVariable, Byte endpointNumber, MultiInstanceCommandClass micc);
    void fireVariableUpdateNotifications(List<VariableUpdate> updates);
}

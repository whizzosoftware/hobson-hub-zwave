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

import com.whizzosoftware.hobson.api.plugin.PluginStatus;
import com.whizzosoftware.wzwave.controller.ZWaveControllerListener;
import com.whizzosoftware.wzwave.node.ZWaveEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FailedState implements State {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ZWaveControllerListener listener;
    private PluginStatus status;

    public FailedState(ZWaveControllerListener listener, PluginStatus status) {
        this.listener = listener;
        this.status = status;
    }

    @Override
    public void onRun(StateContext context) {
        context.setPluginStatus(status);
    }

    @Override
    public void onSerialPortUpdate(StateContext context) {
        logger.debug("Serial port updated: " + context.getSerialPort());
        context.setState(new InitializingState(listener));
    }

    @Override
    public void onZWaveNodeAdded(StateContext context, ZWaveEndpoint node) {
        logger.warn("Ignoring ZWave node add");
    }

    @Override
    public void onZWaveNodeUpdated(StateContext context, ZWaveEndpoint node) {
        logger.warn("Ignoring ZWave node update");
    }

    @Override
    public void stop(StateContext context) {
    }
}

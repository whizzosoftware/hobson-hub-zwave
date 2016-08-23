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
import com.whizzosoftware.wzwave.controller.ZWaveController;
import com.whizzosoftware.wzwave.controller.ZWaveControllerListener;
import com.whizzosoftware.wzwave.controller.netty.NettyZWaveController;
import com.whizzosoftware.wzwave.node.ZWaveEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InitializingState implements State {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ZWaveControllerListener listener;

    public InitializingState(ZWaveControllerListener listener) {
        this.listener = listener;
    }

    @Override
    public void onRun(StateContext context) {
        if (context.getSerialPort() != null) {
            logger.info("Using serial port: " + context.getSerialPort());

            // stop any old controller that is running
            if (context.getZWaveController() != null) {
                context.getZWaveController().stop();
            }

            // attempt to start a new controller
            ZWaveController controller = new NettyZWaveController(context.getSerialPort());
            controller.setListener(listener);
            controller.start();
            context.setZWaveController(controller);

            context.setPluginStatus(PluginStatus.running());
            context.setState(new RunningState(listener));
        } else {
            context.setState(new FailedState(listener, PluginStatus.notConfigured("Serial port has not been configured")));
        }
    }

    @Override
    public void onSerialPortUpdate(StateContext context) {
        logger.warn("Ignoring serial port update");
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

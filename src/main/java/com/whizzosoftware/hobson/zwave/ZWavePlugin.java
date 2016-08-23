/*
 *******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.zwave;

import com.whizzosoftware.hobson.api.device.DeviceContext;
import com.whizzosoftware.hobson.api.device.HobsonDevice;
import com.whizzosoftware.hobson.api.plugin.AbstractHobsonPlugin;
import com.whizzosoftware.hobson.api.plugin.PluginStatus;
import com.whizzosoftware.hobson.api.property.PropertyContainer;
import com.whizzosoftware.hobson.api.property.TypedProperty;
import com.whizzosoftware.hobson.zwave.device.*;
import com.whizzosoftware.hobson.zwave.state.InitializingState;
import com.whizzosoftware.hobson.zwave.state.State;
import com.whizzosoftware.hobson.zwave.state.StateContext;
import com.whizzosoftware.hobson.zwave.util.DeviceUtil;
import com.whizzosoftware.wzwave.commandclass.MultiInstanceCommandClass;
import com.whizzosoftware.wzwave.controller.ZWaveController;
import com.whizzosoftware.wzwave.controller.ZWaveControllerListener;
import com.whizzosoftware.wzwave.frame.DataFrame;
import com.whizzosoftware.wzwave.node.NodeInfo;
import com.whizzosoftware.wzwave.node.ZWaveEndpoint;
import com.whizzosoftware.wzwave.node.ZWaveMultiChannelEndpoint;
import com.whizzosoftware.wzwave.node.ZWaveNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;

/**
 * The Z-Wave plugin. This uses a ZWaveController instance to discover Z-Wave devices and publish them as
 * Hobson devices.
 *
 * @author Dan Noguerol
 */
public class ZWavePlugin extends AbstractHobsonPlugin implements StateContext, ZWaveControllerListener, ZWaveContext {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ZWaveController zwaveController;
    private String serialPort;
    private State state;

    public ZWavePlugin(String pluginId) {
        super(pluginId);
    }

    // ***
    // HobsonPlugin methods
    // ***

    @Override
    public void onStartup(PropertyContainer config) {
        serialPort = (String)config.getPropertyValue("serial.port");
        setState(new InitializingState(this));
    }

    @Override
    public void onShutdown() {
        state.stop(this);
    }

    @Override
    public long getRefreshInterval() {
        return 15;
    }

    @Override
    public void onRefresh() {
        for (HobsonDevice device : getAllPluginDevices()) {
            if (device instanceof HobsonZWaveDevice) {
                ((HobsonZWaveDevice)device).onRefresh(this);
            }
        }
    }

    @Override
    protected TypedProperty[] createSupportedProperties() {
        return new TypedProperty[] {
            new TypedProperty.Builder("serial.port", "Serial Port", "The serial port containing the Z-Wave controller (e.g. /dev/tty.SLAB_USBtoUART)", TypedProperty.Type.SERIAL_PORT).build()
        };
    }

    @Override
    synchronized public void onPluginConfigurationUpdate(PropertyContainer config) {
        this.serialPort = (String)config.getPropertyValue("serial.port");
        state.onSerialPortUpdate(this);
    }

    @Override
    public String getName() {
        return "Z-Wave";
    }

    // ***
    // ZWaveControllerListener methods
    // ***

    /**
     * Called when a device is added to the Z-Wave network
     *
     * @param node the node that was added
     */
    @Override
    synchronized public void onZWaveNodeAdded(final ZWaveEndpoint node) {
        executeInEventLoop(new Runnable() {
            @Override
            public void run() {
                state.onZWaveNodeAdded(ZWavePlugin.this, node);
            }
        });
    }

    /**
     * Called when a device is updated via the Z-Wave network
     *
     * @param node the node that was updated
     */
    @Override
    synchronized public void onZWaveNodeUpdated(final ZWaveEndpoint node) {
        executeInEventLoop(new Runnable() {
            @Override
            public void run() {
                state.onZWaveNodeUpdated(ZWavePlugin.this, node);
                if (node instanceof ZWaveNode) {
                    Boolean a = ((ZWaveNode)node).isAvailable();
                    if (a != null) {
                        setDeviceAvailability(DeviceContext.create(getContext(), DeviceUtil.getDeviceIdForNodeId(node.getNodeId())), a, System.currentTimeMillis());
                    }
                }
            }
        });
    }

    @Override
    public void onZWaveConnectionFailure(Throwable t) {
        logger.error("Error connecting to Z-Wave controler", t);
    }

    @Override
    public void onZWaveControllerInfo(String libraryVersion, Integer homeId, Byte nodeId) {
        logger.info("Z-Wave controller info: library version: {}, home ID: {}, node ID: {}", libraryVersion, homeId, nodeId);
    }

    @Override
    public void onZWaveInclusionStarted() {

    }

    @Override
    public void onZWaveInclusion(NodeInfo nodeInfo, boolean success) {

    }

    @Override
    public void onZWaveInclusionStopped() {

    }

    @Override
    public void onZWaveExclusionStarted() {

    }

    @Override
    public void onZWaveExclusion(NodeInfo nodeInfo, boolean success) {

    }

    @Override
    public void onZWaveExclusionStopped() {

    }

    // ***
    // StateContext methods
    // ***

    @Override
    public String getSerialPort() {
        return serialPort;
    }

    @Override
    public ZWaveController getZWaveController() {
        return zwaveController;
    }

    @Override
    public void setZWaveController(ZWaveController zwaveController) {
        this.zwaveController = zwaveController;
    }

    @Override
    public void setPluginStatus(PluginStatus status) {
        setStatus(status);
    }

    @Override
    synchronized public void setState(State state) {
        if (this.state != state) {
            logger.debug("Changing to state: " + state);
            this.state = state;
            this.state.onRun(this);
        }
    }

    @Override
    public HobsonDevice getZWaveDevice(String deviceId) {
        DeviceContext dctx = DeviceContext.create(getContext(), deviceId);
        if (hasDevice(dctx)) {
            return getDevice(dctx);
        } else {
            return null;
        }
    }

    @Override
    public void createZWaveDevice(Class clazz, ZWaveEndpoint node, String name, String primaryVariable) {
        createZWaveDevice(clazz, node, name, primaryVariable, null, null);
    }

    @Override
    public void createZWaveDevice(Class clazz, ZWaveEndpoint node, String name, String primaryVariable, Byte endpointNumber, MultiInstanceCommandClass micc) {
        String deviceId;

        if (endpointNumber != null) {
            deviceId = DeviceUtil.getDeviceIdForNodeIdAndEndpoint(node.getNodeId(), endpointNumber);
        } else {
            deviceId = DeviceUtil.getDeviceIdForNodeId(node.getNodeId());
        }

        try {
            // instantiate the device
            Constructor c = clazz.getConstructor(ZWavePlugin.class, String.class, ZWaveEndpoint.class, String.class, Byte.class, MultiInstanceCommandClass.class);
            HobsonZWaveDevice device = (HobsonZWaveDevice)c.newInstance(this, deviceId, node, name, endpointNumber, micc);
            device.setPrimaryVariable(primaryVariable);

            // publish the device
            logger.debug("Adding device {}", device.getContext());
            publishDevice(device);

            // check to see if device has any endpoints -- add devices for any that are recognized
            if (micc == null) {
                micc = (MultiInstanceCommandClass)node.getCommandClass(MultiInstanceCommandClass.ID);
                if (micc != null) {
                    logger.debug("Found Z-Wave device with {} endpoints", micc.getEndpoints().size());
                    for (ZWaveMultiChannelEndpoint ep : micc.getEndpoints()) {
                        switch (ep.getGenericDeviceClass()) {
                            case ZWaveMultiChannelEndpoint.BINARY_SWITCH:
                                createZWaveDevice(SwitchDevice.class, ep, null, null, ep.getNumber(), micc);
                                break;
                            case ZWaveMultiChannelEndpoint.MULTI_LEVEL_SWITCH:
                                createZWaveDevice(DimmerDevice.class, ep, null, null, ep.getNumber(), micc);
                                break;
                            default:
                                logger.warn("Unsupported Z-Wave endpoint found: " + ep);
                                break;
                        }
                    }
                }
            }

            // allow device to update it's initial state
            ZWaveEndpoint endpoint = null;
            if (device.isEndpoint()) {
                if (micc != null) {
                    endpoint = micc.getEndpoint(device.getEndpointNumber());
                }
            } else {
                endpoint = node;
            }

            if (endpoint != null) {
                onZWaveNodeUpdated(endpoint);
            } else {
                logger.error("Unable to determine node aspect to update for {}", device.getContext());
            }
        } catch (Exception e) {
            logger.error("Error starting device " + deviceId, e);
        }
    }

    @Override
    public void sendDataFrame(DataFrame frame) {
        zwaveController.sendDataFrame(frame);
    }
}

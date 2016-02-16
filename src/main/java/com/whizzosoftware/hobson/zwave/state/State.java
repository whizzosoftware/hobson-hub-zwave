/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.zwave.state;

import com.whizzosoftware.wzwave.node.ZWaveEndpoint;

/**
 * An interface implemented by all classes that implement states of the finite state machine. The interface defines
 * event callbacks that can trigger state transitions.
 *
 * @author Dan Noguerol
 */
public interface State {
    public void onRun(StateContext context);
    public void onSerialPortUpdate(StateContext context);
    public void onZWaveNodeAdded(StateContext context, ZWaveEndpoint node);
    public void onZWaveNodeUpdated(StateContext context, ZWaveEndpoint node);
    public void stop(StateContext context);
}

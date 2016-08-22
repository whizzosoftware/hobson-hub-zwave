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

import com.whizzosoftware.wzwave.frame.DataFrame;

/**
 * An interface that callback functions can use to send Z-Wave frames.
 *
 * @author Dan Noguerol
 */
public interface ZWaveContext {
    void sendDataFrame(DataFrame frame);
}

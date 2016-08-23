/*
 *******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.zwave.util;

public class DeviceUtil {
    static public String getDeviceIdForNodeId(byte nodeId) {
        return "zwave-" + nodeId;
    }

    static public String getDeviceIdForNodeIdAndEndpoint(byte nodeId, byte endpoint) {
        return "zwave-" + nodeId + "-" + endpoint;
    }
}

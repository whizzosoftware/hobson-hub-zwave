/*******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.zwave.device;

import com.whizzosoftware.hobson.api.device.DeviceType;
import com.whizzosoftware.hobson.api.device.MockDeviceManager;
import com.whizzosoftware.hobson.api.property.TypedProperty;
import com.whizzosoftware.hobson.api.variable.VariableContext;
import com.whizzosoftware.hobson.api.variable.VariableUpdate;
import com.whizzosoftware.hobson.zwave.ZWaveContext;
import com.whizzosoftware.hobson.zwave.ZWavePlugin;
import com.whizzosoftware.wzwave.node.ZWaveEndpoint;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;

public class HobsonZWaveDeviceTest {
    @Test
    public void testProcessUpdate() {
        MockDeviceManager dm = new MockDeviceManager();
        ZWavePlugin plugin = new ZWavePlugin("plugin1");
        plugin.setDeviceManager(dm);
        MockZWaveDevice d = new MockZWaveDevice(plugin, "id",(byte)0, null);

        d.processUpdate(null, Collections.singletonList(new VariableUpdate(VariableContext.createLocal("plugin1", "device1", "foo"), "bar")));
        assertEquals("bar", d.getInitialValue("foo"));

        d.onStartup(null);
        assertNull(d.getInitialValue("foo"));

        d.processUpdate(null, Collections.singletonList(new VariableUpdate(VariableContext.createLocal("plugin1", "device1", "foo"), "bar")));
        assertNull(d.getInitialValue("foo"));
    }


    private static class MockZWaveDevice extends HobsonZWaveDevice {
        public MockZWaveDevice(ZWavePlugin driver, String id, byte nodeId, Byte endpointNumber) {
            super(driver, id, new MockZWaveEndpoint(nodeId, null, null), endpointNumber, null);
        }

        @Override
        public void onUpdate(ZWaveEndpoint endpoint, List<VariableUpdate> updates) {
        }

        @Override
        protected TypedProperty[] createSupportedProperties() {
            return null;
        }

        @Override
        public DeviceType getType() {
            return null;
        }

        @Override
        public void onShutdown() {
        }

        @Override
        public void onSetVariable(String s, Object o) {
        }
    }

    private static class MockZWaveEndpoint extends ZWaveEndpoint {
        public MockZWaveEndpoint(byte nodeId, Byte genericDeviceClass, Byte specificDeviceClass) {
            super(nodeId, genericDeviceClass, specificDeviceClass);
        }
    }
}

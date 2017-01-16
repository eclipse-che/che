/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.machine.server.model.impl.adapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;

import org.eclipse.che.api.core.model.machine.MachineSource;
import org.eclipse.che.api.machine.server.model.impl.MachineSourceImpl;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.io.StringReader;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Test {@link MachineSourceAdapter} on serialization
 *
 * @author Florent Benoit
 */
public class MachineSourceAdapterTest {

    /**
     * Check we can transform object into JSON and JSON into object
     */
    @Test
    public void testSerializeAndDeserialize() {

        MachineSourceAdapter machineSourceAdapter = spy(new MachineSourceAdapter());

        Gson gson = new GsonBuilder().registerTypeAdapter(MachineSource.class, machineSourceAdapter).setPrettyPrinting().create();

        final String TYPE = "myType";
        final String LOCATION = "myLocation";
        final String CONTENT = "myContent";

        // serialize
        MachineSource machineSource = new MachineSourceImpl(TYPE).setLocation(LOCATION).setContent(CONTENT);
        String json = gson.toJson(machineSource, MachineSource.class);
        assertNotNull(json);

        // verify we called serializer
        Mockito.verify(machineSourceAdapter).serialize(eq(machineSource), eq(MachineSource.class), any(JsonSerializationContext.class));

        // now deserialize
        MachineSource machineSourceDeserialize = gson.fromJson(new StringReader(json), MachineSource.class);
        assertNotNull(machineSourceDeserialize);
        assertEquals(machineSourceDeserialize.getLocation(), LOCATION);
        assertEquals(machineSourceDeserialize.getType(), TYPE);
        assertEquals(machineSourceDeserialize.getContent(), CONTENT);
        // verify we called deserializer
        Mockito.verify(machineSourceAdapter).deserialize(any(JsonElement.class), eq(MachineSource.class), any(JsonDeserializationContext.class));
    }
}

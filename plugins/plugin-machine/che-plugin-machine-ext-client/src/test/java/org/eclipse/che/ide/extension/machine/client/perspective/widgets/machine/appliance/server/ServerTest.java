/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance.server;

import org.eclipse.che.api.machine.shared.dto.ServerDto;
import org.eclipse.che.api.machine.shared.dto.ServerPropertiesDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class ServerTest {

    private static final String SOME_TEXT = "someText";

    @Mock
    private ServerDto descriptor;

    @Mock
    private ServerPropertiesDto descriptor2;

    private Server server;

    @Before
    public void setUp() {
        server = new Server(SOME_TEXT, descriptor);
    }

    @Test
    public void exposedPortShouldBeReturned() {
        assertThat(server.getPort(), equalTo(SOME_TEXT));
    }

    @Test
    public void addressShouldBeReturned() {
        when(descriptor.getAddress()).thenReturn(SOME_TEXT);

        assertThat(server.getAddress(), equalTo(SOME_TEXT));

        verify(descriptor).getAddress();
    }

    @Test
    public void urlShouldBeReturned() {
        when(descriptor.getUrl()).thenReturn(SOME_TEXT);

        assertThat(server.getUrl(), equalTo(SOME_TEXT));

        verify(descriptor).getUrl();
    }

    @Test
    public void refShouldBeReturned() {
        when(descriptor.getRef()).thenReturn(SOME_TEXT);

        assertThat(server.getRef(), equalTo(SOME_TEXT));

        verify(descriptor).getRef();
    }

    @Test
    public void pathShouldBeReturned() {
        when(descriptor.getProperties()).thenReturn(descriptor2);
        when(descriptor2.getPath()).thenReturn(SOME_TEXT);

        assertThat(server.getProperties().getPath(), equalTo(SOME_TEXT));

        verify(descriptor).getProperties();
        verify(descriptor2).getPath();
    }

    @Test
    public void internalAddressShouldBeReturned() {
        when(descriptor.getProperties()).thenReturn(descriptor2);
        when(descriptor2.getInternalAddress()).thenReturn(SOME_TEXT);

        assertThat(server.getProperties().getInternalAddress(), equalTo(SOME_TEXT));

        verify(descriptor).getProperties();
        verify(descriptor2).getInternalAddress();
    }

    @Test
    public void internalUrlShouldBeReturned() {
        when(descriptor.getProperties()).thenReturn(descriptor2);
        when(descriptor2.getInternalUrl()).thenReturn(SOME_TEXT);

        assertThat(server.getProperties().getInternalUrl(), equalTo(SOME_TEXT));

        verify(descriptor).getProperties();
        verify(descriptor2).getInternalUrl();
    }

}

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
package org.eclipse.che.plugin.docker.machine;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.machine.server.MachineManager;
import org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.machine.ext.DockerMachineTerminalLauncher;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 *
 */
@Listeners(value = {MockitoTestNGListener.class})
public class DockerMachineTerminalLauncherTest {

    private  EventService    eventService;
    @Mock
    private  DockerConnector docker;
    @Mock
    private  MachineManager  machineManager;

    private DockerMachineTerminalLauncher launcher;

    @BeforeMethod
    public void setUp() throws Exception {
        eventService = new EventService();
    }


    @Test
    public void shouldSkipEventsWithStatusOtherThanRunning() {
        launcher = new DockerMachineTerminalLauncher(eventService,docker,machineManager,"");

        launcher.start();

        eventService.publish(DtoFactory.newDto(MachineStatusEvent.class).withEventType(MachineStatusEvent.EventType.ERROR));

        verifyZeroInteractions(machineManager);
    }

}

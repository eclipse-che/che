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
package org.eclipse.che.ide.ext.machine.server.ssh;

import org.eclipse.che.api.core.model.machine.MachineMetadata;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.machine.server.MachineManager;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent;
import org.eclipse.che.api.ssh.server.SshManager;
import org.eclipse.che.api.ssh.server.model.impl.SshPairImpl;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.Exec;
import org.eclipse.che.plugin.docker.client.LogMessage;
import org.eclipse.che.plugin.docker.client.MessageProcessor;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Test for {@link KeysInjector}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class KeysInjectorTest {
    private static final String MACHINE_ID   = "machine123";
    private static final String OWNER_ID     = "user123";
    private static final String CONTAINER_ID = "container123";
    private static final String EXEC_ID      = "exec123";

    @Captor
    ArgumentCaptor<EventSubscriber<MachineStatusEvent>> subscriberCaptor;
    @Captor
    ArgumentCaptor<MessageProcessor<LogMessage>>        messageProcessorCaptor;

    @Mock
    Instance        instance;
    @Mock
    MachineMetadata metadata;
    @Mock
    Exec            exec;
    @Mock
    LogMessage      logMessage;
    @Mock
    LineConsumer    lineConsumer;

    @Mock
    EventService    eventService;
    @Mock
    DockerConnector docker;
    @Mock
    MachineManager  machineManager;
    @Mock
    SshManager      sshManager;

    EventSubscriber<MachineStatusEvent> subscriber;

    @InjectMocks
    KeysInjector keysInjector;

    @BeforeMethod
    public void setUp() throws Exception {
        final Map<String, String> metadataProperties = new HashMap<>();
        metadataProperties.put("id", CONTAINER_ID);
        when(metadata.getProperties()).thenReturn(metadataProperties);

        when(machineManager.getMachine(MACHINE_ID)).thenReturn(instance);
        when(instance.getOwner()).thenReturn(OWNER_ID);
        when(instance.getMetadata()).thenReturn(metadata);
        when(instance.getLogger()).thenReturn(lineConsumer);

        keysInjector.start();
        verify(eventService).subscribe(subscriberCaptor.capture());
        subscriber = subscriberCaptor.getValue();

        when(docker.createExec(anyString(), anyBoolean(), anyString())).thenReturn(exec);
        when(docker.createExec(anyString(), anyBoolean(), anyString(), anyString(), anyString())).thenReturn(exec);
        when(docker.createExec(anyString(), anyBoolean(), anyString())).thenReturn(exec);
        when(exec.getId()).thenReturn(EXEC_ID);
    }

    @Test
    public void shouldNotDoAnythingIfEventTypeDoesNotEqualToRunning() {
        subscriber.onEvent(newDto(MachineStatusEvent.class).withEventType(MachineStatusEvent.EventType.DESTROYING));

        verifyZeroInteractions(docker, machineManager, sshManager);
    }

    @Test
    public void shouldNotInjectSshKeysWhenThereAreNotAnyPair() throws Exception {
        when(sshManager.getPairs(anyString(), anyString())).thenReturn(Collections.emptyList());

        subscriber.onEvent(newDto(MachineStatusEvent.class).withEventType(MachineStatusEvent.EventType.RUNNING)
                                                           .withMachineId(MACHINE_ID));

        verify(machineManager).getMachine(eq(MACHINE_ID));
        verify(sshManager).getPairs(eq(OWNER_ID), eq("machine"));
        verifyZeroInteractions(docker, machineManager, sshManager);
    }

    @Test
    public void shouldNotInjectSshKeysWhenThereAreNotAnyPairWithPublicKey() throws Exception {
        when(sshManager.getPairs(anyString(), anyString()))
                .thenReturn(Collections.singletonList(new SshPairImpl("machine", "myPair", null, null)));

        subscriber.onEvent(newDto(MachineStatusEvent.class).withEventType(MachineStatusEvent.EventType.RUNNING)
                                                           .withMachineId(MACHINE_ID));

        verify(machineManager).getMachine(eq(MACHINE_ID));
        verify(sshManager).getPairs(eq(OWNER_ID), eq("machine"));
        verifyZeroInteractions(docker, machineManager, sshManager);
    }

    @Test
    public void shouldInjectSshKeysWhenThereAreAnyPairWithNotNullPublicKey() throws Exception {
        when(sshManager.getPairs(anyString(), anyString()))
                .thenReturn(Arrays.asList(new SshPairImpl("machine", "myPair", "publicKey1", null),
                                          new SshPairImpl("machine", "myPair", "publicKey2", null)));

        subscriber.onEvent(newDto(MachineStatusEvent.class).withEventType(MachineStatusEvent.EventType.RUNNING)
                                                           .withMachineId(MACHINE_ID));

        verify(machineManager).getMachine(eq(MACHINE_ID));
        verify(sshManager).getPairs(eq(OWNER_ID), eq("machine"));

        verify(docker).createExec(anyString(), anyBoolean(), eq("/bin/bash"), eq("-c"), eq("mkdir ~/.ssh/ -p" +
                                                                                           "&& echo 'publicKey1' >> ~/.ssh/authorized_keys" +
                                                                                           "&& echo 'publicKey2' >> ~/.ssh/authorized_keys"));
        verify(docker).startExec(eq(EXEC_ID), anyObject());
        verifyZeroInteractions(docker, machineManager, sshManager);
    }

    @Test
    public void shouldSendMessageInMachineLoggerWhenSomeErrorOcursOnKeysInjection() throws Exception {
        when(sshManager.getPairs(anyString(), anyString()))
                .thenReturn(Collections.singletonList(new SshPairImpl("machine", "myPair", "publicKey1", null)));
        when(logMessage.getType()).thenReturn(LogMessage.Type.STDERR);
        when(logMessage.getContent()).thenReturn("FAILED");

        subscriber.onEvent(newDto(MachineStatusEvent.class).withEventType(MachineStatusEvent.EventType.RUNNING)
                                                           .withMachineId(MACHINE_ID));

        verify(docker).startExec(eq(EXEC_ID), messageProcessorCaptor.capture());
        final MessageProcessor<LogMessage> value = messageProcessorCaptor.getValue();
        value.process(logMessage);

        verify(lineConsumer).writeLine(eq("Error of injection public ssh keys. FAILED"));
    }
}

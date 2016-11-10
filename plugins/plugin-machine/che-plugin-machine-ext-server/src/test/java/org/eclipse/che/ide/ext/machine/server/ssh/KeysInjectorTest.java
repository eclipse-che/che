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

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.machine.MachineRuntimeInfo;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.environment.server.CheEnvironmentEngine;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent;
import org.eclipse.che.api.ssh.server.SshManager;
import org.eclipse.che.api.ssh.server.model.impl.SshPairImpl;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.Exec;
import org.eclipse.che.plugin.docker.client.LogMessage;
import org.eclipse.che.plugin.docker.client.MessageProcessor;
import org.eclipse.che.plugin.docker.client.params.CreateExecParams;
import org.eclipse.che.plugin.docker.client.params.StartExecParams;
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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Test for {@link KeysInjector}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class KeysInjectorTest {
    private static final String WORKSPACE_ID = "workspace123";
    private static final String MACHINE_ID   = "machine123";
    private static final String OWNER_NAME   = "user";
    private static final String USER_ID      = "user123";
    private static final String CONTAINER_ID = "container123";
    private static final String EXEC_ID      = "exec123";

    @Captor
    ArgumentCaptor<EventSubscriber<MachineStatusEvent>> subscriberCaptor;
    @Captor
    ArgumentCaptor<MessageProcessor<LogMessage>>        messageProcessorCaptor;

    @Mock
    Instance           instance;
    @Mock
    MachineRuntimeInfo machineRuntime;
    @Mock
    Exec               exec;
    @Mock
    LogMessage         logMessage;
    @Mock
    LineConsumer       lineConsumer;

    @Mock
    EventService         eventService;
    @Mock
    DockerConnector      docker;
    @Mock
    CheEnvironmentEngine environmentEngine;
    @Mock
    SshManager           sshManager;
    @Mock
    UserManager          userManager;
    @Mock
    User                 user;

    EventSubscriber<MachineStatusEvent> subscriber;

    @InjectMocks
    KeysInjector keysInjector;

    @BeforeMethod
    public void setUp() throws Exception {
        final Map<String, String> metadataProperties = new HashMap<>();
        metadataProperties.put("id", CONTAINER_ID);
        when(machineRuntime.getProperties()).thenReturn(metadataProperties);

        when(environmentEngine.getMachine(WORKSPACE_ID, MACHINE_ID)).thenReturn(instance);
        when(instance.getOwner()).thenReturn(OWNER_NAME);
        when(instance.getRuntime()).thenReturn(machineRuntime);
        when(instance.getLogger()).thenReturn(lineConsumer);

        keysInjector.start();
        verify(eventService).subscribe(subscriberCaptor.capture());
        subscriber = subscriberCaptor.getValue();

        when(userManager.getByName(eq(OWNER_NAME))).thenReturn(user);
        when(user.getId()).thenReturn(USER_ID);

        when(docker.createExec(any(CreateExecParams.class))).thenReturn(exec);
        when(exec.getId()).thenReturn(EXEC_ID);
    }

    @Test
    public void shouldNotDoAnythingIfEventTypeDoesNotEqualToRunning() {
        subscriber.onEvent(newDto(MachineStatusEvent.class).withEventType(MachineStatusEvent.EventType.DESTROYING));

        verifyZeroInteractions(docker, environmentEngine, sshManager);
    }

    @Test
    public void shouldNotInjectSshKeysWhenThereAreNotAnyPair() throws Exception {
        when(sshManager.getPairs(anyString(), anyString())).thenReturn(Collections.emptyList());

        subscriber.onEvent(newDto(MachineStatusEvent.class).withEventType(MachineStatusEvent.EventType.RUNNING)
                                                           .withMachineId(MACHINE_ID)
                                                           .withWorkspaceId(WORKSPACE_ID));

        verify(environmentEngine).getMachine(eq(WORKSPACE_ID), eq(MACHINE_ID));
        verify(sshManager).getPairs(eq(USER_ID), eq("machine"));
        verify(sshManager).getPair(eq(USER_ID), eq("workspace"), eq(WORKSPACE_ID));
        verifyZeroInteractions(docker, environmentEngine, sshManager);
    }

    @Test
    public void shouldNotInjectSshKeysWhenThereAreNotAnyPairWithPublicKey() throws Exception {
        when(sshManager.getPairs(anyString(), anyString()))
                .thenReturn(Collections.singletonList(new SshPairImpl(USER_ID, "machine", "myPair", null, null)));

        subscriber.onEvent(newDto(MachineStatusEvent.class).withEventType(MachineStatusEvent.EventType.RUNNING)
                                                           .withMachineId(MACHINE_ID)
                                                           .withWorkspaceId(WORKSPACE_ID));

        verify(environmentEngine).getMachine(eq(WORKSPACE_ID), eq(MACHINE_ID));
        verify(sshManager).getPairs(eq(USER_ID), eq("machine"));
        verify(sshManager).getPair(eq(USER_ID), eq("workspace"), eq(WORKSPACE_ID));
        verifyZeroInteractions(docker, environmentEngine, sshManager);
    }

    @Test
    public void shouldInjectSshKeysWhenThereAreAnyPairWithNotNullPublicKey() throws Exception {
        when(sshManager.getPairs(anyString(), anyString()))
                .thenReturn(Arrays.asList(new SshPairImpl(USER_ID, "machine", "myPair", "publicKey1", null),
                                          new SshPairImpl(USER_ID, "machine", "myPair", "publicKey2", null)));

        subscriber.onEvent(newDto(MachineStatusEvent.class).withEventType(MachineStatusEvent.EventType.RUNNING)
                                                           .withMachineId(MACHINE_ID)
                                                           .withWorkspaceId(WORKSPACE_ID));

        verify(environmentEngine).getMachine(eq(WORKSPACE_ID), eq(MACHINE_ID));
        verify(sshManager).getPairs(eq(USER_ID), eq("machine"));
        verify(sshManager).getPair(eq(USER_ID), eq("workspace"), eq(WORKSPACE_ID));

        ArgumentCaptor<CreateExecParams> argumentCaptor = ArgumentCaptor.forClass(CreateExecParams.class);
        verify(docker).createExec(argumentCaptor.capture());
        assertEquals(argumentCaptor.getValue().getCmd(), new String[] {"/bin/bash", "-c", "mkdir ~/.ssh/ -p" +
                                                                                          "&& echo 'publicKey1' >> ~/.ssh/authorized_keys" +
                                                                                          "&& echo 'publicKey2' >> ~/.ssh/authorized_keys"});
        verify(docker).startExec(eq(StartExecParams.create(EXEC_ID)), anyObject());
        verifyZeroInteractions(docker, environmentEngine, sshManager);
    }

    /**
     * Validate the usecase: There is a workspace sshkeypair but no machine keypair (empty list)
     * Expect that the workspace public key is injected.
     */
    @Test
    public void shouldInjectSshKeysWhenThereIsOnlyWorkspaceKey() throws Exception {
        // no machine key pairs
        when(sshManager.getPairs(anyString(), eq("machine")))
                .thenReturn(Collections.emptyList());

        // workspace keypair
        when(sshManager.getPair(anyString(), eq("workspace"), anyString()))
                .thenReturn(new SshPairImpl(USER_ID, "workspace", WORKSPACE_ID, "publicKeyWorkspace", null));


        subscriber.onEvent(newDto(MachineStatusEvent.class).withEventType(MachineStatusEvent.EventType.RUNNING)
                                                           .withMachineId(MACHINE_ID)
                                                           .withWorkspaceId(WORKSPACE_ID));

        verify(environmentEngine).getMachine(eq(WORKSPACE_ID), eq(MACHINE_ID));
        // check calls for machine and workspace ssh pairs
        verify(sshManager).getPairs(eq(USER_ID), eq("machine"));
        verify(sshManager).getPair(eq(USER_ID), eq("workspace"), eq(WORKSPACE_ID));

        ArgumentCaptor<CreateExecParams> argumentCaptor = ArgumentCaptor.forClass(CreateExecParams.class);
        verify(docker).createExec(argumentCaptor.capture());
        assertEquals(argumentCaptor.getValue().getCmd(), new String[] {"/bin/bash", "-c", "mkdir ~/.ssh/ -p" +
                                                                                          "&& echo 'publicKeyWorkspace' >> ~/.ssh/authorized_keys"});
        verify(docker).startExec(eq(StartExecParams.create(EXEC_ID)), anyObject());
        verifyZeroInteractions(docker, environmentEngine, sshManager);
    }

    /**
     * Validate the usecase: There is a workspace sshkeypair (without public key) but there is a machine keypair
     * Expect that only the machine keypair is injected (as workspace keypair has no public key).
     */
    @Test
    public void shouldInjectSshKeysWhenThereIsNoPublicWorkspaceKeyButMachineKeys() throws Exception {
        // no machine key pairs
        when(sshManager.getPairs(anyString(), eq("machine")))
                .thenReturn(Arrays.asList(new SshPairImpl(USER_ID, "machine", "myPair", "publicKey1", null)));

        // workspace keypair without public key
        when(sshManager.getPair(anyString(), eq("workspace"), anyString()))
                .thenReturn(new SshPairImpl(USER_ID, "workspace", WORKSPACE_ID, null, null));


        subscriber.onEvent(newDto(MachineStatusEvent.class).withEventType(MachineStatusEvent.EventType.RUNNING)
                                                           .withMachineId(MACHINE_ID)
                                                           .withWorkspaceId(WORKSPACE_ID));

        verify(environmentEngine).getMachine(eq(WORKSPACE_ID), eq(MACHINE_ID));
        // check calls for machine and workspace ssh pairs
        verify(sshManager).getPairs(eq(USER_ID), eq("machine"));
        verify(sshManager).getPair(eq(USER_ID), eq("workspace"), eq(WORKSPACE_ID));

        ArgumentCaptor<CreateExecParams> argumentCaptor = ArgumentCaptor.forClass(CreateExecParams.class);
        verify(docker).createExec(argumentCaptor.capture());
        assertEquals(argumentCaptor.getValue().getCmd(), new String[] {"/bin/bash", "-c", "mkdir ~/.ssh/ -p" +
                                                                                          "&& echo 'publicKey1' >> ~/.ssh/authorized_keys"});
        verify(docker).startExec(eq(StartExecParams.create(EXEC_ID)), anyObject());
        verifyZeroInteractions(docker, environmentEngine, sshManager);
    }

    /**
     * Validate the usecase of no workspace keypair (notfound exception) and no machine keypair
     * Expect no ssh keys are injected
     */
    @Test
    public void shouldNotInjectSshKeysWhenThereIsNoWorkspaceKey() throws Exception {
        // no machine key pairs
        when(sshManager.getPairs(anyString(), eq("machine")))
                .thenReturn(Collections.emptyList());

        // no workspace keypair
        when(sshManager.getPair(anyString(), eq("workspace"), anyString()))
                .thenThrow(NotFoundException.class);


        subscriber.onEvent(newDto(MachineStatusEvent.class).withEventType(MachineStatusEvent.EventType.RUNNING)
                                                           .withMachineId(MACHINE_ID)
                                                           .withWorkspaceId(WORKSPACE_ID));

        verify(environmentEngine).getMachine(eq(WORKSPACE_ID), eq(MACHINE_ID));
        // check calls for machine and workspace ssh pairs
        verify(sshManager).getPairs(eq(USER_ID), eq("machine"));
        verify(sshManager).getPair(eq(USER_ID), eq("workspace"), eq(WORKSPACE_ID));

        verifyZeroInteractions(docker, environmentEngine, sshManager);
    }

    @Test
    public void shouldSendMessageInMachineLoggerWhenSomeErrorOcursOnKeysInjection() throws Exception {
        when(sshManager.getPairs(anyString(), anyString()))
                .thenReturn(Collections.singletonList(new SshPairImpl(USER_ID, "machine", "myPair", "publicKey1", null)));
        when(logMessage.getType()).thenReturn(LogMessage.Type.STDERR);
        when(logMessage.getContent()).thenReturn("FAILED");

        subscriber.onEvent(newDto(MachineStatusEvent.class).withEventType(MachineStatusEvent.EventType.RUNNING)
                                                           .withMachineId(MACHINE_ID)
                                                           .withWorkspaceId(WORKSPACE_ID));

        verify(docker).startExec(eq(StartExecParams.create(EXEC_ID)), messageProcessorCaptor.capture());
        final MessageProcessor<LogMessage> value = messageProcessorCaptor.getValue();
        value.process(logMessage);

        verify(lineConsumer).writeLine(eq("Error of injection public ssh keys. FAILED"));
    }
}

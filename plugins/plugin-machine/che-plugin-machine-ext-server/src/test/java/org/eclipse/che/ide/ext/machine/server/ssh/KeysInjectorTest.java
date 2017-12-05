/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.machine.server.ssh;

import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;

/**
 * Test for {@link KeysInjector}
 *
 * @author Sergii Leschenko
 */
// FIXME: spi
@Listeners(MockitoTestNGListener.class)
public class KeysInjectorTest {
  //    private static final String WORKSPACE_ID = "workspace123";
  //    private static final String MACHINE_ID   = "machine123";
  //    private static final String OWNER        = "user123";
  //    private static final String CONTAINER_ID = "container123";
  //    private static final String EXEC_ID      = "exec123";
  //
  //    @Captor
  //    ArgumentCaptor<EventSubscriber<MachineStatusEvent>> subscriberCaptor;
  //    @Captor
  //    ArgumentCaptor<MessageProcessor<LogMessage>>        messageProcessorCaptor;
  //
  //    @Mock
  //    Instance           instance;
  //    @Mock
  //    MachineRuntimeInfo machineRuntime;
  //    @Mock
  //    Exec               exec;
  //    @Mock
  //    LogMessage         logMessage;
  //    @Mock
  //    LineConsumer       lineConsumer;
  //
  //    @Mock
  //    EventService         eventService;
  //    @Mock
  //    DockerConnector      docker;
  //    @Mock
  //    CheEnvironmentEngine environmentEngine;
  //    @Mock
  //    SshManager           sshManager;
  //    @Mock
  //    User                 user;
  //
  //    EventSubscriber<MachineStatusEvent> subscriber;
  //
  //    private class MockConnectorProvider extends DockerConnectorProvider {
  //
  //        public MockConnectorProvider() {
  //            super(Collections.emptyMap(), "default");
  //        }
  //
  //        @Override
  //        public DockerConnector get() {
  //            return docker;
  //        }
  //    }
  //
  //    KeysInjector keysInjector;
  //
  //    @BeforeMethod
  //    public void setUp() throws Exception {
  //        final Map<String, String> metadataProperties = new HashMap<>();
  //        metadataProperties.put("id", CONTAINER_ID);
  //        when(machineRuntime.getAttributes()).thenReturn(metadataProperties);
  //
  //        when(environmentEngine.getMachine(WORKSPACE_ID, MACHINE_ID)).thenReturn(instance);
  //        when(instance.getOwner()).thenReturn(OWNER);
  //        when(instance.getRuntime()).thenReturn(machineRuntime);
  //        when(instance.getLogger()).thenReturn(lineConsumer);
  //
  //        keysInjector = new KeysInjector(eventService,
  //                                        new MockConnectorProvider(),
  //                                        sshManager,
  //                                        environmentEngine);
  //
  //        keysInjector.start();
  //        verify(eventService).subscribe(subscriberCaptor.capture());
  //        subscriber = subscriberCaptor.getValue();
  //
  //        when(docker.createExec(any(CreateExecParams.class))).thenReturn(exec);
  //        when(exec.getId()).thenReturn(EXEC_ID);
  //    }
  //
  //    @Test
  //    public void shouldNotDoAnythingIfEventTypeDoesNotEqualToRunning() {
  //
  // subscriber.onEvent(newDto(MachineStatusEvent.class).withEventType(MachineStatusEvent.EventType.DESTROYING));
  //
  //        verifyZeroInteractions(docker, environmentEngine, sshManager);
  //    }
  //
  //    @Test
  //    public void shouldNotInjectSshKeysWhenThereAreNotAnyPair() throws Exception {
  //        when(sshManager.getPairs(anyString(), anyString())).thenReturn(Collections.emptyList());
  //
  //
  // subscriber.onEvent(newDto(MachineStatusEvent.class).withEventType(MachineStatusEvent.EventType.RUNNING)
  //                                                           .withMachineId(MACHINE_ID)
  //                                                           .withWorkspaceId(WORKSPACE_ID));
  //
  //        verify(environmentEngine).getMachine(eq(WORKSPACE_ID), eq(MACHINE_ID));
  //        verify(sshManager).getPairs(eq(OWNER), eq("machine"));
  //        verify(sshManager).getPair(eq(OWNER), eq("workspace"), eq(WORKSPACE_ID));
  //        verifyZeroInteractions(docker, environmentEngine, sshManager);
  //    }
  //
  //    @Test
  //    public void shouldNotInjectSshKeysWhenThereAreNotAnyPairWithPublicKey() throws Exception {
  //        when(sshManager.getPairs(anyString(), anyString()))
  //                .thenReturn(Collections.singletonList(new SshPairImpl(OWNER, "machine",
  // "myPair", null, null)));
  //
  //
  // subscriber.onEvent(newDto(MachineStatusEvent.class).withEventType(MachineStatusEvent.EventType.RUNNING)
  //                                                           .withMachineId(MACHINE_ID)
  //                                                           .withWorkspaceId(WORKSPACE_ID));
  //
  //        verify(environmentEngine).getMachine(eq(WORKSPACE_ID), eq(MACHINE_ID));
  //        verify(sshManager).getPairs(eq(OWNER), eq("machine"));
  //        verify(sshManager).getPair(eq(OWNER), eq("workspace"), eq(WORKSPACE_ID));
  //        verifyZeroInteractions(docker, environmentEngine, sshManager);
  //    }
  //
  //    @Test
  //    public void shouldInjectSshKeysWhenThereAreAnyPairWithNotNullPublicKey() throws Exception {
  //        when(sshManager.getPairs(anyString(), anyString()))
  //                .thenReturn(Arrays.asList(new SshPairImpl(OWNER, "machine", "myPair",
  // "publicKey1", null),
  //                                          new SshPairImpl(OWNER, "machine", "myPair",
  // "publicKey2", null)));
  //
  //
  // subscriber.onEvent(newDto(MachineStatusEvent.class).withEventType(MachineStatusEvent.EventType.RUNNING)
  //                                                           .withMachineId(MACHINE_ID)
  //                                                           .withWorkspaceId(WORKSPACE_ID));
  //
  //        verify(environmentEngine).getMachine(eq(WORKSPACE_ID), eq(MACHINE_ID));
  //        verify(sshManager).getPairs(eq(OWNER), eq("machine"));
  //        verify(sshManager).getPair(eq(OWNER), eq("workspace"), eq(WORKSPACE_ID));
  //
  //        ArgumentCaptor<CreateExecParams> argumentCaptor =
  // ArgumentCaptor.forClass(CreateExecParams.class);
  //        verify(docker).createExec(argumentCaptor.capture());
  //        assertEquals(argumentCaptor.getValue().getCmd(), new String[] {"/bin/bash", "-c", "mkdir
  // ~/.ssh/ -p" +
  //                                                                                          "&&
  // echo 'publicKey1' >> ~/.ssh/authorized_keys" +
  //                                                                                          "&&
  // echo 'publicKey2' >> ~/.ssh/authorized_keys"});
  //        verify(docker).startExec(eq(StartExecParams.create(EXEC_ID)), anyObject());
  //        verifyZeroInteractions(docker, environmentEngine, sshManager);
  //    }
  //
  //    /**
  //     * Validate the usecase: There is a workspace sshkeypair but no machine keypair (empty list)
  //     * Expect that the workspace public key is injected.
  //     */
  //    @Test
  //    public void shouldInjectSshKeysWhenThereIsOnlyWorkspaceKey() throws Exception {
  //        // no machine key pairs
  //        when(sshManager.getPairs(anyString(), eq("machine")))
  //                .thenReturn(Collections.emptyList());
  //
  //        // workspace keypair
  //        when(sshManager.getPair(anyString(), eq("workspace"), anyString()))
  //                .thenReturn(new SshPairImpl(OWNER, "workspace", WORKSPACE_ID,
  // "publicKeyWorkspace", null));
  //
  //
  //
  // subscriber.onEvent(newDto(MachineStatusEvent.class).withEventType(MachineStatusEvent.EventType.RUNNING)
  //                                                           .withMachineId(MACHINE_ID)
  //                                                           .withWorkspaceId(WORKSPACE_ID));
  //
  //        verify(environmentEngine).getMachine(eq(WORKSPACE_ID), eq(MACHINE_ID));
  //        // check calls for machine and workspace ssh pairs
  //        verify(sshManager).getPairs(eq(OWNER), eq("machine"));
  //        verify(sshManager).getPair(eq(OWNER), eq("workspace"), eq(WORKSPACE_ID));
  //
  //        ArgumentCaptor<CreateExecParams> argumentCaptor =
  // ArgumentCaptor.forClass(CreateExecParams.class);
  //        verify(docker).createExec(argumentCaptor.capture());
  //        assertEquals(argumentCaptor.getValue().getCmd(), new String[] {"/bin/bash", "-c", "mkdir
  // ~/.ssh/ -p" +
  //                                                                                          "&&
  // echo 'publicKeyWorkspace' >> ~/.ssh/authorized_keys"});
  //        verify(docker).startExec(eq(StartExecParams.create(EXEC_ID)), anyObject());
  //        verifyZeroInteractions(docker, environmentEngine, sshManager);
  //    }
  //
  //    /**
  //     * Validate the usecase: There is a workspace sshkeypair (without public key) but there is a
  // machine keypair
  //     * Expect that only the machine keypair is injected (as workspace keypair has no public
  // key).
  //     */
  //    @Test
  //    public void shouldInjectSshKeysWhenThereIsNoPublicWorkspaceKeyButMachineKeys() throws
  // Exception {
  //        // no machine key pairs
  //        when(sshManager.getPairs(anyString(), eq("machine")))
  //                .thenReturn(Arrays.asList(new SshPairImpl(OWNER, "machine", "myPair",
  // "publicKey1", null)));
  //
  //        // workspace keypair without public key
  //        when(sshManager.getPair(anyString(), eq("workspace"), anyString()))
  //                .thenReturn(new SshPairImpl(OWNER, "workspace", WORKSPACE_ID, null, null));
  //
  //
  //
  // subscriber.onEvent(newDto(MachineStatusEvent.class).withEventType(MachineStatusEvent.EventType.RUNNING)
  //                                                           .withMachineId(MACHINE_ID)
  //                                                           .withWorkspaceId(WORKSPACE_ID));
  //
  //        verify(environmentEngine).getMachine(eq(WORKSPACE_ID), eq(MACHINE_ID));
  //        // check calls for machine and workspace ssh pairs
  //        verify(sshManager).getPairs(eq(OWNER), eq("machine"));
  //        verify(sshManager).getPair(eq(OWNER), eq("workspace"), eq(WORKSPACE_ID));
  //
  //        ArgumentCaptor<CreateExecParams> argumentCaptor =
  // ArgumentCaptor.forClass(CreateExecParams.class);
  //        verify(docker).createExec(argumentCaptor.capture());
  //        assertEquals(argumentCaptor.getValue().getCmd(), new String[] {"/bin/bash", "-c", "mkdir
  // ~/.ssh/ -p" +
  //                                                                                          "&&
  // echo 'publicKey1' >> ~/.ssh/authorized_keys"});
  //        verify(docker).startExec(eq(StartExecParams.create(EXEC_ID)), anyObject());
  //        verifyZeroInteractions(docker, environmentEngine, sshManager);
  //    }
  //
  //    /**
  //     * Validate the usecase of no workspace keypair (notfound exception) and no machine keypair
  //     * Expect no ssh keys are injected
  //     */
  //    @Test
  //    public void shouldNotInjectSshKeysWhenThereIsNoWorkspaceKey() throws Exception {
  //        // no machine key pairs
  //        when(sshManager.getPairs(anyString(), eq("machine")))
  //                .thenReturn(Collections.emptyList());
  //
  //        // no workspace keypair
  //        when(sshManager.getPair(anyString(), eq("workspace"), anyString()))
  //                .thenThrow(NotFoundException.class);
  //
  //
  //
  // subscriber.onEvent(newDto(MachineStatusEvent.class).withEventType(MachineStatusEvent.EventType.RUNNING)
  //                                                           .withMachineId(MACHINE_ID)
  //                                                           .withWorkspaceId(WORKSPACE_ID));
  //
  //        verify(environmentEngine).getMachine(eq(WORKSPACE_ID), eq(MACHINE_ID));
  //        // check calls for machine and workspace ssh pairs
  //        verify(sshManager).getPairs(eq(OWNER), eq("machine"));
  //        verify(sshManager).getPair(eq(OWNER), eq("workspace"), eq(WORKSPACE_ID));
  //
  //        verifyZeroInteractions(docker, environmentEngine, sshManager);
  //    }
  //
  //    @Test
  //    public void shouldSendMessageInMachineLoggerWhenSomeErrorOcursOnKeysInjection() throws
  // Exception {
  //        when(sshManager.getPairs(anyString(), anyString()))
  //                .thenReturn(Collections.singletonList(new SshPairImpl(OWNER, "machine",
  // "myPair", "publicKey1", null)));
  //        when(logMessage.getType()).thenReturn(LogMessage.Type.STDERR);
  //        when(logMessage.getContent()).thenReturn("FAILED");
  //
  //
  // subscriber.onEvent(newDto(MachineStatusEvent.class).withEventType(MachineStatusEvent.EventType.RUNNING)
  //                                                           .withMachineId(MACHINE_ID)
  //                                                           .withWorkspaceId(WORKSPACE_ID));
  //
  //        verify(docker).startExec(eq(StartExecParams.create(EXEC_ID)),
  // messageProcessorCaptor.capture());
  //        final MessageProcessor<LogMessage> value = messageProcessorCaptor.getValue();
  //        value.process(logMessage);
  //
  //        verify(lineConsumer).writeLine(eq("Error of injection public ssh keys. FAILED"));
  //    }
}

/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.machine.server.ssh;

import static org.eclipse.che.api.workspace.shared.Constants.SERVER_EXEC_AGENT_HTTP_REFERENCE;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.agent.exec.client.ExecAgentClient;
import org.eclipse.che.agent.exec.client.ExecAgentClientFactory;
import org.eclipse.che.agent.exec.shared.dto.GetProcessResponseDto;
import org.eclipse.che.agent.exec.shared.dto.ProcessStartResponseDto;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.ssh.server.SshManager;
import org.eclipse.che.api.ssh.server.model.impl.SshPairImpl;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.shared.dto.MachineDto;
import org.eclipse.che.api.workspace.shared.dto.RuntimeDto;
import org.eclipse.che.api.workspace.shared.dto.ServerDto;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Test for {@link KeysInjector}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class KeysInjectorTest {

  private static final String WORKSPACE_ID = "workspace123";
  private static final String OWNER = "user123";

  @Mock private WorkspaceImpl workspace;
  @Mock private RuntimeDto runtime;

  @Mock private SshManager sshManager;
  @Mock private WorkspaceManager workspaceManager;
  @Mock private ExecAgentClient client;
  @Mock private ExecAgentClientFactory execAgentClientFactory;

  private KeysInjector keysInjector;

  @BeforeMethod
  public void setUp() throws Exception {
    keysInjector = new KeysInjector(sshManager, workspaceManager, execAgentClientFactory);

    when(execAgentClientFactory.create(anyString())).thenReturn(client);
    when(client.startProcess(anyString(), anyString(), anyString(), anyString()))
        .thenReturn(newDto(ProcessStartResponseDto.class).withPid(100));
    when(client.getProcess(anyString(), anyInt()))
        .thenReturn(
            newDto(GetProcessResponseDto.class).withAlive(false).withPid(100).withExitCode(0));
    prepareAndMockServers();

    EnvironmentContext context = new EnvironmentContext();
    context.setSubject(new SubjectImpl("name", OWNER, "token123", false));
    EnvironmentContext.setCurrent(context);
  }

  @Test
  public void shouldNotInjectSshKeysWhenThereAreNotAnyPair() throws Exception {
    when(sshManager.getPairs(anyString(), anyString())).thenReturn(Collections.emptyList());

    keysInjector.injectPublicKeys(WORKSPACE_ID);

    verify(sshManager).getPairs(eq(OWNER), eq("machine"));
    verify(sshManager).getPair(eq(OWNER), eq("workspace"), eq(WORKSPACE_ID));
    verifyZeroInteractions(execAgentClientFactory, sshManager);
  }

  @Test
  public void shouldNotInjectSshKeysWhenThereAreNotAnyPairWithPublicKey() throws Exception {
    when(sshManager.getPairs(anyString(), anyString()))
        .thenReturn(
            Collections.singletonList(new SshPairImpl(OWNER, "machine", "myPair", null, null)));

    keysInjector.injectPublicKeys(WORKSPACE_ID);

    verify(sshManager).getPairs(eq(OWNER), eq("machine"));
    verify(sshManager).getPair(eq(OWNER), eq("workspace"), eq(WORKSPACE_ID));
    verifyZeroInteractions(execAgentClientFactory, sshManager);
  }

  @Test
  public void shouldInjectSshKeysWhenThereAreAnyPairWithNotNullPublicKey() throws Exception {
    when(sshManager.getPairs(anyString(), anyString()))
        .thenReturn(
            Arrays.asList(
                new SshPairImpl(OWNER, "machine", "myPair", "publicKey1", null),
                new SshPairImpl(OWNER, "machine", "myPair", "publicKey2", null)));

    keysInjector.injectPublicKeys(WORKSPACE_ID);

    verify(sshManager).getPairs(eq(OWNER), eq("machine"));
    verify(sshManager).getPair(eq(OWNER), eq("workspace"), eq(WORKSPACE_ID));

    ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
    verify(client, times(2))
        .startProcess(eq(WORKSPACE_ID), argumentCaptor.capture(), anyString(), anyString());
    assertEquals(
        "mkdir ~/.ssh/ -p"
            + " && echo 'publicKey1' >> ~/.ssh/authorized_keys"
            + " && echo 'publicKey2' >> ~/.ssh/authorized_keys",
        argumentCaptor.getValue());
  }

  /**
   * Validate the usecase: There is a workspace sshkeypair but no machine keypair (empty list)
   * Expect that the workspace public key is injected.
   */
  @Test
  public void shouldInjectSshKeysWhenThereIsOnlyWorkspaceKey() throws Exception {
    // no machine key pairs
    when(sshManager.getPairs(anyString(), eq("machine"))).thenReturn(Collections.emptyList());

    // workspace keypair
    when(sshManager.getPair(anyString(), eq("workspace"), anyString()))
        .thenReturn(new SshPairImpl(OWNER, "workspace", WORKSPACE_ID, "publicKeyWorkspace", null));

    keysInjector.injectPublicKeys(WORKSPACE_ID);
    // check calls for machine and workspace ssh pairs
    verify(sshManager).getPairs(eq(OWNER), eq("machine"));
    verify(sshManager).getPair(eq(OWNER), eq("workspace"), eq(WORKSPACE_ID));

    ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
    verify(client, times(2))
        .startProcess(eq(WORKSPACE_ID), argumentCaptor.capture(), anyString(), anyString());
    assertEquals(
        "mkdir ~/.ssh/ -p" + " && echo 'publicKeyWorkspace' >> ~/.ssh/authorized_keys",
        argumentCaptor.getValue());
  }

  /**
   * Validate the usecase: There is a workspace sshkeypair (without public key) but there is a
   * machine keypair Expect that only the machine keypair is injected (as workspace keypair has no
   * public key).
   */
  @Test
  public void shouldInjectSshKeysWhenThereIsNoPublicWorkspaceKeyButMachineKeys() throws Exception {
    // no machine key pairs
    when(sshManager.getPairs(anyString(), eq("machine")))
        .thenReturn(
            Collections.singletonList(
                new SshPairImpl(OWNER, "machine", "myPair", "publicKey1", null)));

    // workspace keypair without public key
    when(sshManager.getPair(anyString(), eq("workspace"), anyString()))
        .thenReturn(new SshPairImpl(OWNER, "workspace", WORKSPACE_ID, null, null));

    keysInjector.injectPublicKeys(WORKSPACE_ID);
    // check calls for machine and workspace ssh pairs
    verify(sshManager).getPairs(eq(OWNER), eq("machine"));
    verify(sshManager).getPair(eq(OWNER), eq("workspace"), eq(WORKSPACE_ID));

    ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
    verify(client, times(2))
        .startProcess(eq(WORKSPACE_ID), argumentCaptor.capture(), anyString(), anyString());
    assertEquals(
        "mkdir ~/.ssh/ -p" + " && echo 'publicKey1' >> ~/.ssh/authorized_keys",
        argumentCaptor.getValue());
  }

  /**
   * Validate the usecase of no workspace keypair (notfound exception) and no machine keypair Expect
   * no ssh keys are injected
   */
  @Test
  public void shouldNotInjectSshKeysWhenThereIsNoWorkspaceKey() throws Exception {
    // no machine key pairs
    when(sshManager.getPairs(anyString(), eq("machine"))).thenReturn(Collections.emptyList());

    // no workspace keypair
    when(sshManager.getPair(anyString(), eq("workspace"), anyString()))
        .thenThrow(NotFoundException.class);

    keysInjector.injectPublicKeys(WORKSPACE_ID);

    // check calls for machine and workspace ssh pairs
    verify(sshManager).getPairs(eq(OWNER), eq("machine"));
    verify(sshManager).getPair(eq(OWNER), eq("workspace"), eq(WORKSPACE_ID));

    verifyZeroInteractions(execAgentClientFactory, sshManager);
  }

  private void prepareAndMockServers() throws ServerException, NotFoundException {
    ServerDto server1 = newDto(ServerDto.class).withUrl("http://localhost:38001/exec");
    ServerDto server2 = newDto(ServerDto.class).withUrl("http://localhost:38002/exec");
    Map<String, ServerDto> map1 = new HashMap<>();
    Map<String, ServerDto> map2 = new HashMap<>();
    map1.put(SERVER_EXEC_AGENT_HTTP_REFERENCE, server1);
    map2.put(SERVER_EXEC_AGENT_HTTP_REFERENCE, server2);
    MachineDto machine1 = newDto(MachineDto.class).withServers(map1);
    MachineDto machine2 = newDto(MachineDto.class).withServers(map2);
    Map<String, MachineDto> machineMap = new HashMap<>();
    machineMap.put("machine1", machine1);
    machineMap.put("machine2", machine2);

    when(workspaceManager.getWorkspace(anyString())).thenReturn(workspace);
    when(workspace.getRuntime()).thenReturn(runtime);
    when(runtime.getMachines()).thenReturn(machineMap);
  }
}

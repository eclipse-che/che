/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.cache.tck;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.eclipse.che.workspace.infrastructure.kubernetes.cache.tck.TestObjects.createRuntimeState;
import static org.eclipse.che.workspace.infrastructure.kubernetes.cache.tck.TestObjects.createWorkspace;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.config.Command;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.test.tck.TckListener;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.KubernetesRuntimeStateCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesRuntimeState;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link KubernetesRuntimeStateCache} contract.
 *
 * @author Sergii Leshchenko
 */
@Listeners(TckListener.class)
@Test(suiteName = KubernetesRuntimeStateCacheTest.SUITE_NAME)
public class KubernetesRuntimeStateCacheTest {

  public static final String SUITE_NAME = "KubernetesRuntimeStateCacheTck";

  @Inject private TckRepository<WorkspaceImpl> workspaceTckRepository;
  @Inject private TckRepository<AccountImpl> accountRepository;
  @Inject private TckRepository<KubernetesRuntimeState> runtimesRepository;

  @Inject private KubernetesRuntimeStateCache runtimesStatesCache;

  @Inject private EventService eventService;

  private WorkspaceImpl[] workspaces;
  private KubernetesRuntimeState[] runtimesStates;

  @BeforeMethod
  public void setUp() throws TckRepositoryException {
    workspaces = new WorkspaceImpl[] {createWorkspace(), createWorkspace(), createWorkspace()};

    AccountImpl[] accounts =
        new AccountImpl[] {
          workspaces[0].getAccount(), workspaces[1].getAccount(), workspaces[2].getAccount()
        };

    accountRepository.createAll(asList(accounts));
    workspaceTckRepository.createAll(asList(workspaces));

    runtimesStates =
        new KubernetesRuntimeState[] {
          createRuntimeState(workspaces[0]), createRuntimeState(workspaces[1])
        };

    runtimesRepository.createAll(asList(runtimesStates));
  }

  @AfterMethod
  public void removeEntities() throws TckRepositoryException {
    runtimesRepository.removeAll();

    workspaceTckRepository.removeAll();
    accountRepository.removeAll();
  }

  @Test
  public void shouldReturnRuntimesIdentities() throws Exception {
    // when
    Set<RuntimeIdentity> identities = runtimesStatesCache.getIdentities();

    // then
    assertEquals(identities.size(), 2);
    assertTrue(identities.contains(runtimesStates[0].getRuntimeId()));
    assertTrue(identities.contains(runtimesStates[1].getRuntimeId()));
  }

  @Test
  public void shouldReturnCommands() throws Exception {
    // when
    List<? extends Command> commands =
        runtimesStatesCache.getCommands(runtimesStates[0].getRuntimeId());

    // then
    assertEquals(commands.size(), runtimesStates[0].getCommands().size());
    assertTrue(commands.containsAll(runtimesStates[0].getCommands()));
  }

  @Test
  public void shouldReturnEmptyCommandsListIfStateDoesNotExist() throws Exception {
    // when
    List<? extends Command> commands =
        runtimesStatesCache.getCommands(
            new RuntimeIdentityImpl("non-existent-ws", "defEnv", "acc1", "infraNamespace"));

    // then
    assertTrue(commands.isEmpty());
  }

  @Test(dependsOnMethods = "shouldReturnCommands")
  public void shouldUpdateCommands() throws Exception {
    // given
    List<CommandImpl> newCommands = new ArrayList<>();
    CommandImpl newCommand = new CommandImpl("new", "build", "custom");
    newCommands.add(newCommand);

    // when
    runtimesStatesCache.updateCommands(runtimesStates[0].getRuntimeId(), newCommands);

    // then
    List<? extends Command> updatedCommands =
        runtimesStatesCache.getCommands(runtimesStates[0].getRuntimeId());
    assertEquals(updatedCommands.size(), 1);
    assertEquals(new CommandImpl(updatedCommands.get(0)), newCommand);
  }

  // Ensure that we are not affected https://bugs.eclipse.org/bugs/show_bug.cgi?id=474203 Orphan
  // Removal not working
  // when, object is added to collection and then same object is removed from collection in same
  // transaction.
  //
  // Probable reason - two different transactions was used.
  @Test(dependsOnMethods = "shouldReturnCommands")
  public void shouldUpdateCommandsAndDeleteRuntime() {
    // given
    List<CommandImpl> newCommands = new ArrayList<>();
    CommandImpl newCommand = new CommandImpl("new", "build", "custom");
    newCommands.add(newCommand);

    // when
    try {
      runtimesStatesCache.updateCommands(runtimesStates[0].getRuntimeId(), newCommands);
      runtimesStatesCache.remove(runtimesStates[0].getRuntimeId());
    } catch (InfrastructureException e) {
      fail("No exception expected here, got " + e.getLocalizedMessage());
    }
    // then
    // if no exception happened during remove operation that means test passed correctly.
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp =
          "Runtime state for workspace with id 'non-existent-ws' was not found")
  public void shouldThrowExceptionUpdateCommands() throws Exception {
    // given
    CommandImpl newCommand = new CommandImpl("new", "build", "custom");

    // when
    runtimesStatesCache.updateCommands(
        new RuntimeIdentityImpl("non-existent-ws", "defEnv", "acc1", "infraNamespace"),
        singletonList(newCommand));
  }

  @Test
  public void shouldReturnRuntimeStateByRuntimeId() throws Exception {
    // given
    KubernetesRuntimeState expectedState = runtimesStates[1];

    // when
    Optional<KubernetesRuntimeState> fetchedOpt =
        runtimesStatesCache.get(expectedState.getRuntimeId());

    // then
    assertTrue(fetchedOpt.isPresent());
    assertEquals(expectedState, fetchedOpt.get());
  }

  @Test
  public void shouldReturnEmptyOptionalIfRuntimeStateIsNotFound() throws Exception {
    // given
    KubernetesRuntimeState nonExisting = createRuntimeState(workspaces[2]);

    // when
    Optional<KubernetesRuntimeState> fetchedOpt =
        runtimesStatesCache.get(nonExisting.getRuntimeId());

    // then
    assertFalse(fetchedOpt.isPresent());
  }

  @Test
  public void shouldReturnRuntimeStatus() throws Exception {
    // when
    Optional<WorkspaceStatus> statusOpt =
        runtimesStatesCache.getStatus(runtimesStates[0].getRuntimeId());

    // then
    assertTrue(statusOpt.isPresent());
    assertEquals(runtimesStates[0].getStatus(), statusOpt.get());
  }

  @Test
  public void shouldReturnEmptyOptionalWhenThereIsNotRuntimeStateWhileStatusRetrieving()
      throws Exception {
    // when
    Optional<WorkspaceStatus> statusOpt =
        runtimesStatesCache.getStatus(
            new RuntimeIdentityImpl("non-existent-ws", "defEnv", "acc1", "infraNamespace"));

    // then
    assertFalse(statusOpt.isPresent());
  }

  @Test(dependsOnMethods = "shouldReturnRuntimeStatus")
  public void shouldUpdateStatus() throws Exception {
    // given
    KubernetesRuntimeState stateToUpdate = runtimesStates[0];

    // when
    runtimesStatesCache.updateStatus(stateToUpdate.getRuntimeId(), WorkspaceStatus.STOPPED);

    // then
    Optional<WorkspaceStatus> updatedStatusOpt =
        runtimesStatesCache.getStatus(stateToUpdate.getRuntimeId());
    assertTrue(updatedStatusOpt.isPresent());
    assertEquals(updatedStatusOpt.get(), WorkspaceStatus.STOPPED);
    assertNotEquals(stateToUpdate, WorkspaceStatus.STOPPED);
  }

  @Test(dependsOnMethods = "shouldReturnRuntimeStatus")
  public void shouldUpdateStatusIfPreviousValueMatchesPredicate() throws Exception {
    // given
    KubernetesRuntimeState stateToUpdate = runtimesStates[0];

    // when
    boolean isUpdated =
        runtimesStatesCache.updateStatus(
            stateToUpdate.getRuntimeId(),
            s -> s == stateToUpdate.getStatus(),
            WorkspaceStatus.STOPPED);

    // then
    assertTrue(isUpdated);
    Optional<WorkspaceStatus> updatedStatusOpt =
        runtimesStatesCache.getStatus(stateToUpdate.getRuntimeId());
    assertTrue(updatedStatusOpt.isPresent());
    assertEquals(updatedStatusOpt.get(), WorkspaceStatus.STOPPED);
    assertNotEquals(stateToUpdate, WorkspaceStatus.STOPPED);
  }

  @Test(dependsOnMethods = "shouldReturnRuntimeStatus")
  public void shouldNotUpdateStatusIfPreviousValueDoesNotMatchesPredicate() throws Exception {
    // given
    KubernetesRuntimeState stateToUpdate = runtimesStates[0];

    // when
    boolean isUpdated =
        runtimesStatesCache.updateStatus(
            stateToUpdate.getRuntimeId(),
            s -> s == WorkspaceStatus.STARTING,
            WorkspaceStatus.STOPPED);

    // then
    assertFalse(isUpdated);
    Optional<WorkspaceStatus> updatedStatusOpt =
        runtimesStatesCache.getStatus(stateToUpdate.getRuntimeId());
    assertTrue(updatedStatusOpt.isPresent());
    assertEquals(updatedStatusOpt.get(), WorkspaceStatus.RUNNING);
    assertEquals(stateToUpdate.getStatus(), WorkspaceStatus.RUNNING);
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp =
          "Runtime state for workspace with id 'non-existent-ws' was not found")
  public void shouldThrowExceptionWhenThereIsNotRuntimeStateWhileStatusUpdatingWithoutPredicate()
      throws Exception {
    // when
    runtimesStatesCache.updateStatus(
        new RuntimeIdentityImpl("non-existent-ws", "defEnv", "acc1", "infraNamespace"),
        WorkspaceStatus.STOPPED);
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp =
          "Runtime state for workspace with id 'non-existent-ws' was not found")
  public void shouldThrowExceptionWhenThereIsNotRuntimeStateWhileStatusUpdatingWithPredicate()
      throws Exception {
    // when
    runtimesStatesCache.updateStatus(
        new RuntimeIdentityImpl("non-existent-ws", "defEnv", "acc1", "infraNamespace"),
        s -> s.equals(WorkspaceStatus.STOPPING),
        WorkspaceStatus.STOPPED);
  }

  @Test(dependsOnMethods = "shouldReturnRuntimeStateByRuntimeId")
  public void shouldPutRuntimeState() throws Exception {
    // given
    KubernetesRuntimeState runtimeState = createRuntimeState(workspaces[2]);

    // when
    boolean isInserted = runtimesStatesCache.putIfAbsent(runtimeState);

    // then
    assertTrue(isInserted);
    Optional<KubernetesRuntimeState> fetchedState =
        runtimesStatesCache.get(runtimeState.getRuntimeId());
    assertTrue(fetchedState.isPresent());
    assertEquals(runtimeState, fetchedState.get());
  }

  @Test(dependsOnMethods = "shouldReturnRuntimeStateByRuntimeId")
  public void shouldNotPutRuntimeStateIfRuntimeStateIsAlreadyPut() throws Exception {
    // given
    KubernetesRuntimeState runtimeState = createRuntimeState(workspaces[0]);

    // when
    boolean isInserted = runtimesStatesCache.putIfAbsent(runtimeState);

    // then
    assertFalse(isInserted);
    Optional<KubernetesRuntimeState> fetchedState =
        runtimesStatesCache.get(runtimeState.getRuntimeId());
    assertTrue(fetchedState.isPresent());
    assertEquals(runtimesStates[0], fetchedState.get());
  }

  @Test(dependsOnMethods = "shouldReturnEmptyOptionalIfRuntimeStateIsNotFound")
  public void shouldRemoveRuntimeState() throws Exception {
    // given
    KubernetesRuntimeState runtimeState = createRuntimeState(workspaces[0]);
    RuntimeIdentity toRemove = runtimeState.getRuntimeId();

    // when
    runtimesStatesCache.remove(toRemove);

    // then
    assertFalse(runtimesStatesCache.get(toRemove).isPresent());
  }

  @Test(dependsOnMethods = "shouldReturnEmptyOptionalIfRuntimeStateIsNotFound")
  public void shouldDoNothingIfStateIsAlreadyRemove() throws Exception {
    // given
    KubernetesRuntimeState runtimeState = createRuntimeState(workspaces[2]);
    RuntimeIdentity toRemove = runtimeState.getRuntimeId();

    // when
    runtimesStatesCache.remove(toRemove);

    // then
    assertFalse(runtimesStatesCache.get(toRemove).isPresent());
  }
}

/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.cache.tck;

import static java.util.Arrays.asList;
import static org.eclipse.che.workspace.infrastructure.kubernetes.cache.tck.TestObjects.createRuntimeState;
import static org.eclipse.che.workspace.infrastructure.kubernetes.cache.tck.TestObjects.createWorkspace;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.commons.test.tck.TckListener;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.KubernetesRuntimeStateCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesRuntimeState;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesRuntimeState.RuntimeId;
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
    WorkspaceStatus status = runtimesStatesCache.getStatus(runtimesStates[0].getRuntimeId());

    // then
    assertEquals(runtimesStates[0].getStatus(), status);
  }

  @Test
  public void shouldThrowExceptionWhenThereIsNotStateForSpecifiedRuntimeId() throws Exception {
    // when
    WorkspaceStatus status = runtimesStatesCache.getStatus(runtimesStates[0].getRuntimeId());

    // then
    assertEquals(runtimesStates[0].getStatus(), status);
  }

  @Test(dependsOnMethods = "shouldReturnRuntimeStatus")
  public void shouldUpdateStatus() throws Exception {
    // given
    KubernetesRuntimeState stateToUpdate = runtimesStates[0];

    // when
    runtimesStatesCache.updateStatus(stateToUpdate.getRuntimeId(), WorkspaceStatus.STOPPED);

    // then
    WorkspaceStatus updatedStatus = runtimesStatesCache.getStatus(stateToUpdate.getRuntimeId());
    assertEquals(updatedStatus, WorkspaceStatus.STOPPED);
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
    WorkspaceStatus updatedStatus = runtimesStatesCache.getStatus(stateToUpdate.getRuntimeId());
    assertEquals(updatedStatus, WorkspaceStatus.STOPPED);
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
    WorkspaceStatus updatedStatus = runtimesStatesCache.getStatus(stateToUpdate.getRuntimeId());
    assertEquals(updatedStatus, WorkspaceStatus.RUNNING);
    assertEquals(stateToUpdate.getStatus(), WorkspaceStatus.RUNNING);
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
    RuntimeId toRemove = runtimeState.getRuntimeId();

    // when
    runtimesStatesCache.remove(toRemove);

    // then
    assertFalse(runtimesStatesCache.get(toRemove).isPresent());
  }

  @Test(dependsOnMethods = "shouldReturnEmptyOptionalIfRuntimeStateIsNotFound")
  public void shouldDoNothingIfStateIsAlreadyRemove() throws Exception {
    // given
    KubernetesRuntimeState runtimeState = createRuntimeState(workspaces[2]);
    RuntimeId toRemove = runtimeState.getRuntimeId();

    // when
    runtimesStatesCache.remove(toRemove);

    // then
    assertFalse(runtimesStatesCache.get(toRemove).isPresent());
  }
}

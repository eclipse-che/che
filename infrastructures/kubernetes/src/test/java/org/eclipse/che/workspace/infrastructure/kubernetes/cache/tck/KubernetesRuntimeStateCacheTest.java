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

import java.util.Set;
import javax.inject.Inject;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
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

  private KubernetesRuntimeState[] runtimesStates;

  @BeforeMethod
  public void setUp() throws TckRepositoryException {
    WorkspaceImpl[] workspaces = new WorkspaceImpl[] {createWorkspace()};

    AccountImpl[] accounts = new AccountImpl[] {workspaces[0].getAccount()};

    accountRepository.createAll(asList(accounts));
    workspaceTckRepository.createAll(asList(workspaces));

    runtimesStates = new KubernetesRuntimeState[] {createRuntimeState(workspaces[0])};

    runtimesRepository.createAll(asList(runtimesStates));
  }

  @AfterMethod
  public void removeEntities() throws TckRepositoryException {
    runtimesRepository.removeAll();

    workspaceTckRepository.removeAll();
    accountRepository.removeAll();
  }

  // TODO Cover all methods

  @Test
  public void shouldReturnRuntimesIdentities() throws Exception {
    Set<RuntimeIdentity> identities = runtimesStatesCache.getIdentities();

    assertEquals(identities.size(), 1);
  }
}

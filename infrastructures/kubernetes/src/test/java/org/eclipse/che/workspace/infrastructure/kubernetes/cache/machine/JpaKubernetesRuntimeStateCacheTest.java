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
package org.eclipse.che.workspace.infrastructure.kubernetes.cache.machine;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;

import java.util.Set;
import javax.inject.Inject;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.commons.test.tck.TckListener;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.KubernetesRuntimeStateCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesRuntimeState;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesRuntimeState.RuntimeId;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Sergii Leshchenko */
@Listeners(TckListener.class)
@Test(suiteName = "K8sRuntimeTest")
public class JpaKubernetesRuntimeStateCacheTest {

  @Inject private KubernetesRuntimeStateCache kubernetesRuntimesStates;

  @Inject private TckRepository<KubernetesRuntimeState> runtimeRepository;

  private KubernetesRuntimeState[] runtimes;

  @BeforeMethod
  public void setUp() throws TckRepositoryException {
    runtimes =
        new KubernetesRuntimeState[] {
          new KubernetesRuntimeState(
              new RuntimeId("ws123", "envName", "ownerId"), "namespace", WorkspaceStatus.RUNNING)
        };

    runtimeRepository.createAll(asList(runtimes));
  }

  @Test
  public void shouldReturnRuntimesIdentities() throws Exception {
    Set<RuntimeIdentity> identities = kubernetesRuntimesStates.getIdentities();

    assertEquals(identities.size(), 1);
  }
}

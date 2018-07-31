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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodSecurityContext;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Test {@link SecurityContextProvisioner}.
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class SecurityContextProvisionerTest {

  @Mock private RuntimeIdentity runtimeIdentity;

  private KubernetesEnvironment kubernetesEnvironment;
  private Pod pod;

  private SecurityContextProvisioner securityContextProvisioner;

  @BeforeMethod
  public void setUp() throws Exception {
    pod = new PodBuilder().withNewSpec().endSpec().build();

    kubernetesEnvironment =
        KubernetesEnvironment.builder().setPods(ImmutableMap.of("pod", pod)).build();
  }

  @Test
  public void shouldNotProvisionSecurityContextIfItIsNotConfigured() throws Exception {
    // given
    securityContextProvisioner = new SecurityContextProvisioner(null, null);

    // when
    securityContextProvisioner.provision(kubernetesEnvironment, runtimeIdentity);

    // then
    assertNull(pod.getSpec().getSecurityContext());
  }

  @Test
  public void shouldProvisionSecurityContextIfItIsConfigured() throws Exception {
    // given
    securityContextProvisioner = new SecurityContextProvisioner("1", "2");

    // when
    securityContextProvisioner.provision(kubernetesEnvironment, runtimeIdentity);

    // then
    PodSecurityContext securityContext = pod.getSpec().getSecurityContext();
    assertNotNull(securityContext);

    assertEquals(securityContext.getRunAsUser(), new Long(1));
    assertEquals(securityContext.getFsGroup(), new Long(2));
  }
}

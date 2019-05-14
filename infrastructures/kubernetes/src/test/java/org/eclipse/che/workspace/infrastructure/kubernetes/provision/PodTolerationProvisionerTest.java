/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link PodTolerationProvisioner}.
 *
 * @author Masak Muranaka
 */
@Listeners(MockitoTestNGListener.class)
public class PodTolerationProvisionerTest {

  @Mock private KubernetesEnvironment k8sEnv;
  @Mock private RuntimeIdentity runtimeIdentity;

  @Test
  public void doNothingWhenSttringIsNull() throws Exception {
    // given
    PodTolerationProvisioner podTolerationProvisioner = new PodTolerationProvisioner(null);

    // when
    podTolerationProvisioner.provision(k8sEnv, runtimeIdentity);

    // then
    verify(k8sEnv, never()).getIngresses();
  }

  @Test
  public void doNothingWhenSttringIsEmpty() throws Exception {
    // given
    PodTolerationProvisioner podTolerationProvisioner = new PodTolerationProvisioner("");

    // when
    podTolerationProvisioner.provision(k8sEnv, runtimeIdentity);

    // then
    verify(k8sEnv, never()).getIngresses();
  }

  @Test
  public void justOnlyEffectIsSpecified() throws Exception {
    // given
    PodTolerationProvisioner podTolerationProvisioner = new PodTolerationProvisioner("NoSchedule");

    // when
    podTolerationProvisioner.provision(k8sEnv, runtimeIdentity);

    // then
    for (PodData pod : k8sEnv.getPodsData().values()) {
      assertEquals(pod.getSpec().getTolerations().size(), 1);
      System.err.println(pod.getSpec().getTolerations());
      assertEquals(pod.getSpec().getTolerations().get(0).getEffect(), "NoSchedule");
    }
  }

  @Test
  public void commaOnly() throws Exception {
    // given
    PodTolerationProvisioner podTolerationProvisioner = new PodTolerationProvisioner(",,,,");

    // when
    podTolerationProvisioner.provision(k8sEnv, runtimeIdentity);

    // then
    for (PodData pod : k8sEnv.getPodsData().values()) {
      assertEquals(pod.getSpec().getTolerations().size(), 0);
    }
  }

  @Test
  public void azureAKSwithACI() throws Exception {
    // given
    PodTolerationProvisioner podTolerationProvisioner =
        new PodTolerationProvisioner(":virtual-kubelet.io/provider:Exist,NoSchedule:azure.com/aci");

    // when
    podTolerationProvisioner.provision(k8sEnv, runtimeIdentity);

    // then
    for (PodData pod : k8sEnv.getPodsData().values()) {
      assertEquals(pod.getSpec().getTolerations().size(), 2);
      assertEquals(pod.getSpec().getTolerations().get(0).getEffect(), null);
      assertEquals(pod.getSpec().getTolerations().get(0).getKey(), "virtual-kubelet.io/provider");
      assertEquals(pod.getSpec().getTolerations().get(0).getOperator(), "Exist");
      assertEquals(pod.getSpec().getTolerations().get(0).getTolerationSeconds(), null);
      assertEquals(pod.getSpec().getTolerations().get(0).getValue(), null);
      assertEquals(pod.getSpec().getTolerations().get(1).getEffect(), "NoSchedule");
      assertEquals(pod.getSpec().getTolerations().get(1).getKey(), "azure.com/aci");
      assertEquals(pod.getSpec().getTolerations().get(1).getOperator(), null);
      assertEquals(pod.getSpec().getTolerations().get(1).getTolerationSeconds(), null);
      assertEquals(pod.getSpec().getTolerations().get(1).getValue(), null);
    }
  }

  @Test
  public void multiRecord() throws Exception {
    // given
    PodTolerationProvisioner podTolerationProvisioner =
        new PodTolerationProvisioner(
            "NoSchedule:key1:operator1:1:value1,NoExecute:key2:operator2:2:value2");

    // when
    podTolerationProvisioner.provision(k8sEnv, runtimeIdentity);

    // then
    for (PodData pod : k8sEnv.getPodsData().values()) {
      assertEquals(pod.getSpec().getTolerations().size(), 2);
      assertEquals(pod.getSpec().getTolerations().get(0).getEffect(), "NoSchedule");
      assertEquals(pod.getSpec().getTolerations().get(0).getKey(), "key1");
      assertEquals(pod.getSpec().getTolerations().get(0).getOperator(), "operator1");
      assertEquals(pod.getSpec().getTolerations().get(0).getTolerationSeconds(), Long.valueOf(1L));
      assertEquals(pod.getSpec().getTolerations().get(0).getValue(), "value1");
      assertEquals(pod.getSpec().getTolerations().get(1).getEffect(), "NoExecute");
      assertEquals(pod.getSpec().getTolerations().get(1).getKey(), "key2");
      assertEquals(pod.getSpec().getTolerations().get(1).getOperator(), "operator2");
      assertEquals(pod.getSpec().getTolerations().get(1).getTolerationSeconds(), Long.valueOf(2L));
      assertEquals(pod.getSpec().getTolerations().get(1).getValue(), "value2");
    }
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void moreThanFiveFields() throws Exception {
    // given
    PodTolerationProvisioner podTolerationProvisioner =
        new PodTolerationProvisioner("NoSchedule:key:operator:1:value:shouldBeError");

    // when / then
    podTolerationProvisioner.provision(k8sEnv, runtimeIdentity);
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void tolerationSecondsNotLong() throws Exception {
    // given
    PodTolerationProvisioner podTolerationProvisioner =
        new PodTolerationProvisioner("NoSchedule:key:operator:notLong:value");

    // when / then
    podTolerationProvisioner.provision(k8sEnv, runtimeIdentity);
  }
}

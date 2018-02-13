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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision.restartpolicy;

import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.restartpolicy.RestartPolicyRewriter.DEFAULT_RESTART_POLICY;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;
import java.util.Iterator;
import java.util.List;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.WarningImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Alexander Garagatyi */
@Listeners(MockitoTestNGListener.class)
public class RestartPolicyRewriterTest {

  private static final String TEST_POD_NAME = "app";
  private static final String ALWAYS_RESTART_POLICY = "Always";

  @Mock private KubernetesEnvironment k8sEnv;
  @Mock private RuntimeIdentity runtimeIdentity;
  @InjectMocks private RestartPolicyRewriter restartPolicyRewriter;

  @Captor private ArgumentCaptor<Warning> warningCaptor;

  @Test
  public void rewritesRestartPolicyWhenItsDifferentWithDefaultOne() throws Exception {
    when(k8sEnv.getPods())
        .thenReturn(singletonMap(TEST_POD_NAME, newPod(TEST_POD_NAME, ALWAYS_RESTART_POLICY)));

    restartPolicyRewriter.provision(k8sEnv, runtimeIdentity);

    assertEquals(
        k8sEnv.getPods().get(TEST_POD_NAME).getSpec().getRestartPolicy(), DEFAULT_RESTART_POLICY);
    verifyWarnings(
        new WarningImpl(
            101,
            format(
                "Restart policy '%s' for pod '%s' is rewritten with %s",
                ALWAYS_RESTART_POLICY, TEST_POD_NAME, DEFAULT_RESTART_POLICY)));
  }

  private static Pod newPod(String podName, String restartPolicy, Container... containers) {
    final ObjectMeta podMetadata = new ObjectMetaBuilder().withName(podName).build();
    final PodSpec podSpec =
        new PodSpecBuilder().withRestartPolicy(restartPolicy).withContainers(containers).build();
    return new PodBuilder().withMetadata(podMetadata).withSpec(podSpec).build();
  }

  private void verifyWarnings(Warning... expectedWarnings) {
    final Iterator<Warning> actualWarnings = captureWarnings().iterator();
    for (Warning expected : expectedWarnings) {
      if (!actualWarnings.hasNext()) {
        fail("It is expected to receive environment warning");
      }
      final Warning actual = actualWarnings.next();
      assertEquals(actual, expected);
    }
    if (actualWarnings.hasNext()) {
      fail("No more warnings expected");
    }
  }

  private List<Warning> captureWarnings() {
    verify(k8sEnv, atLeastOnce()).addWarning(warningCaptor.capture());
    return warningCaptor.getAllValues();
  }
}

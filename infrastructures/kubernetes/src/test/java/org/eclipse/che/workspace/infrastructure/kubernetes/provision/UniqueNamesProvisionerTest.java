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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_ORIGINAL_NAME_LABEL;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressBuilder;
import java.util.HashMap;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link UniqueNamesProvisioner}.
 *
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class UniqueNamesProvisionerTest {

  private static final String WORKSPACE_ID = "workspace37";
  private static final String POD_NAME = "testPod";
  private static final String INGRESS_NAME = "testIngress";

  @Mock private KubernetesEnvironment k8sEnv;
  @Mock private RuntimeIdentity runtimeIdentity;

  private UniqueNamesProvisioner<KubernetesEnvironment> uniqueNamesProvisioner;

  @BeforeMethod
  public void setup() {
    uniqueNamesProvisioner = new UniqueNamesProvisioner<>();
  }

  @Test
  public void provideUniquePodsNames() throws Exception {
    when(runtimeIdentity.getWorkspaceId()).thenReturn(WORKSPACE_ID);
    final HashMap<String, Pod> pods = new HashMap<>();
    Pod pod = newPod();
    pods.put(POD_NAME, pod);
    doReturn(pods).when(k8sEnv).getPods();

    uniqueNamesProvisioner.provision(k8sEnv, runtimeIdentity);

    ObjectMeta podMetadata = pod.getMetadata();
    assertNotEquals(podMetadata.getName(), POD_NAME);
    assertEquals(podMetadata.getLabels().get(CHE_ORIGINAL_NAME_LABEL), POD_NAME);
  }

  @Test
  public void provideUniqueIngressesNames() throws Exception {
    final HashMap<String, Ingress> ingresses = new HashMap<>();
    Ingress ingress = newIngress();
    ingresses.put(POD_NAME, ingress);
    doReturn(ingresses).when(k8sEnv).getIngresses();

    uniqueNamesProvisioner.provision(k8sEnv, runtimeIdentity);

    final ObjectMeta ingressMetadata = ingress.getMetadata();
    assertNotEquals(ingressMetadata.getName(), INGRESS_NAME);
    assertEquals(ingressMetadata.getLabels().get(CHE_ORIGINAL_NAME_LABEL), INGRESS_NAME);
  }

  private static Pod newPod() {
    return new PodBuilder()
        .withMetadata(new ObjectMetaBuilder().withName(POD_NAME).build())
        .build();
  }

  private static Ingress newIngress() {
    return new IngressBuilder()
        .withMetadata(new ObjectMetaBuilder().withName(INGRESS_NAME).build())
        .build();
  }
}

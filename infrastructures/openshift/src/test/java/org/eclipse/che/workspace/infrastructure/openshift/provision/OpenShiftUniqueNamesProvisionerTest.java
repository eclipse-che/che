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
package org.eclipse.che.workspace.infrastructure.openshift.provision;

import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_ORIGINAL_NAME_LABEL;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteBuilder;
import java.util.HashMap;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link OpenShiftUniqueNamesProvisioner}.
 *
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class OpenShiftUniqueNamesProvisionerTest {

  private static final String WORKSPACE_ID = "workspace37";
  private static final String POD_NAME = "testPod";
  private static final String ROUTE_NAME = "testRoute";

  @Mock private OpenShiftEnvironment osEnv;
  @Mock private RuntimeIdentity runtimeIdentity;

  private OpenShiftUniqueNamesProvisioner uniqueNamesProvisioner;

  @BeforeMethod
  public void setup() {
    uniqueNamesProvisioner = new OpenShiftUniqueNamesProvisioner();
  }

  @Test
  public void provideUniquePodsNames() throws Exception {
    when(runtimeIdentity.getWorkspaceId()).thenReturn(WORKSPACE_ID);
    Pod pod = newPod();
    PodData podData = new PodData(pod.getSpec(), pod.getMetadata());
    doReturn(ImmutableMap.of(POD_NAME, podData)).when(osEnv).getPodsData();

    uniqueNamesProvisioner.provision(osEnv, runtimeIdentity);

    ObjectMeta podMetadata = pod.getMetadata();
    assertNotEquals(podMetadata.getName(), POD_NAME);
    assertEquals(podMetadata.getLabels().get(CHE_ORIGINAL_NAME_LABEL), POD_NAME);
  }

  @Test
  public void provideUniqueRoutesNames() throws Exception {
    final HashMap<String, Route> routes = new HashMap<>();
    Route route = newRoute();
    routes.put(POD_NAME, route);
    doReturn(routes).when(osEnv).getRoutes();

    uniqueNamesProvisioner.provision(osEnv, runtimeIdentity);

    final ObjectMeta routeData = route.getMetadata();
    assertNotEquals(routeData.getName(), ROUTE_NAME);
    assertEquals(routeData.getLabels().get(CHE_ORIGINAL_NAME_LABEL), ROUTE_NAME);
  }

  private static Pod newPod() {
    return new PodBuilder()
        .withMetadata(new ObjectMetaBuilder().withName(POD_NAME).build())
        .build();
  }

  private static Route newRoute() {
    return new RouteBuilder()
        .withMetadata(new ObjectMetaBuilder().withName(ROUTE_NAME).build())
        .build();
  }
}

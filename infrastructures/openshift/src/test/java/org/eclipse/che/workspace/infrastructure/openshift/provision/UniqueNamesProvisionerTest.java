/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift.provision;

import static java.util.stream.Collectors.toList;
import static org.eclipse.che.workspace.infrastructure.openshift.provision.UniqueNamesProvisioner.CHE_ORIGINAL_NAME_LABEL;
import static org.eclipse.che.workspace.infrastructure.openshift.provision.UniqueNamesProvisioner.ROUTE_PREFIX;
import static org.eclipse.che.workspace.infrastructure.openshift.provision.UniqueNamesProvisioner.ROUTE_SUFFIX_SIZE;
import static org.eclipse.che.workspace.infrastructure.openshift.provision.UniqueNamesProvisioner.SEPARATOR;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteBuilder;
import java.util.HashMap;
import java.util.regex.Pattern;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InternalEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
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
  private static final String ROUTE_NAME = "testRoute";
  private static final Pattern UNIQUE_ROUTE_NAME_REGEX =
      Pattern.compile('^' + ROUTE_PREFIX + "[A-z0-9]{" + ROUTE_SUFFIX_SIZE + "}$");

  @Mock private InternalEnvironment environment;
  @Mock private OpenShiftEnvironment osEnv;
  @Mock private RuntimeIdentity runtimeIdentity;

  private UniqueNamesProvisioner uniqueNamesProvisioner;

  @BeforeMethod
  public void setup() {
    uniqueNamesProvisioner = new UniqueNamesProvisioner();
  }

  @Test
  public void provideUniquePodsNames() throws Exception {
    when(runtimeIdentity.getWorkspaceId()).thenReturn(WORKSPACE_ID);
    final HashMap<String, Pod> pods = new HashMap<>();
    pods.put(POD_NAME, newPod());
    doReturn(pods).when(osEnv).getPods();

    uniqueNamesProvisioner.provision(environment, osEnv, runtimeIdentity);

    final String expected = WORKSPACE_ID + SEPARATOR + POD_NAME;
    final ObjectMeta podMeta = osEnv.getPods().get(expected).getMetadata();
    assertEquals(podMeta.getName(), expected);
    assertEquals(podMeta.getLabels().get(CHE_ORIGINAL_NAME_LABEL), POD_NAME);
  }

  @Test
  public void provideUniqueRoutesNames() throws Exception {
    final HashMap<String, Route> routes = new HashMap<>();
    routes.put(POD_NAME, newRoute());
    doReturn(routes).when(osEnv).getRoutes();

    uniqueNamesProvisioner.provision(environment, osEnv, runtimeIdentity);

    final ObjectMeta routeData =
        osEnv.getRoutes().values().stream().map(Route::getMetadata).collect(toList()).get(0);
    assertTrue(routeData.getName().startsWith(ROUTE_PREFIX));
    assertTrue(UNIQUE_ROUTE_NAME_REGEX.matcher(routeData.getName()).matches());
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

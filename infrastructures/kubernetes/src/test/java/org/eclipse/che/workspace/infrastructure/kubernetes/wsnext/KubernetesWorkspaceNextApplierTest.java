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
package org.eclipse.che.workspace.infrastructure.kubernetes.wsnext;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.Quantity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.wsnext.model.CheService;
import org.eclipse.che.api.workspace.server.wsnext.model.CheServiceSpec;
import org.eclipse.che.api.workspace.server.wsnext.model.EnvVar;
import org.eclipse.che.api.workspace.server.wsnext.model.ResourceRequirements;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Alexander Garagatyi */
@Listeners(MockitoTestNGListener.class)
public class KubernetesWorkspaceNextApplierTest {
  private static final String TEST_IMAGE = "testImage/test:test";
  private static final String ENV_VAR = "PLUGINS_ENV_VAR";
  private static final String ENV_VAR_VALUE = "PLUGINS_ENV_VAR_VALUE";
  private static final String MEMORY_KEY = "memory";
  private static final String MEMORY_VALUE = "100Mi";
  private static final String POD_NAME = "pod12";
  private static final Map<String, String> RESOURCES_REQUEST =
      ImmutableMap.of(MEMORY_KEY, MEMORY_VALUE);

  @Mock Pod pod;
  @Mock PodSpec podSpec;
  @Mock ObjectMeta meta;
  @Mock KubernetesEnvironment internalEnvironment;

  KubernetesWorkspaceNextApplier applier;
  List<Container> containers;
  Map<String, InternalMachineConfig> machines;

  @BeforeMethod
  public void setUp() {
    applier = new KubernetesWorkspaceNextApplier(200);
    machines = new HashMap<>();
    containers = new ArrayList<>();

    when(internalEnvironment.getPods()).thenReturn(of(POD_NAME, pod));
    when(pod.getSpec()).thenReturn(podSpec);
    when(podSpec.getContainers()).thenReturn(containers);
    when(pod.getMetadata()).thenReturn(meta);
    when(meta.getName()).thenReturn(POD_NAME);
    when(internalEnvironment.getMachines()).thenReturn(machines);
  }

  @Test
  public void doesNothingIfServicesListIsEmpty() throws Exception {
    applier.apply(internalEnvironment, emptyList());

    verifyZeroInteractions(internalEnvironment);
  }

  @Test(
    expectedExceptions = InfrastructureException.class,
    expectedExceptionsMessageRegExp =
        "Workspace.Next configuration can be applied to a workspace with one pod only"
  )
  public void throwsExceptionWhenTheNumberOfPodsIsNot1() throws Exception {
    when(internalEnvironment.getPods()).thenReturn(of("pod1", pod, "pod2", pod));

    applier.apply(internalEnvironment, singletonList(testService()));
  }

  @Test
  public void addToolingContainerToAPod() throws Exception {
    applier.apply(internalEnvironment, singletonList(testService()));

    assertEquals(containers.size(), 1);
    Container toolingContainer = containers.get(0);
    verifyContainer(toolingContainer);
  }

  @Test
  public void canAddMultipleToolingContainersToAPodFromOneService() throws Exception {
    applier.apply(internalEnvironment, singletonList(testServiceWith2Containers()));

    assertEquals(containers.size(), 2);
    for (Container container : containers) {
      verifyContainer(container);
    }
  }

  @Test
  public void canAddMultipleToolingContainersToAPodFromSeveralServices() throws Exception {
    applier.apply(internalEnvironment, ImmutableList.of(testService(), testService()));

    assertEquals(containers.size(), 2);
    for (Container container : containers) {
      verifyContainer(container);
    }
  }

  private CheService testService() {
    CheService service = new CheService();
    CheServiceSpec cheServiceSpec = new CheServiceSpec();
    cheServiceSpec.setContainers(singletonList(testContainer()));
    service.setSpec(cheServiceSpec);
    return service;
  }

  private CheService testServiceWith2Containers() {
    CheService service = new CheService();
    CheServiceSpec cheServiceSpec = new CheServiceSpec();
    cheServiceSpec.setContainers(Arrays.asList(testContainer(), testContainer()));
    service.setSpec(cheServiceSpec);
    return service;
  }

  private org.eclipse.che.api.workspace.server.wsnext.model.Container testContainer() {
    org.eclipse.che.api.workspace.server.wsnext.model.Container cheContainer =
        new org.eclipse.che.api.workspace.server.wsnext.model.Container();
    cheContainer.setImage(TEST_IMAGE);
    cheContainer.setEnv(singletonList(new EnvVar().name(ENV_VAR).value(ENV_VAR_VALUE)));
    cheContainer.setResources(new ResourceRequirements().requests(RESOURCES_REQUEST));
    return cheContainer;
  }

  private void verifyContainer(Container toolingContainer) {
    assertEquals(toolingContainer.getImage(), TEST_IMAGE);
    assertEquals(
        toolingContainer.getEnv(),
        singletonList(new io.fabric8.kubernetes.api.model.EnvVar(ENV_VAR, ENV_VAR_VALUE, null)));
    io.fabric8.kubernetes.api.model.ResourceRequirements resourceRequirements =
        new io.fabric8.kubernetes.api.model.ResourceRequirements();
    resourceRequirements.setLimits(emptyMap());
    resourceRequirements.setRequests(singletonMap(MEMORY_KEY, new Quantity(MEMORY_VALUE)));
    assertEquals(toolingContainer.getResources(), resourceRequirements);
  }
}

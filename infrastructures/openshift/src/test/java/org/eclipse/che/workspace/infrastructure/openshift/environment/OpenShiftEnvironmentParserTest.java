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
package org.eclipse.che.workspace.infrastructure.openshift.environment;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironmentParser.DEFAULT_RESTART_POLICY;
import static org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironmentParser.PVC_IGNORED_WARNING_CODE;
import static org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironmentParser.PVC_IGNORED_WARNING_MESSAGE;
import static org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironmentParser.ROUTES_IGNORED_WARNING_MESSAGE;
import static org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironmentParser.ROUTE_IGNORED_WARNING_CODE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.DoneableKubernetesList;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;
import io.fabric8.kubernetes.client.dsl.KubernetesListMixedOperation;
import io.fabric8.kubernetes.client.dsl.RecreateFromServerGettable;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.client.OpenShiftClient;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.workspace.server.model.impl.WarningImpl;
import org.eclipse.che.api.workspace.server.spi.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.InternalEnvironment.InternalRecipe;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests {@link OpenShiftEnvironmentParser}.
 *
 * @author Anton Korneta
 */
public class OpenShiftEnvironmentParserTest {

  private static final String YAML_RECIPE = "application/x-yaml";
  private static final String TEST_POD_NAME = "app";
  private static final String ALWAYS_RESTART_POLICY = "Always";

  private OpenShiftEnvironmentParser osEnvironmentParser;

  @Mock private OpenShiftClientFactory factory;
  @Mock private OpenShiftClient client;
  @Mock private InternalEnvironment internalEnvironment;
  @Mock private InternalRecipe internalRecipe;
  @Mock private KubernetesListMixedOperation listMixedOperation;
  @Mock private KubernetesList validatedObjects;

  @Captor private ArgumentCaptor<Warning> warningCaptor;

  @Mock
  private RecreateFromServerGettable<KubernetesList, KubernetesList, DoneableKubernetesList>
      serverGettable;

  @BeforeMethod
  public void setup() {
    MockitoAnnotations.initMocks(this);
    osEnvironmentParser = new OpenShiftEnvironmentParser(factory);
    when(factory.create()).thenReturn(client);
    when(client.lists()).thenReturn(listMixedOperation);
    when(listMixedOperation.load(any(InputStream.class))).thenReturn(serverGettable);
    when(serverGettable.get()).thenReturn(validatedObjects);
    when(internalEnvironment.getRecipe()).thenReturn(internalRecipe);
    when(internalRecipe.getContentType()).thenReturn(YAML_RECIPE);
    when(internalRecipe.getContent()).thenReturn("recipe content");
  }

  @Test
  public void rewritesRestartPolicyWhenItsDifferentWithDefaultOne() throws Exception {
    final List<HasMetadata> pods = singletonList(newPod(TEST_POD_NAME, ALWAYS_RESTART_POLICY));
    when(validatedObjects.getItems()).thenReturn(pods);

    final OpenShiftEnvironment parsed = osEnvironmentParser.parse(internalEnvironment);

    assertEquals(
        parsed.getPods().get(TEST_POD_NAME).getSpec().getRestartPolicy(), DEFAULT_RESTART_POLICY);
    verifyWarnings(
        new WarningImpl(
            101,
            format(
                "Restart policy '%s' for pod '%s' is rewritten with %s",
                ALWAYS_RESTART_POLICY, TEST_POD_NAME, DEFAULT_RESTART_POLICY)));
  }

  @Test
  public void ignoreRoutesWhenRecipeContainsThem() throws Exception {
    final List<HasMetadata> objects = asList(new Route(), new Route());
    when(validatedObjects.getItems()).thenReturn(objects);

    final OpenShiftEnvironment parsed = osEnvironmentParser.parse(internalEnvironment);

    assertTrue(parsed.getRoutes().isEmpty());
    verifyWarnings(new WarningImpl(ROUTE_IGNORED_WARNING_CODE, ROUTES_IGNORED_WARNING_MESSAGE));
  }

  @Test
  public void ignorePVCsWhenRecipeContainsThem() throws Exception {
    final List<HasMetadata> pvc = singletonList(new PersistentVolumeClaim());
    when(validatedObjects.getItems()).thenReturn(pvc);

    final OpenShiftEnvironment parsed = osEnvironmentParser.parse(internalEnvironment);

    assertTrue(parsed.getRoutes().isEmpty());
    verifyWarnings(new WarningImpl(PVC_IGNORED_WARNING_CODE, PVC_IGNORED_WARNING_MESSAGE));
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
    verify(internalEnvironment, atLeastOnce()).addWarning(warningCaptor.capture());
    return warningCaptor.getAllValues();
  }

  private static Pod newPod(String podName, String restartPolicy, Container... containers) {
    final ObjectMeta podMetadata = new ObjectMetaBuilder().withName(podName).build();
    final PodSpec podSpec =
        new PodSpecBuilder().withRestartPolicy(restartPolicy).withContainers(containers).build();
    return new PodBuilder().withMetadata(podMetadata).withSpec(podSpec).build();
  }
}

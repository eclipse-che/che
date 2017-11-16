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

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironmentFactory.PVC_IGNORED_WARNING_CODE;
import static org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironmentFactory.PVC_IGNORED_WARNING_MESSAGE;
import static org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironmentFactory.ROUTES_IGNORED_WARNING_MESSAGE;
import static org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironmentFactory.ROUTE_IGNORED_WARNING_CODE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import io.fabric8.kubernetes.api.model.DoneableKubernetesList;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.client.dsl.KubernetesListMixedOperation;
import io.fabric8.kubernetes.client.dsl.RecreateFromServerGettable;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.client.OpenShiftClient;
import java.io.InputStream;
import java.util.List;
import org.eclipse.che.api.workspace.server.model.impl.WarningImpl;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.environment.InternalRecipe;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link OpenShiftEnvironmentFactory}.
 *
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class OpenShiftEnvironmentFactoryTest {

  private static final String YAML_RECIPE = "application/x-yaml";

  private OpenShiftEnvironmentFactory osEnvironmentFactory;

  @Mock private OpenShiftClientFactory factory;
  @Mock private OpenShiftEnvironmentValidator osEnvValidator;
  @Mock private OpenShiftClient client;
  @Mock private InternalEnvironment internalEnvironment;
  @Mock private InternalRecipe internalRecipe;
  @Mock private KubernetesListMixedOperation listMixedOperation;
  @Mock private KubernetesList validatedObjects;

  @Mock
  private RecreateFromServerGettable<KubernetesList, KubernetesList, DoneableKubernetesList>
      serverGettable;

  @BeforeMethod
  public void setup() {
    osEnvironmentFactory =
        new OpenShiftEnvironmentFactory(null, null, null, factory, osEnvValidator);
    when(factory.create()).thenReturn(client);
    when(client.lists()).thenReturn(listMixedOperation);
    when(listMixedOperation.load(any(InputStream.class))).thenReturn(serverGettable);
    when(serverGettable.get()).thenReturn(validatedObjects);
    when(internalEnvironment.getRecipe()).thenReturn(internalRecipe);
    when(internalRecipe.getContentType()).thenReturn(YAML_RECIPE);
    when(internalRecipe.getContent()).thenReturn("recipe content");
  }

  @Test
  public void ignoreRoutesWhenRecipeContainsThem() throws Exception {
    final List<HasMetadata> objects = asList(new Route(), new Route());
    when(validatedObjects.getItems()).thenReturn(objects);

    final OpenShiftEnvironment parsed =
        osEnvironmentFactory.doCreate(internalRecipe, emptyMap(), emptyList());

    assertTrue(parsed.getRoutes().isEmpty());
    assertEquals(parsed.getWarnings().size(), 1);
    assertEquals(
        parsed.getWarnings().get(0),
        new WarningImpl(ROUTE_IGNORED_WARNING_CODE, ROUTES_IGNORED_WARNING_MESSAGE));
  }

  @Test
  public void ignorePVCsWhenRecipeContainsThem() throws Exception {
    final List<HasMetadata> pvc = singletonList(new PersistentVolumeClaim());
    when(validatedObjects.getItems()).thenReturn(pvc);

    final OpenShiftEnvironment parsed =
        osEnvironmentFactory.doCreate(internalRecipe, emptyMap(), emptyList());

    assertTrue(parsed.getRoutes().isEmpty());
    assertEquals(parsed.getWarnings().size(), 1);
    assertEquals(
        parsed.getWarnings().get(0),
        new WarningImpl(PVC_IGNORED_WARNING_CODE, PVC_IGNORED_WARNING_MESSAGE));
  }
}

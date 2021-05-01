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
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace;

import static org.eclipse.che.workspace.infrastructure.kubernetes.Annotations.CREATE_IN_CHE_INSTALLATION_NAMESPACE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_WORKSPACE_ID_LABEL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.ConfigMapList;
import io.fabric8.kubernetes.api.model.DeletionPropagation;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.WorkspaceRuntimes;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalRuntime;
import org.eclipse.che.workspace.infrastructure.kubernetes.CheServerKubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.CheInstallationLocation;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class CheNamespaceTest {

  private static final String WORKSPACE_ID = "ws-id";
  private static final String OWNER_ID = "owner-id";
  private static final String CHE_NAMESPACE = "che";

  private CheNamespace cheNamespace;

  @Mock private CheInstallationLocation cheInstallationLocation;
  @Mock private CheServerKubernetesClientFactory clientFactory;
  @Mock private WorkspaceRuntimes workspaceRuntimes;
  @Mock private RuntimeIdentity identity;
  @Mock private KubernetesClient kubeClient;

  @Mock private MixedOperation<ConfigMap, ConfigMapList, Resource<ConfigMap>> kubeConfigMaps;

  @Mock
  private MixedOperation<ConfigMap, ConfigMapList, Resource<ConfigMap>> kubeConfigMapsInNamespace;

  @Mock
  private MixedOperation<ConfigMap, ConfigMapList, Resource<ConfigMap>> kubeConfigMapsWithLabel;

  @Mock
  private MixedOperation<ConfigMap, ConfigMapList, Resource<ConfigMap>>
      kubeConfigMapsWithPropagationPolicy;

  @Mock private InternalRuntime internalRuntime;

  @BeforeMethod
  public void setUp() throws InfrastructureException, ServerException {
    when(cheInstallationLocation.getInstallationLocationNamespace()).thenReturn(CHE_NAMESPACE);
    cheNamespace = new CheNamespace(cheInstallationLocation, clientFactory, workspaceRuntimes);
    lenient().when(identity.getWorkspaceId()).thenReturn(WORKSPACE_ID);
    lenient().when(identity.getOwnerId()).thenReturn(OWNER_ID);
    lenient().when(workspaceRuntimes.getInternalRuntime(WORKSPACE_ID)).thenReturn(internalRuntime);
  }

  @Test
  public void testCreateConfigMaps() throws InfrastructureException {
    // given
    when(internalRuntime.getOwner()).thenReturn(OWNER_ID);
    when(internalRuntime.getStatus()).thenReturn(WorkspaceStatus.STARTING);
    Map<String, String> cheNamespaceAnnotations =
        ImmutableMap.of(CREATE_IN_CHE_INSTALLATION_NAMESPACE, "true");

    ConfigMap cm1 =
        new ConfigMapBuilder()
            .withNewMetadata()
            .withName("cm1")
            .withAnnotations(cheNamespaceAnnotations)
            .endMetadata()
            .build();
    ConfigMap cm2 =
        new ConfigMapBuilder()
            .withNewMetadata()
            .withName("cm2")
            .withAnnotations(cheNamespaceAnnotations)
            .endMetadata()
            .build();

    when(clientFactory.create()).thenReturn(kubeClient);
    when(kubeClient.configMaps()).thenReturn(kubeConfigMaps);
    when(kubeConfigMaps.inNamespace(CHE_NAMESPACE)).thenReturn(kubeConfigMapsInNamespace);
    when(kubeConfigMapsInNamespace.create(any(ConfigMap.class))).thenReturn(cm1).thenReturn(cm2);

    List<ConfigMap> configMapsToCreate = Arrays.asList(cm1, cm2);

    // when
    List<ConfigMap> createdConfigMaps = cheNamespace.createConfigMaps(configMapsToCreate, identity);

    // then
    assertEquals(createdConfigMaps.size(), 2);
    createdConfigMaps.forEach(
        cm -> assertEquals(cm.getMetadata().getLabels().get(CHE_WORKSPACE_ID_LABEL), WORKSPACE_ID));
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void failWhenTryToCreateCmWithoutAnnotation() throws InfrastructureException {
    // given
    when(internalRuntime.getOwner()).thenReturn(OWNER_ID);
    when(internalRuntime.getStatus()).thenReturn(WorkspaceStatus.STARTING);

    ConfigMap cm1 = new ConfigMapBuilder().withNewMetadata().withName("cm1").endMetadata().build();

    when(clientFactory.create()).thenReturn(kubeClient);
    when(kubeClient.configMaps()).thenReturn(kubeConfigMaps);
    when(kubeConfigMaps.inNamespace(CHE_NAMESPACE)).thenReturn(kubeConfigMapsInNamespace);
    when(kubeConfigMapsInNamespace.create(any(ConfigMap.class))).thenReturn(cm1);

    List<ConfigMap> configMapsToCreate = Collections.singletonList(cm1);

    // when
    cheNamespace.createConfigMaps(configMapsToCreate, identity);

    // then exception
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void failWhenTryToCreateCmWithWronglySetAnnotation() throws InfrastructureException {
    // given
    when(internalRuntime.getOwner()).thenReturn(OWNER_ID);
    when(internalRuntime.getStatus()).thenReturn(WorkspaceStatus.STARTING);

    ConfigMap cm1 =
        new ConfigMapBuilder()
            .withNewMetadata()
            .withName("cm1")
            .withAnnotations(
                Collections.singletonMap(CREATE_IN_CHE_INSTALLATION_NAMESPACE, "blabol"))
            .endMetadata()
            .build();

    when(clientFactory.create()).thenReturn(kubeClient);
    when(kubeClient.configMaps()).thenReturn(kubeConfigMaps);
    when(kubeConfigMaps.inNamespace(CHE_NAMESPACE)).thenReturn(kubeConfigMapsInNamespace);
    when(kubeConfigMapsInNamespace.create(any(ConfigMap.class))).thenReturn(cm1);

    List<ConfigMap> configMapsToCreate = Collections.singletonList(cm1);

    // when
    cheNamespace.createConfigMaps(configMapsToCreate, identity);

    // then exception
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void testCreateConfigmapFailsWhenNoWorkspaceIdFoundInRuntimes()
      throws InfrastructureException, ServerException {
    // given
    when(workspaceRuntimes.getInternalRuntime(WORKSPACE_ID)).thenThrow(ServerException.class);
    when(internalRuntime.getOwner()).thenReturn(OWNER_ID);
    when(internalRuntime.getStatus()).thenReturn(WorkspaceStatus.STARTING);

    ConfigMap cm1 = new ConfigMapBuilder().withNewMetadata().withName("cm1").endMetadata().build();

    // when
    cheNamespace.createConfigMaps(Collections.singletonList(cm1), identity);
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void testCreateConfigmapFailsWhenOwnerDontMatch() throws InfrastructureException {
    // given
    when(internalRuntime.getOwner()).thenReturn("nope");
    when(internalRuntime.getStatus()).thenReturn(WorkspaceStatus.STARTING);

    ConfigMap cm1 = new ConfigMapBuilder().withNewMetadata().withName("cm1").endMetadata().build();

    // when
    cheNamespace.createConfigMaps(Collections.singletonList(cm1), identity);
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void testCreateConfigmapFailsWhenWorkspaceStatusIsNotStarting()
      throws InfrastructureException {
    // given
    when(internalRuntime.getOwner()).thenReturn(OWNER_ID);
    when(internalRuntime.getStatus()).thenReturn(WorkspaceStatus.RUNNING);

    ConfigMap cm1 = new ConfigMapBuilder().withNewMetadata().withName("cm1").endMetadata().build();

    // when
    cheNamespace.createConfigMaps(Collections.singletonList(cm1), identity);
  }

  @Test
  public void testCreateEmptyReturnEmpty() throws InfrastructureException {
    assertTrue(cheNamespace.createConfigMaps(Collections.emptyList(), identity).isEmpty());
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void throwExceptionWhenCheInstallationLocationFails() throws InfrastructureException {
    when(cheInstallationLocation.getInstallationLocationNamespace())
        .thenThrow(InfrastructureException.class);

    new CheNamespace(cheInstallationLocation, clientFactory, workspaceRuntimes);
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void cleanupThrowExceptionWhenWorkspaceIdIsNull() throws InfrastructureException {
    cheNamespace.cleanUp(null);
  }

  @Test
  public void testCleanup() throws InfrastructureException {
    // given
    when(clientFactory.create()).thenReturn(kubeClient);
    when(kubeClient.configMaps()).thenReturn(kubeConfigMaps);
    when(kubeConfigMaps.inNamespace(CHE_NAMESPACE)).thenReturn(kubeConfigMapsInNamespace);
    when(kubeConfigMapsInNamespace.withLabel(CHE_WORKSPACE_ID_LABEL, WORKSPACE_ID))
        .thenReturn(kubeConfigMapsWithLabel);
    when(kubeConfigMapsWithLabel.withPropagationPolicy(DeletionPropagation.BACKGROUND))
        .thenReturn(kubeConfigMapsWithPropagationPolicy);

    // when
    cheNamespace.cleanUp(WORKSPACE_ID);

    // then
    verify(kubeConfigMapsWithPropagationPolicy).delete();
  }
}

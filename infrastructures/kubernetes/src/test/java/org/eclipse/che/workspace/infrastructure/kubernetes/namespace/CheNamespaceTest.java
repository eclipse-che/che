package org.eclipse.che.workspace.infrastructure.kubernetes.namespace;

import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_WORKSPACE_ID_LABEL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.ConfigMapList;
import io.fabric8.kubernetes.api.model.DoneableConfigMap;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.WorkspaceRuntimes;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalRuntime;
import org.eclipse.che.workspace.infrastructure.kubernetes.CheKubernetesClientFactory;
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
  @Mock private CheKubernetesClientFactory clientFactory;
  @Mock private WorkspaceRuntimes workspaceRuntimes;
  @Mock private RuntimeIdentity identity;
  @Mock private KubernetesClient kubeClient;

  @Mock
  private MixedOperation<
          ConfigMap, ConfigMapList, DoneableConfigMap, Resource<ConfigMap, DoneableConfigMap>>
      kubeConfigMaps;

  @Mock
  private MixedOperation<
          ConfigMap, ConfigMapList, DoneableConfigMap, Resource<ConfigMap, DoneableConfigMap>>
      kubeConfigMapsInNamespace;

  @Mock
  private MixedOperation<
          ConfigMap, ConfigMapList, DoneableConfigMap, Resource<ConfigMap, DoneableConfigMap>>
      kubeConfigMapsWithLabel;

  @Mock
  private MixedOperation<
          ConfigMap, ConfigMapList, DoneableConfigMap, Resource<ConfigMap, DoneableConfigMap>>
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

    ConfigMap cm1 = new ConfigMapBuilder().withNewMetadata().withName("cm1").endMetadata().build();
    ConfigMap cm2 = new ConfigMapBuilder().withNewMetadata().withName("cm2").endMetadata().build();

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
    when(kubeConfigMapsWithLabel.withPropagationPolicy("Background"))
        .thenReturn(kubeConfigMapsWithPropagationPolicy);

    // when
    cheNamespace.cleanUp(WORKSPACE_ID);

    // then
    verify(kubeConfigMapsWithPropagationPolicy).delete();
  }
}

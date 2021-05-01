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

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import io.fabric8.kubernetes.api.model.ObjectReferenceBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.openshift.api.model.PolicyRuleBuilder;
import io.fabric8.openshift.api.model.Role;
import io.fabric8.openshift.api.model.RoleBinding;
import io.fabric8.openshift.api.model.RoleBindingBuilder;
import io.fabric8.openshift.api.model.RoleBindingList;
import io.fabric8.openshift.api.model.RoleBuilder;
import io.fabric8.openshift.api.model.RoleList;
import io.fabric8.openshift.client.OpenShiftClient;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.CheInstallationLocation;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Test for {@link OpenShiftStopWorkspaceRoleProvisioner}
 *
 * <p>#author Tom George
 */
@Listeners(MockitoTestNGListener.class)
public class OpenShiftStopWorkspaceRoleProvisionerTest {

  @Mock private CheInstallationLocation cheInstallationLocation;
  private OpenShiftStopWorkspaceRoleProvisioner stopWorkspaceRoleProvisioner;

  @Mock private OpenShiftClientFactory clientFactory;
  @Mock private OpenShiftClient osClient;
  @Mock private KubernetesClient kubernetesClient;

  @Mock private MixedOperation<Role, RoleList, Resource<Role>> mixedRoleOperation;

  @Mock
  private MixedOperation<RoleBinding, RoleBindingList, Resource<RoleBinding>>
      mixedRoleBindingOperation;

  @Mock private NonNamespaceOperation<Role, RoleList, Resource<Role>> nonNamespaceRoleOperation;

  @Mock
  private NonNamespaceOperation<RoleBinding, RoleBindingList, Resource<RoleBinding>>
      nonNamespaceRoleBindingOperation;

  @Mock private Resource<Role> roleResource;
  @Mock private Resource<RoleBinding> roleBindingResource;
  @Mock private Role mockRole;
  @Mock private RoleBinding mockRoleBinding;

  private final Role expectedRole =
      new RoleBuilder()
          .withNewMetadata()
          .withName("workspace-stop")
          .endMetadata()
          .withRules(
              new PolicyRuleBuilder()
                  .withApiGroups("")
                  .withResources("pods")
                  .withVerbs("get", "list", "watch", "delete")
                  .build(),
              new PolicyRuleBuilder()
                  .withApiGroups("")
                  .withResources("configmaps", "services", "secrets")
                  .withVerbs("delete", "list", "get")
                  .build(),
              new PolicyRuleBuilder()
                  .withApiGroups("route.openshift.io")
                  .withResources("routes")
                  .withVerbs("delete", "list")
                  .build(),
              new PolicyRuleBuilder()
                  .withApiGroups("apps")
                  .withResources("deployments", "replicasets")
                  .withVerbs("delete", "list", "get", "patch")
                  .build())
          .build();

  private final RoleBinding expectedRoleBinding =
      new RoleBindingBuilder()
          .withNewMetadata()
          .withName("che-workspace-stop")
          .withNamespace("developer-che")
          .endMetadata()
          .withNewRoleRef()
          .withName("workspace-stop")
          .withNamespace("developer-che")
          .endRoleRef()
          .withSubjects(
              new ObjectReferenceBuilder()
                  .withKind("ServiceAccount")
                  .withName("che")
                  .withNamespace("che")
                  .build())
          .build();

  @BeforeMethod
  public void setUp() throws Exception {
    lenient().when(cheInstallationLocation.getInstallationLocationNamespace()).thenReturn("che");
    stopWorkspaceRoleProvisioner =
        new OpenShiftStopWorkspaceRoleProvisioner(clientFactory, cheInstallationLocation, true);
    lenient().when(clientFactory.createOC()).thenReturn(osClient);
    lenient().when(osClient.roles()).thenReturn(mixedRoleOperation);
    lenient().when(osClient.roleBindings()).thenReturn(mixedRoleBindingOperation);
    lenient()
        .when(mixedRoleOperation.inNamespace(anyString()))
        .thenReturn(nonNamespaceRoleOperation);
    lenient()
        .when(mixedRoleBindingOperation.inNamespace(anyString()))
        .thenReturn(nonNamespaceRoleBindingOperation);
    lenient().when(nonNamespaceRoleOperation.withName(anyString())).thenReturn(roleResource);
    lenient()
        .when(nonNamespaceRoleBindingOperation.withName(anyString()))
        .thenReturn(roleBindingResource);
    lenient().when(roleResource.get()).thenReturn(null);
    lenient().when(nonNamespaceRoleOperation.createOrReplace(any())).thenReturn(mockRole);
    lenient()
        .when(nonNamespaceRoleBindingOperation.createOrReplace(any()))
        .thenReturn(mockRoleBinding);
  }

  @Test
  public void shouldCreateRole() {
    assertEquals(
        stopWorkspaceRoleProvisioner.createStopWorkspacesRole("workspace-stop"), expectedRole);
  }

  @Test
  public void shouldCreateRoleBinding() throws InfrastructureException {
    when(cheInstallationLocation.getInstallationLocationNamespace()).thenReturn("che");
    assertEquals(
        stopWorkspaceRoleProvisioner.createStopWorkspacesRoleBinding("developer-che"),
        expectedRoleBinding);
  }

  @Test
  public void shouldCreateRoleAndRoleBindingWhenRoleDoesNotYetExist()
      throws InfrastructureException {
    stopWorkspaceRoleProvisioner.provision("developer-che");
    verify(osClient, times(2)).roles();
    verify(osClient.roles(), times(2)).inNamespace("developer-che");
    verify(osClient.roles().inNamespace("developer-che")).withName("workspace-stop");
    verify(osClient.roles().inNamespace("developer-che")).createOrReplace(expectedRole);
    verify(osClient).roleBindings();
    verify(osClient.roleBindings()).inNamespace("developer-che");
    verify(osClient.roleBindings().inNamespace("developer-che"))
        .createOrReplace(expectedRoleBinding);
  }

  @Test
  public void shouldCreateRoleBindingWhenRoleAlreadyExists() throws InfrastructureException {
    lenient().when(roleResource.get()).thenReturn(expectedRole);
    stopWorkspaceRoleProvisioner.provision("developer-che");
    verify(osClient, times(1)).roles();
    verify(osClient).roleBindings();
    verify(osClient.roleBindings()).inNamespace("developer-che");
    verify(osClient.roleBindings().inNamespace("developer-che"))
        .createOrReplace(expectedRoleBinding);
  }

  @Test
  public void shouldNotCreateRoleBindingWhenStopWorkspaceRolePropertyIsDisabled()
      throws InfrastructureException {
    OpenShiftStopWorkspaceRoleProvisioner disabledStopWorkspaceRoleProvisioner =
        new OpenShiftStopWorkspaceRoleProvisioner(clientFactory, cheInstallationLocation, false);
    disabledStopWorkspaceRoleProvisioner.provision("developer-che");
    verify(osClient, never()).roles();
    verify(osClient, never()).roleBindings();
    verify(osClient.roleBindings(), never()).inNamespace("developer-che");
  }

  @Test
  public void shouldNotCreateRoleBindingWhenInstallationLocationIsNull()
      throws InfrastructureException {
    lenient().when(cheInstallationLocation.getInstallationLocationNamespace()).thenReturn(null);
    OpenShiftStopWorkspaceRoleProvisioner
        stopWorkspaceRoleProvisionerWithoutValidInstallationLocation =
            new OpenShiftStopWorkspaceRoleProvisioner(clientFactory, cheInstallationLocation, true);
    stopWorkspaceRoleProvisionerWithoutValidInstallationLocation.provision("developer-che");
    verify(osClient, never()).roles();
    verify(osClient, never()).roleBindings();
    verify(osClient.roleBindings(), never()).inNamespace("developer-che");
  }
}

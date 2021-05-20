/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import static org.eclipse.che.workspace.infrastructure.kubernetes.Warnings.NOT_ABLE_TO_PROVISION_WORKSPACE_DEPLOYMENT;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Warnings.NOT_ABLE_TO_PROVISION_WORKSPACE_DEPLOYMENT_LABELS_OR_ANNOTATIONS;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.DeploymentMetadataProvisioner.WS_DEPLOYMENT_ANNOTATIONS_ATTR_NAME;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.DeploymentMetadataProvisioner.WS_DEPLOYMENT_LABELS_ATTR_NAME;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import java.util.Collections;
import java.util.Map;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.WarningImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class DeploymentMetadataProvisionerTest {
  static final String WS_ID = "ws-123";
  static final String DEPLOYMENT_NAME = "dep-324";
  @Mock KubernetesEnvironment k8sEnv;
  @Mock RuntimeIdentity identity;
  @Mock WorkspaceManager workspaceManager;
  @Mock WorkspaceImpl workspace;
  @InjectMocks DeploymentMetadataProvisioner provisioner;

  @BeforeMethod
  public void setup() {
    when(identity.getWorkspaceId()).thenReturn(WS_ID);
  }

  @Test
  public void shouldProvisionWorkspaceDeploymentLabels()
      throws NotFoundException, ServerException, InfrastructureException {
    // given
    when(workspace.getAttributes())
        .thenReturn(ImmutableMap.of(WS_DEPLOYMENT_LABELS_ATTR_NAME, "L1=V1"));
    when(workspaceManager.getWorkspace(eq(WS_ID))).thenReturn(workspace);

    Deployment deployment = newDeployment();
    doReturn(ImmutableMap.of(DEPLOYMENT_NAME, deployment)).when(k8sEnv).getDeploymentsCopy();
    // when
    provisioner.provision(k8sEnv, identity);
    // then
    Map<String, String> labels = deployment.getMetadata().getLabels();
    assertEquals(1, labels.size());
    assertEquals(labels.get("L1"), "V1");
  }

  @Test
  public void shouldProvisionWorkspaceDeploymentAnnotations()
      throws NotFoundException, ServerException, InfrastructureException {
    // given
    when(workspace.getAttributes())
        .thenReturn(ImmutableMap.of(WS_DEPLOYMENT_ANNOTATIONS_ATTR_NAME, "A1=V1"));
    when(workspaceManager.getWorkspace(eq(WS_ID))).thenReturn(workspace);

    Deployment deployment = newDeployment();
    doReturn(ImmutableMap.of(DEPLOYMENT_NAME, deployment)).when(k8sEnv).getDeploymentsCopy();
    // when
    provisioner.provision(k8sEnv, identity);
    // then
    Map<String, String> labels = deployment.getMetadata().getAnnotations();
    assertEquals(1, labels.size());
    assertEquals(labels.get("A1"), "V1");
  }

  @Test
  public void shouldDoNothingWithDeploymentIfNoAttributesIsSet()
      throws NotFoundException, ServerException, InfrastructureException {
    // given
    when(workspace.getAttributes()).thenReturn(Collections.emptyMap());
    when(workspaceManager.getWorkspace(eq(WS_ID))).thenReturn(workspace);

    Deployment deployment = mock(Deployment.class);
    doReturn(ImmutableMap.of(DEPLOYMENT_NAME, deployment)).when(k8sEnv).getDeploymentsCopy();
    // when
    provisioner.provision(k8sEnv, identity);
    // then
    verifyZeroInteractions(deployment);
  }

  @Test
  public void shouldAddWarningIfNotAbleToGetWorkspaceAttributes()
      throws NotFoundException, ServerException, InfrastructureException {
    // given
    doThrow(new NotFoundException("Element not found"))
        .when(workspaceManager)
        .getWorkspace(eq(WS_ID));
    // when
    provisioner.provision(k8sEnv, identity);
    // then
    ArgumentCaptor<WarningImpl> argumentCaptor = ArgumentCaptor.forClass(WarningImpl.class);
    verify(k8sEnv).addWarning(argumentCaptor.capture());
    assertEquals(argumentCaptor.getAllValues().size(), 1);
    assertEquals(
        argumentCaptor.getValue(),
        new WarningImpl(
            4200,
            "Not able to find workspace attributes for " + WS_ID + ". Reason Element not found"));
  }

  @Test
  public void shouldAddWarningIfNotAbleToGetWorkspaceAttributesServerException()
      throws NotFoundException, ServerException, InfrastructureException {
    // given
    doThrow(new ServerException("Connection problem"))
        .when(workspaceManager)
        .getWorkspace(eq(WS_ID));
    // when
    provisioner.provision(k8sEnv, identity);
    // then
    ArgumentCaptor<WarningImpl> argumentCaptor = ArgumentCaptor.forClass(WarningImpl.class);
    verify(k8sEnv).addWarning(argumentCaptor.capture());
    assertEquals(argumentCaptor.getAllValues().size(), 1);
    assertEquals(
        argumentCaptor.getValue(),
        new WarningImpl(
            NOT_ABLE_TO_PROVISION_WORKSPACE_DEPLOYMENT,
            "Not able to find workspace attributes for " + WS_ID + ". Reason Connection problem"));
  }

  @Test
  public void shouldAddWarningIfLabelAttributeInInvalidFormat()
      throws NotFoundException, ServerException, InfrastructureException {
    // given
    when(workspace.getAttributes())
        .thenReturn(ImmutableMap.of(WS_DEPLOYMENT_LABELS_ATTR_NAME, "L1~V1"));
    when(workspaceManager.getWorkspace(eq(WS_ID))).thenReturn(workspace);

    Deployment deployment = newDeployment();
    doReturn(ImmutableMap.of(DEPLOYMENT_NAME, deployment)).when(k8sEnv).getDeploymentsCopy();
    // when
    provisioner.provision(k8sEnv, identity);
    // then
    ArgumentCaptor<WarningImpl> argumentCaptor = ArgumentCaptor.forClass(WarningImpl.class);
    verify(k8sEnv).addWarning(argumentCaptor.capture());
    assertEquals(argumentCaptor.getAllValues().size(), 1);
    assertEquals(
        argumentCaptor.getValue(),
        new WarningImpl(
            NOT_ABLE_TO_PROVISION_WORKSPACE_DEPLOYMENT_LABELS_OR_ANNOTATIONS,
            "Not able to provision workspace "
                + WS_ID
                + " deployment labels or annotations because of invalid configuration. Reason: 'Chunk [L1~V1] is not a valid entry'"));
  }

  @Test
  public void shouldAddWarningIfAnnotationAttributeInInvalidFormat()
      throws NotFoundException, ServerException, InfrastructureException {
    // given
    when(workspace.getAttributes())
        .thenReturn(ImmutableMap.of(WS_DEPLOYMENT_ANNOTATIONS_ATTR_NAME, "A1~V1"));
    when(workspaceManager.getWorkspace(eq(WS_ID))).thenReturn(workspace);

    Deployment deployment = newDeployment();
    doReturn(ImmutableMap.of(DEPLOYMENT_NAME, deployment)).when(k8sEnv).getDeploymentsCopy();
    // when
    provisioner.provision(k8sEnv, identity);
    // then
    ArgumentCaptor<WarningImpl> argumentCaptor = ArgumentCaptor.forClass(WarningImpl.class);
    verify(k8sEnv).addWarning(argumentCaptor.capture());
    assertEquals(argumentCaptor.getAllValues().size(), 1);
    assertEquals(
        argumentCaptor.getValue(),
        new WarningImpl(
            NOT_ABLE_TO_PROVISION_WORKSPACE_DEPLOYMENT_LABELS_OR_ANNOTATIONS,
            "Not able to provision workspace "
                + WS_ID
                + " deployment labels or annotations because of invalid configuration. Reason: 'Chunk [A1~V1] is not a valid entry'"));
  }

  private static Deployment newDeployment() {
    return new DeploymentBuilder()
        .withNewMetadata()
        .withName(DEPLOYMENT_NAME)
        .endMetadata()
        .withNewSpec()
        .withNewTemplate()
        .withNewMetadata()
        .withName("POD_NAME")
        .endMetadata()
        .endTemplate()
        .endSpec()
        .build();
  }
}

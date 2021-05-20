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

import static java.lang.String.format;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Warnings.NOT_ABLE_TO_PROVISION_WORKSPACE_DEPLOYMENT;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Warnings.NOT_ABLE_TO_PROVISION_WORKSPACE_DEPLOYMENT_LABELS_OR_ANNOTATIONS;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Warnings.NOT_ABLE_TO_PROVISION_WORKSPACE_DEPLOYMENT_LABELS_OR_ANNOTATIONS_MESSAGE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Warnings.NOT_ABLE_TO_PROVISION_WORKSPACE_DEPLOYMENT_MESSAGE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.putAnnotations;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.putLabels;

import com.google.common.base.Splitter;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.WarningImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that is provisioning deployment labels and annotations that are configured in workspace
 * attributes.
 */
public class DeploymentMetadataProvisioner
    implements ConfigurationProvisioner<KubernetesEnvironment> {

  public static final String WS_DEPLOYMENT_LABELS_ATTR_NAME = "workspaceDeploymentLabels";
  public static final String WS_DEPLOYMENT_ANNOTATIONS_ATTR_NAME = "workspaceDeploymentAnnotations";
  private static final Logger LOG = LoggerFactory.getLogger(DeploymentMetadataProvisioner.class);

  private final WorkspaceManager workspaceManager;
  private final Splitter.MapSplitter splitter = Splitter.on(",").withKeyValueSeparator("=");

  @Inject
  public DeploymentMetadataProvisioner(WorkspaceManager workspaceManager) {
    this.workspaceManager = workspaceManager;
  }

  @Override
  public void provision(KubernetesEnvironment k8sEnv, RuntimeIdentity identity)
      throws InfrastructureException {
    try {
      Workspace wsg = workspaceManager.getWorkspace(identity.getWorkspaceId());
      for (Deployment d : k8sEnv.getDeploymentsCopy().values()) {
        Map<String, String> workspaceAttributes = wsg.getAttributes();
        if (workspaceAttributes.containsKey(WS_DEPLOYMENT_LABELS_ATTR_NAME)) {
          putLabels(
              d.getMetadata(),
              splitter.split(workspaceAttributes.get(WS_DEPLOYMENT_LABELS_ATTR_NAME)));
        }
        if (workspaceAttributes.containsKey(WS_DEPLOYMENT_ANNOTATIONS_ATTR_NAME)) {
          putAnnotations(
              d.getMetadata(),
              splitter.split(workspaceAttributes.get(WS_DEPLOYMENT_ANNOTATIONS_ATTR_NAME)));
        }
      }
    } catch (NotFoundException | ServerException e) {
      String message =
          format(
              NOT_ABLE_TO_PROVISION_WORKSPACE_DEPLOYMENT_MESSAGE,
              identity.getWorkspaceId(),
              e.getMessage());
      LOG.warn(message);
      k8sEnv.addWarning(new WarningImpl(NOT_ABLE_TO_PROVISION_WORKSPACE_DEPLOYMENT, message));
    } catch (IllegalArgumentException e) {
      String message =
          format(
              NOT_ABLE_TO_PROVISION_WORKSPACE_DEPLOYMENT_LABELS_OR_ANNOTATIONS_MESSAGE,
              identity.getWorkspaceId(),
              e.getMessage());
      LOG.warn(message);
      k8sEnv.addWarning(
          new WarningImpl(
              NOT_ABLE_TO_PROVISION_WORKSPACE_DEPLOYMENT_LABELS_OR_ANNOTATIONS, message));
    }
  }
}

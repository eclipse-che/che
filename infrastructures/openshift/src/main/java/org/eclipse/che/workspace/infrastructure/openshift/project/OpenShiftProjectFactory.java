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
package org.eclipse.che.workspace.infrastructure.openshift.project;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.inject.Named;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespaceFactory;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;

/**
 * Helps to create {@link OpenShiftProject} instances.
 *
 * @author Anton Korneta
 */
@Singleton
public class OpenShiftProjectFactory extends KubernetesNamespaceFactory {

  private final String projectName;
  private final String serviceAccountName;
  private final OpenShiftClientFactory clientFactory;

  @Inject
  public OpenShiftProjectFactory(
      @Nullable @Named("che.infra.openshift.project") String projectName,
      @Nullable @Named("che.infra.kubernetes.service_account_name") String serviceAccountName,
      OpenShiftClientFactory clientFactory) {
    super(projectName, clientFactory);
    this.projectName = projectName;
    this.serviceAccountName = serviceAccountName;
    this.clientFactory = clientFactory;
  }

  /**
   * Creates a OpenShift project for the specified workspace.
   *
   * <p>The project name will be chosen according to a configuration, and it will be prepared
   * (created if necessary).
   *
   * @param workspaceId identifier of the workspace
   * @return created project
   * @throws InfrastructureException if any exception occurs during project preparing
   */
  public OpenShiftProject create(String workspaceId) throws InfrastructureException {
    final String projectName = isPredefined() ? this.projectName : workspaceId;
    OpenShiftProject osProject = doCreateProject(workspaceId, projectName);
    osProject.prepare();

    if (!isPredefined() && !isNullOrEmpty(serviceAccountName)) {
      // prepare service account for workspace only if account name is configured
      // and project is not predefined
      // since predefined project should be prepared during Che deployment
      WorkspaceServiceAccount workspaceServiceAccount =
          doCreateServiceAccount(workspaceId, projectName);
      workspaceServiceAccount.prepare();
    }

    return osProject;
  }

  /**
   * Creates a kubernetes namespace for the specified workspace.
   *
   * <p>Project won't be prepared. This method should be used only in case workspace recovering.
   *
   * @param workspaceId identifier of the workspace
   * @return created namespace
   */
  public OpenShiftProject create(String workspaceId, String projectName) {
    return doCreateProject(workspaceId, projectName);
  }

  @VisibleForTesting
  OpenShiftProject doCreateProject(String workspaceId, String name) {
    return new OpenShiftProject(clientFactory, name, workspaceId);
  }

  @VisibleForTesting
  WorkspaceServiceAccount doCreateServiceAccount(String workspaceId, String projectName) {
    return new WorkspaceServiceAccount(workspaceId, projectName, serviceAccountName, clientFactory);
  }
}

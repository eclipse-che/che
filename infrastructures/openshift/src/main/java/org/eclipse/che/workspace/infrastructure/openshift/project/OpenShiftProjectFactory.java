/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift.project;

import static com.google.common.base.Strings.isNullOrEmpty;

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
  private final OpenShiftClientFactory clientFactory;

  @Inject
  public OpenShiftProjectFactory(
      @Nullable @Named("che.infra.openshift.project") String projectName,
      OpenShiftClientFactory clientFactory) {
    super(projectName, clientFactory);
    this.projectName = projectName;
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
    final String projectName = isNullOrEmpty(this.projectName) ? workspaceId : this.projectName;

    OpenShiftProject osProject = new OpenShiftProject(clientFactory, projectName, workspaceId);
    osProject.prepare();

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
    return new OpenShiftProject(clientFactory, projectName, workspaceId);
  }
}

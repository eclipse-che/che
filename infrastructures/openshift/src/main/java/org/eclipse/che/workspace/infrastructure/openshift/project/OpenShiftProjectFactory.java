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
package org.eclipse.che.workspace.infrastructure.openshift.project;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.inject.Named;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;

/**
 * Helps to create {@link OpenShiftProject} instances.
 *
 * @author Anton Korneta
 */
@Singleton
public class OpenShiftProjectFactory {

  private final String projectName;
  private final OpenShiftClientFactory clientFactory;

  @Inject
  public OpenShiftProjectFactory(
      @Nullable @Named("che.infra.openshift.project") String projectName,
      OpenShiftClientFactory clientFactory) {
    this.projectName = projectName;
    this.clientFactory = clientFactory;
  }

  public OpenShiftProject create(String workspaceId) throws InfrastructureException {
    final String projectName = isNullOrEmpty(this.projectName) ? workspaceId : this.projectName;
    return new OpenShiftProject(clientFactory, projectName, workspaceId);
  }
}

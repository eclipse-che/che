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
package org.eclipse.che.multiuser.organization.api.resource;

import static java.util.Arrays.asList;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.commons.lang.Size;
import org.eclipse.che.multiuser.organization.api.OrganizationManager;
import org.eclipse.che.multiuser.organization.shared.model.Organization;
import org.eclipse.che.multiuser.organization.spi.impl.OrganizationImpl;
import org.eclipse.che.multiuser.resource.api.free.DefaultResourcesProvider;
import org.eclipse.che.multiuser.resource.api.type.RamResourceType;
import org.eclipse.che.multiuser.resource.api.type.RuntimeResourceType;
import org.eclipse.che.multiuser.resource.api.type.TimeoutResourceType;
import org.eclipse.che.multiuser.resource.api.type.WorkspaceResourceType;
import org.eclipse.che.multiuser.resource.spi.impl.ResourceImpl;

/**
 * Provided free resources that are available for usage by organizational accounts by default.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class DefaultOrganizationResourcesProvider implements DefaultResourcesProvider {
  private final OrganizationManager organizationManager;
  private final long ramPerOrganization;
  private final int workspacesPerOrganization;
  private final int runtimesPerOrganization;
  private final long timeout;

  @Inject
  public DefaultOrganizationResourcesProvider(
      OrganizationManager organizationManager,
      @Named("che.limits.organization.workspaces.ram") String ramPerOrganization,
      @Named("che.limits.organization.workspaces.count") int workspacesPerOrganization,
      @Named("che.limits.organization.workspaces.run.count") int runtimesPerOrganization,
      @Named("che.limits.workspace.idle.timeout") long timeout) {
    this.timeout = TimeUnit.MILLISECONDS.toMinutes(timeout);
    this.organizationManager = organizationManager;
    this.ramPerOrganization =
        "-1".equals(ramPerOrganization) ? -1 : Size.parseSizeToMegabytes(ramPerOrganization);
    this.workspacesPerOrganization = workspacesPerOrganization;
    this.runtimesPerOrganization = runtimesPerOrganization;
  }

  @Override
  public String getAccountType() {
    return OrganizationImpl.ORGANIZATIONAL_ACCOUNT;
  }

  @Override
  public List<ResourceImpl> getResources(String accountId)
      throws ServerException, NotFoundException {
    final Organization organization = organizationManager.getById(accountId);
    // only root organizations should have own resources
    if (organization.getParent() == null) {
      return asList(
          new ResourceImpl(TimeoutResourceType.ID, timeout, TimeoutResourceType.UNIT),
          new ResourceImpl(RamResourceType.ID, ramPerOrganization, RamResourceType.UNIT),
          new ResourceImpl(
              WorkspaceResourceType.ID, workspacesPerOrganization, WorkspaceResourceType.UNIT),
          new ResourceImpl(
              RuntimeResourceType.ID, runtimesPerOrganization, RuntimeResourceType.UNIT));
    }

    return Collections.emptyList();
  }
}

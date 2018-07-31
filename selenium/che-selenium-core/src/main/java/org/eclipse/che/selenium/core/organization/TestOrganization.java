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
package org.eclipse.che.selenium.core.organization;

import javax.annotation.PreDestroy;
import org.eclipse.che.multiuser.organization.shared.dto.OrganizationDto;
import org.eclipse.che.selenium.core.client.TestOrganizationServiceClient;

/**
 * Represents organization in a test environment.
 *
 * @author Dmytro Nochevnov
 */
public class TestOrganization {
  private final OrganizationDto organization;
  private final TestOrganizationServiceClient testOrganizationServiceClient;

  public TestOrganization(String name, TestOrganizationServiceClient testOrganizationServiceClient)
      throws Exception {
    this.testOrganizationServiceClient = testOrganizationServiceClient;
    organization = testOrganizationServiceClient.create(name);
  }

  public TestOrganization(
      String name, String parentId, TestOrganizationServiceClient testOrganizationServiceClient)
      throws Exception {
    this.testOrganizationServiceClient = testOrganizationServiceClient;
    organization = testOrganizationServiceClient.create(name, parentId);
  }

  /** Returns the name of the organization. */
  public String getName() {
    return organization.getName();
  }

  /** Returns the id of the organization. */
  public String getId() {
    return organization.getId();
  }

  public void addAdmin(String userId) throws Exception {
    testOrganizationServiceClient.addAdmin(getId(), userId);
  }

  public void addMember(String userId) throws Exception {
    testOrganizationServiceClient.addMember(getId(), userId);
  }

  /** Deletes organization. */
  @PreDestroy
  public void delete() throws Exception {
    testOrganizationServiceClient.deleteById(getId());
  }

  public String getQualifiedName() {
    return organization.getQualifiedName();
  }
}

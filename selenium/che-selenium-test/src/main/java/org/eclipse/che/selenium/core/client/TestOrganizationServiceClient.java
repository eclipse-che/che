/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core.client;

import java.util.List;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.multiuser.organization.shared.dto.OrganizationDto;
/** @author Ihor Okhrimenko */
public interface TestOrganizationServiceClient {

  List<OrganizationDto> getAll() throws Exception;

  List<OrganizationDto> getAll(@Nullable String parent) throws Exception;

  OrganizationDto create(String name, String parentId) throws Exception;

  OrganizationDto create(String name) throws Exception;

  void deleteById(String id) throws Exception;

  void deleteByName(String name) throws Exception;

  void deleteAll(String user) throws Exception;

  OrganizationDto get(String organizationName) throws Exception;

  void addMember(String organizationId, String userId) throws Exception;

  void addAdmin(String organizationId, String userId) throws Exception;

  void addMember(String organizationId, String userId, List<String> actions) throws Exception;
}

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
package org.eclipse.che.multiuser.organization.shared.dto;

import java.util.List;
import org.eclipse.che.api.core.rest.shared.dto.Hyperlinks;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.dto.shared.DTO;
import org.eclipse.che.multiuser.organization.shared.model.Organization;

/** @author Sergii Leschenko */
@DTO
public interface OrganizationDto extends Organization, Hyperlinks {
  @Override
  String getId();

  void setId(String id);

  OrganizationDto withId(String id);

  @Override
  String getName();

  void setName(String name);

  OrganizationDto withName(String name);

  @Override
  String getQualifiedName();

  void setQualifiedName(String qualifiedName);

  OrganizationDto withQualifiedName(String qualifiedName);

  @Override
  String getParent();

  void setParent(String parent);

  OrganizationDto withParent(String parent);

  @Override
  OrganizationDto withLinks(List<Link> links);
}

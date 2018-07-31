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
package org.eclipse.che.multiuser.api.permission.shared.dto;

import java.util.List;
import org.eclipse.che.dto.shared.DTO;
import org.eclipse.che.multiuser.api.permission.shared.model.PermissionsDomain;

/** @author Sergii Leschenko */
@DTO
public interface DomainDto extends PermissionsDomain {
  @Override
  String getId();

  void setId(String id);

  DomainDto withId(String id);

  @Override
  List<String> getAllowedActions();

  void setAllowedActions(List<String> allowedActions);

  DomainDto withAllowedActions(List<String> allowedActions);

  @Override
  Boolean isInstanceRequired();

  void setInstanceRequired(Boolean isInstanceRequired);

  DomainDto withInstanceRequired(Boolean isInstanceRequired);
}

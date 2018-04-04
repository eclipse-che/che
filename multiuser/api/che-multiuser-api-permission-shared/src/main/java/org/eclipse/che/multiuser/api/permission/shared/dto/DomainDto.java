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

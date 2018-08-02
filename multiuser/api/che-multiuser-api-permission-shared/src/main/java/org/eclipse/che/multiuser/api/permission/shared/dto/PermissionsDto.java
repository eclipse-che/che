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
package org.eclipse.che.multiuser.api.permission.shared.dto;

import java.util.List;
import org.eclipse.che.dto.shared.DTO;
import org.eclipse.che.multiuser.api.permission.shared.model.Permissions;

/** @author Sergii Leschenko */
@DTO
public interface PermissionsDto extends Permissions {
  @Override
  String getUserId();

  void setUserId(String userId);

  PermissionsDto withUserId(String userId);

  @Override
  String getDomainId();

  void setDomainId(String domainId);

  PermissionsDto withDomainId(String domainId);

  @Override
  String getInstanceId();

  void setInstanceId(String instanceId);

  PermissionsDto withInstanceId(String instanceId);

  @Override
  List<String> getActions();

  void setActions(List<String> actions);

  PermissionsDto withActions(List<String> actions);
}

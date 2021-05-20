/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.devfile.shared.dto;

import com.google.common.annotations.Beta;
import java.util.List;
import org.eclipse.che.api.core.model.workspace.devfile.UserDevfile;
import org.eclipse.che.api.core.rest.shared.dto.Hyperlinks;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.workspace.shared.dto.devfile.DevfileDto;
import org.eclipse.che.dto.shared.DTO;

@DTO
@Beta
public interface UserDevfileDto extends UserDevfile, Hyperlinks {

  void setId(String id);

  UserDevfileDto withId(String id);

  String getId();

  void setName(String name);

  UserDevfileDto withName(String name);

  String getName();

  void setDescription(String name);

  UserDevfileDto withDescription(String name);

  String getDescription();

  @Override
  DevfileDto getDevfile();

  void setDevfile(DevfileDto devfile);

  @Override
  String getNamespace();

  void setNamespace(String namespace);

  UserDevfileDto withNamespace(String namespace);

  UserDevfileDto withDevfile(DevfileDto devfile);

  UserDevfileDto withLinks(List<Link> links);
}

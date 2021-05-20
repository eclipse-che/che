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
package org.eclipse.che.api.workspace.shared.dto.devfile;

import java.util.Map;
import org.eclipse.che.api.core.model.workspace.devfile.Endpoint;
import org.eclipse.che.dto.shared.DTO;

/** @author Sergii Leshchenko */
@DTO
public interface EndpointDto extends Endpoint {

  @Override
  String getName();

  void setName(String name);

  EndpointDto withName(String name);

  @Override
  Integer getPort();

  void setPort(Integer port);

  EndpointDto withPort(Integer port);

  @Override
  Map<String, String> getAttributes();

  void setAttributes(Map<String, String> attributes);

  EndpointDto withAttributes(Map<String, String> attributes);
}

/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.dto.definitions;

import java.io.Serializable;
import java.util.Map;
import org.eclipse.che.dto.shared.DTO;

/** DTO for testing serialization of fields with {@link java.io.Serializable} type */
@DTO
public interface DtoWithSerializable {
  int getId();

  DtoWithSerializable withId(int id);

  Serializable getObject();

  void setObject(Serializable object);

  DtoWithSerializable withObject(Serializable object);

  Map<String, Serializable> getObjectMap();

  void setObjectMap(Map<String, Serializable> map);

  DtoWithSerializable withObjectMap(Map<String, Serializable> map);
}

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
package org.eclipse.che.dto.definitions;

import org.eclipse.che.dto.shared.DTO;

/**
 * DTO for testing that the {@link org.eclipse.che.dto.generator.DtoGenerator} correctly generates
 * server implementations for simple DTO interface.
 *
 * @author Artem Zatsarynnyi
 */
@DTO
public interface SimpleDto {
  int getId();

  SimpleDto withId(int id);

  String getName();

  SimpleDto withName(String name);

  String getDefault();

  void setDefault(String s);

  SimpleDto withDefault(String s);
}

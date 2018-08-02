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
package org.eclipse.che.api.project.shared.dto;

import java.util.List;
import org.eclipse.che.api.core.model.project.type.Value;
import org.eclipse.che.dto.shared.DTO;

/**
 * Data transfer object (DTO) for Value
 *
 * @author gazarenkov
 */
@DTO
public interface ValueDto extends Value {

  @Override
  String getString();

  @Override
  List<String> getList();

  ValueDto withList(List<String> list);
}

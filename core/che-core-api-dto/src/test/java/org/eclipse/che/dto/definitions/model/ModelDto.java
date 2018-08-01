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
package org.eclipse.che.dto.definitions.model;

import java.util.List;
import org.eclipse.che.dto.shared.DTO;

/**
 * Test DTO extension for {@link Model}
 *
 * @author Eugene Voevodin
 */
@DTO
public interface ModelDto extends Model {

  @Override
  List<ModelComponentDto> getComponents();

  void setComponents(List<ModelComponentDto> components);

  ModelDto withComponents(List<ModelComponentDto> components);

  @Override
  ModelComponentDto getPrimary();

  void setPrimary(ModelComponentDto primary);

  ModelDto withPrimary(ModelComponentDto primary);
}

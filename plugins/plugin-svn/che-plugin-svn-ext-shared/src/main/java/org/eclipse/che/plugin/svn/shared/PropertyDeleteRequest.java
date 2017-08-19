/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.svn.shared;

import javax.validation.constraints.NotNull;
import org.eclipse.che.dto.shared.DTO;

/**
 * Remove a property from an item request.
 *
 * @author Vladyslav Zhukovskyi
 */
@DTO
public interface PropertyDeleteRequest extends PropertyRequest {
  /** {@inheritDoc} */
  @Override
  PropertyDeleteRequest withProjectPath(@NotNull final String projectPath);

  /** {@inheritDoc} */
  @Override
  PropertyDeleteRequest withName(String name);

  /** {@inheritDoc} */
  @Override
  PropertyDeleteRequest withDepth(Depth depth);

  /** {@inheritDoc} */
  @Override
  PropertyDeleteRequest withForce(boolean force);

  /** {@inheritDoc} */
  @Override
  PropertyDeleteRequest withPath(String path);
}

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
 * Set properties on files, directories, or revisions.
 *
 * @author Vladyslav Zhukovskyi
 */
@DTO
public interface PropertySetRequest extends PropertyRequest {
  /**
   * Property value.
   *
   * @return property value
   */
  String getValue();

  /**
   * Property value.
   *
   * @param value property value
   */
  void setValue(String value);

  /**
   * Property value.
   *
   * @param value property value
   */
  PropertySetRequest withValue(String value);

  /** {@inheritDoc} */
  @Override
  PropertySetRequest withProjectPath(@NotNull final String projectPath);

  /** {@inheritDoc} */
  @Override
  PropertySetRequest withName(String name);

  /** {@inheritDoc} */
  @Override
  PropertySetRequest withDepth(Depth depth);

  /** {@inheritDoc} */
  @Override
  PropertySetRequest withForce(boolean force);

  /** {@inheritDoc} */
  @Override
  PropertySetRequest withPath(String path);
}

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
package org.eclipse.che.api.promises.client;

import org.eclipse.che.commons.annotation.Nullable;

/**
 * Represents a promise rejection reason.
 *
 * @author Mickaël Leduque
 * @author Artem Zatsarynnyi
 */
public interface PromiseError {

  /**
   * Returns the error message.
   *
   * @return the error message
   */
  @Nullable
  String getMessage();

  /**
   * Returns the error cause. May returns {@code null} in case this {@link PromiseError} represents
   * a JS Error object.
   *
   * @return the error cause
   */
  @Nullable
  Throwable getCause();
}

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
package org.eclipse.che.api.core.model.workspace.devfile;

public interface Source {
  /** Returns type of source. It is mandatory. */
  String getType();

  /** Returns project's source location address. It is mandatory. */
  String getLocation();

  /**
   * Returns the name of the refspec to check out after the clone. This can be a branch, tag, commit
   * id or anything the particular source type understands. It is optional.
   */
  String getRefspec();
}

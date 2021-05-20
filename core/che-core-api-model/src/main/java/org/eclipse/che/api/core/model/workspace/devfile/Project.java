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
package org.eclipse.che.api.core.model.workspace.devfile;

public interface Project {
  /** Returns projects name. It is mandatory and unique per projects set. */
  String getName();

  /** Returns source where project should be cloned from. It is mandatory. */
  Source getSource();

  /**
   * Returns the path relative to the root of the projects to which this project should be cloned
   * into. This is a unix-style relative path (i.e. uses forward slashes). The path is invalid if it
   * is absolute or tries to escape the project root through the usage of '..'. If not specified,
   * defaults to the project name.
   */
  String getClonePath();
}

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

public interface Action {
  /** Returns action type. Is is mandatory. */
  String getType();

  /** Returns component to which given action relates. */
  String getComponent();

  /** Returns the actual action command-line string. */
  String getCommand();

  /** Returns the working directory where the command should be executed. It is optional. */
  String getWorkdir();

  /** Returns the name of the referenced IDE-specific configuration file. */
  String getReference();

  /** Returns the content of the referenced IDE-specific configuration file. */
  String getReferenceContent();
}

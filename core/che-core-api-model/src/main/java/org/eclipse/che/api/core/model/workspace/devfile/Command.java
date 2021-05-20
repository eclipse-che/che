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

import java.util.List;
import java.util.Map;

public interface Command {
  /** Returns the name of the command. It is mandatory and unique per commands set. */
  String getName();

  /** Returns preview url of the command. Optional parameter, can be null if not specified. */
  PreviewUrl getPreviewUrl();

  /**
   * Returns the command actions. Now the only one command must be specified in list but there are
   * plans to implement supporting multiple actions commands. It is mandatory.
   */
  List<? extends Action> getActions();

  /**
   * Returns the command attributes. Empty map is returned when command does not have attributes. It
   * is optional.
   */
  Map<String, String> getAttributes();
}

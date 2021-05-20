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

public interface Entrypoint {
  /**
   * Returns the name of the container to apply the entrypoint to. If not specified, the entrypoint
   * is modified on all matching containers.
   */
  String getContainerName();

  /**
   * Returns the name of the top level object in the referenced object list in which to search for
   * containers. If not specified, the objects to search through can have any name.
   */
  String getParentName();

  /**
   * Returns the selector for matching top level objects. If not specified, the objects to search
   * through can have any labels.
   */
  Map<String, String> getParentSelector();

  /**
   * The command to run in the component instead of the default one provided in the image of the
   * container. Defaults to to empty list, meaning use whatever is defined in the image.
   */
  List<String> getCommand();

  /**
   * The arguments to supply to the command running the component. The arguments are supplied either
   * to the default command provided in the image of the container or to the overridden command.
   * Defaults to empty list, meaning use whatever is defined in the image.
   */
  List<String> getArgs();
}

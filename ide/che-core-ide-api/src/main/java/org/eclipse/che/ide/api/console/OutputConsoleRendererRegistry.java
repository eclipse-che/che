/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.console;

import java.util.Set;
import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Registry to store the available console output renderers {@link OutputConsoleRenderer} as well as
 * links between their types and the types of command {@link Command}
 *
 * @author Victor Rubezhny
 */
public interface OutputConsoleRendererRegistry {

  /**
   * Register editor provider for file type.
   *
   * @param projectType
   * @param type
   * @param renderer
   */
  void register(@NotNull String type, @NotNull OutputConsoleRenderer renderer);

  /**
   * Registers a command type to be processed by a given type of renderer
   *
   * @param commandType
   * @param type
   */
  void registerCommandDefault(@NotNull String commandType, @NotNull String type);

  /**
   * Returns all the registered renderer types
   *
   * @return List of available renderer types
   */
  Set<String> getAllOutputRendererTypes();

  /**
   * Returns all the registered renderers
   *
   * @return List of available renderers
   */
  Set<OutputConsoleRenderer> getAllOutputRenderers();

  /**
   * Returns the set of renderers registered for a given type
   *
   * @param type
   * @return Set of renderers available for a given type
   */
  @Nullable
  Set<OutputConsoleRenderer> getOutputRenderers(String type);

  /**
   * Returns the set of renderers registered as the default ones for a given command type
   *
   * @param commandType
   * @return Set of renderers available for a given command type
   */
  Set<String> getCommandDefaultOutputRenderers(String commandType);
}

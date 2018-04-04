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
package org.eclipse.che.ide.project;

import org.eclipse.che.commons.annotation.Nullable;

/**
 * Registry for implementations of {@link ResolvingProjectStateHolder}.
 *
 * @author Roman Nikitenko
 */
public interface ResolvingProjectStateHolderRegistry {

  /**
   * Returns the state holder of Resolving project process for the specified project type or {@code
   * null} if none.
   *
   * @param projectType the project type for which you need to get corresponding holder
   * @return the holder of Resolving project sate process for the specified project type or {@code
   *     null} if none
   */
  @Nullable
  public ResolvingProjectStateHolder getResolvingProjectStateHolder(String projectType);
}

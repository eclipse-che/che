/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.editor;

import com.google.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

/**
 * Contains the list of paths that correspond to opened files and should not trigger events
 * notifications from file system because they are initiated by ourselves (e.g. refactoring, git
 * checkout etc.)
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class DeletedFilesController {
  private Set<String> deletedFiles = new HashSet<>();

  /**
   * Adds the path to the file which need to skip.
   *
   * @param path path to the file
   */
  public void add(String path) {
    deletedFiles.add(path);
  }

  /**
   * Removes the path to the file which need to skip.
   *
   * @param path path to the file
   * @return {@code true} if set contains the specified path
   */
  public boolean remove(String path) {
    return deletedFiles.remove(path);
  }

  /** Returns {@code true} if this set contains the specified path. */
  public boolean contains(String path) {
    return deletedFiles.contains(path);
  }
}

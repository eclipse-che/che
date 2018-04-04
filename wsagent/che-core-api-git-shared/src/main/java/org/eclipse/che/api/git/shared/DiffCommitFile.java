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
package org.eclipse.che.api.git.shared;

import org.eclipse.che.dto.shared.DTO;

/**
 * Describes the changes of the committed file.
 *
 * @author Shimon Ben Yair.
 */
@DTO
public interface DiffCommitFile {

  /** Returns the file change type. */
  String getChangeType();

  /** Set the file change type. */
  void setChangeType(String type);

  /**
   * Create a {@link DiffCommitFile} object based on a given file change type.
   *
   * @param type file change type
   * @return a {@link DiffCommitFile} object that contains information about changes of the
   *     committed file
   */
  DiffCommitFile withChangeType(String type);

  /** Returns the file previous location. */
  String getOldPath();

  /** Set the file previous location. */
  void setOldPath(String oldPath);

  /**
   * Create a {@link DiffCommitFile} object based on a given file previous location.
   *
   * @param oldPath file previous location
   * @return a {@link DiffCommitFile} object that contains information about changes of the
   *     committed file
   */
  DiffCommitFile withOldPath(String oldPath);

  /** Returns the file new location. */
  String getNewPath();

  /** Set the file new location. */
  void setNewPath(String newPath);

  /**
   * Create a {@link DiffCommitFile} object based on a given file new location.
   *
   * @param newPath file new location
   * @return a {@link DiffCommitFile} object that contains information about changes of the
   *     committed file
   */
  DiffCommitFile withNewPath(String newPath);
}

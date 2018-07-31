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
package org.eclipse.che.ide.ext.java.shared.dto.refactoring;

import org.eclipse.che.dto.shared.DTO;

/**
 * DTO represents the information about performed refactoring change.
 *
 * @author Valeriy Svydenko
 */
@DTO
public interface ChangeInfo {
  /** @return name of the change. */
  ChangeName getName();

  void setName(ChangeName name);

  /** @return path of the resource before applying changes. */
  String getOldPath();

  void setOldPath(String path);

  /** @return path of the resource after applying changes. */
  String getPath();

  void setPath(String path);

  enum ChangeName {
    RENAME_COMPILATION_UNIT,
    RENAME_PACKAGE,
    UPDATE,
    MOVE
  }
}

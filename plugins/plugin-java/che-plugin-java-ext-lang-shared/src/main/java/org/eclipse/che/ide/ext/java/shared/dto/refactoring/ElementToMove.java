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
 * Represents java compilation unit or package
 *
 * @author Evgen Vidolob
 */
@DTO
public interface ElementToMove {
  /**
   * Workspace path for package or FQN for compilation unit
   *
   * @return path or FQN
   */
  String getPath();

  void setPath(String path);

  /** @return true if this element is package and false if compilation unit */
  // TODO due limitation in DTO generator we can't name method 'isPackage'
  boolean isPack();

  void setPack(boolean pack);
}

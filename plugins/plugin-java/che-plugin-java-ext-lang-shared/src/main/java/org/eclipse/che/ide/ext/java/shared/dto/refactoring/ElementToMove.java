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

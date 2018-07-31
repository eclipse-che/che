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
package org.eclipse.che.ide.ext.java.shared;

import org.eclipse.che.dto.shared.DTO;

/** @author Evgen Vidolob */
@DTO
public interface JarEntry {
  enum JarEntryType {
    PACKAGE,
    FOLDER,
    CLASS_FILE,
    FILE
  }

  JarEntryType getType();

  void setType(JarEntryType type);

  String getName();

  void setName(String name);

  String getPath();

  void setPath(String path);
}

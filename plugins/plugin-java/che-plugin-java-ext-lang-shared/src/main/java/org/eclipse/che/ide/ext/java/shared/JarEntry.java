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

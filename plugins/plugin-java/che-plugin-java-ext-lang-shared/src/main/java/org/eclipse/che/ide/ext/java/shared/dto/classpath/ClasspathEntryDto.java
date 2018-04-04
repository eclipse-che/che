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
package org.eclipse.che.ide.ext.java.shared.dto.classpath;

import java.util.List;
import org.eclipse.che.dto.shared.DTO;

/**
 * DTO represents the information about classpath of the project.
 *
 * @author Valeriy Svydenko
 */
@DTO
public interface ClasspathEntryDto {
  /** Returns type of the entry. */
  int getEntryKind();

  void setEntryKind(int kind);

  ClasspathEntryDto withEntryKind(int kind);

  /** Returns path to the entry. */
  String getPath();

  void setPath(String path);

  ClasspathEntryDto withPath(String path);

  /** Returns sub entries. */
  List<ClasspathEntryDto> getExpandedEntries();

  void setExpandedEntries(List<ClasspathEntryDto> expandedEntries);

  ClasspathEntryDto withExpandedEntries(List<ClasspathEntryDto> expandedEntries);
}

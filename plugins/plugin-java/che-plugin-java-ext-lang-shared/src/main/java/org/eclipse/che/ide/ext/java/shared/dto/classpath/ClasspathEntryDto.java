/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

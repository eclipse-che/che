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
package org.eclipse.che.api.project.shared.dto;

import org.eclipse.che.dto.shared.DTO;

/**
 * DTO represents the information about a text change of editor content.
 *
 * @author Roman Nikitenko
 */
@DTO
public interface EditorChangesDto {
  /** Returns the full path to the file that was changed */
  String getFileLocation();

  EditorChangesDto withFileLocation(String fileLocation);

  /** Returns the path to the project that contains the modified file */
  String getProjectPath();

  EditorChangesDto withProjectPath(String path);

  Type getType();

  EditorChangesDto withType(Type type);

  /** Returns the offset of the change. */
  int getOffset();

  EditorChangesDto withOffset(int offset);

  /** Returns length of the text change. */
  int getLength();

  EditorChangesDto withLength(int length);

  /** Returns text of the change. */
  String getText();

  EditorChangesDto withText(String text);

  /** Returns the number of characters removed from the file. */
  int getRemovedCharCount();

  EditorChangesDto withRemovedCharCount(int removedCharCount);

  enum Type {
    /** Identifies an insert operation. */
    INSERT,
    /** Identifies an remove operation. */
    REMOVE,
  }
}

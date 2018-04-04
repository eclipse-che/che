/**
 * ***************************************************************************** Copyright (c) 2016
 * Rogue Wave Software, Inc. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Rogue Wave Software, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.api.testing.shared.dto;

import org.eclipse.che.dto.shared.DTO;

/**
 * Provides info about file to be opened in the editor and line number on which the selection should
 * be made.
 *
 * @author Bartlomiej Laczkowski
 */
@DTO
public interface SimpleLocationDto {

  /**
   * Returns path to resource that should be opened in the editor.
   *
   * @return path to resource that should be opened in the editor
   */
  String getResourcePath();

  /**
   * Sets path to resource that should be opened in the editor.
   *
   * @param resourcePath
   */
  void setResourcePath(String resourcePath);

  /**
   * Returns line number that should be selected in the editor.
   *
   * @return line number that should be selected in the editor.
   */
  int getLineNumber();

  /**
   * Sets the line number that should be selected in the editor.
   *
   * @param lineNumber
   */
  void setLineNumber(int lineNumber);
}

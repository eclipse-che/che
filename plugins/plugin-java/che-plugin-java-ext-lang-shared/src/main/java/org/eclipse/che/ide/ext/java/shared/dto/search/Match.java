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
package org.eclipse.che.ide.ext.java.shared.dto.search;

import org.eclipse.che.dto.shared.DTO;
import org.eclipse.che.ide.ext.java.shared.dto.Region;

/**
 * A textual match in a given object.
 *
 * @author Evgen Vidolob
 */
@DTO
public interface Match {

  /**
   * Match region in file.
   *
   * @return the match region.
   */
  Region getFileMatchRegion();

  void setFileMatchRegion(Region region);

  /**
   * String content of matched line.
   *
   * @return the line content
   */
  String getMatchedLine();

  void setMatchedLine(String matchedLine);

  /**
   * Match region in matched line. Used for UI purpose, to highlight matched word in matched line.
   *
   * @return the match region.
   */
  Region getMatchInLine();

  void setMatchInLine(Region region);

  /**
   * The line number of matched line.
   *
   * @return the line number.
   */
  int getMatchLineNumber();

  void setMatchLineNumber(int lineNumber);
}

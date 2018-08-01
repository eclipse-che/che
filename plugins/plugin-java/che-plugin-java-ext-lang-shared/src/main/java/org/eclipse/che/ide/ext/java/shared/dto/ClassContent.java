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
package org.eclipse.che.ide.ext.java.shared.dto;

import org.eclipse.che.dto.shared.DTO;

/**
 * Represents content of *.class file. if jar has attached sources contains source of class
 *
 * @author Evgen Vidolob
 */
@DTO
public interface ClassContent {

  /** @return the content of class file */
  String getContent();

  void setContent(String content);

  /**
   * @return true if content of class generated(decompiled) from .class file byte code, false if jar
   *     has attached sources
   */
  boolean isGenerated();

  void setGenerated(boolean generated);
}

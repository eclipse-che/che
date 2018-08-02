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
package org.eclipse.che.ide.ext.java.shared.dto.refactoring;

import org.eclipse.che.dto.shared.DTO;

/**
 * DTO represents refactoring change preview.
 *
 * @author Evgen Vidolob
 */
@DTO
public interface ChangePreview {
  /** @return part of the old content */
  String getOldContent();

  void setOldContent(String oldContent);

  /** @return part of the new content */
  String getNewContent();

  void setNewContent(String newContent);

  /** @return name of the file which has corresponding change */
  String getFileName();

  void setFileName(String name);
}

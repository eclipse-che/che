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
package org.eclipse.che.ide.ext.java.shared;

import org.eclipse.che.dto.shared.DTO;

/** @author Evgen Vidolob */
@DTO
public interface OpenDeclarationDescriptor {
  int getLibId();

  void setLibId(int libId);

  int getOffset();

  void setOffset(int offset);

  int getLength();

  void setLength(int length);

  String getPath();

  void setPath(String path);

  boolean isBinary();

  void setBinary(boolean binary);
}

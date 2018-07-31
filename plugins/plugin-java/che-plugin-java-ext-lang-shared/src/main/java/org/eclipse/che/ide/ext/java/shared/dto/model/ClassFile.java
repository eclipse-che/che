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
package org.eclipse.che.ide.ext.java.shared.dto.model;

import org.eclipse.che.dto.shared.DTO;

/**
 * Represents an entire binary type (single <code>.class</code> file). A class file has a single
 * child of type <code>IType</code>.
 *
 * @author Evgen Vidolob
 */
@DTO
public interface ClassFile extends TypeRoot, LabelElement {

  /**
   * Returns the type contained in this class file. This is a handle-only method. The type may or
   * may not exist.
   *
   * @return the type contained in this class file
   */
  Type getType();

  void setType(Type type);
}

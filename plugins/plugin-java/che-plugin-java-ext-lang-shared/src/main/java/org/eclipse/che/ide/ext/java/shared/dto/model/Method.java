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
package org.eclipse.che.ide.ext.java.shared.dto.model;

import org.eclipse.che.dto.shared.DTO;

/**
 * Represents a method (or constructor) declared in a type.
 *
 * @author Evgen Vidolob
 * @author Valeriy Svydenko
 */
@DTO
public interface Method extends Member {
  /**
   * Returns the simple name of this method. For a constructor, this returns the simple name of the
   * declaring type. Note: This holds whether the constructor appears in a source or binary type
   * (even though class files internally define constructor names to be <code>"&lt;init&gt;"</code>
   * ). For the class initialization methods in binary types, this returns the special name <code>
   * "&lt;clinit&gt;"</code>. This is a handle-only method.
   *
   * @return the simple name of this method
   */
  String getElementName();

  void setElementName(String elementName);

  /** @return name of the return type */
  String getReturnType();

  void setReturnType(String returnType);
}

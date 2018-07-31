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
 * Represents an import declaration in Java compilation unit.
 *
 * @author Evgen Vidolob
 */
@DTO
public interface ImportDeclaration extends JavaElement {

  /**
   * Returns the name that has been imported. For an on-demand import, this includes the trailing
   * <code>".*"</code>. For example, for the statement <code>"import java.util.*"</code>, this
   * returns <code>"java.util.*"</code>. For the statement <code>"import java.util.Hashtable"</code>
   * , this returns <code>"java.util.Hashtable"</code>.
   *
   * @return the name that has been imported
   */
  String getElementName();

  void setElementName(String elementName);

  /**
   * Returns the modifier flags for this import. The flags can be examined using class <code>Flags
   * </code>. Only the static flag is meaningful for import declarations.
   *
   * @return the modifier flags for this import
   */
  int getFlags();

  void setFlags(int flags);
}

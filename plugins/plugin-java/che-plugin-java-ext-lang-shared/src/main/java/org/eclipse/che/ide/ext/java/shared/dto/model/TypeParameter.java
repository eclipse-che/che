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

import java.util.List;
import org.eclipse.che.dto.shared.DTO;

/**
 * Represents a type parameter defined by a type or a method in a compilation unit or a class file.
 *
 * @author Evgen Vidolob
 */
@DTO
public interface TypeParameter extends JavaElement {
  /**
   * Returns the names of the class and interface bounds of this type parameter. Returns an empty
   * array if this type parameter has no bounds. A bound name is the name as it appears in the
   * source (without the <code>extends</code> keyword) if the type parameter comes from a
   * compilation unit. It is the dot-separated fully qualified name of the bound if the type
   * parameter comes from a class file.
   *
   * @return the names of the bounds
   */
  List<String> getBounds();

  void setBounds(List<String> bounds);
}

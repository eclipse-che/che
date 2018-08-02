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
 * Represents a field declared in a type.
 *
 * @author Evgen Vidolob
 */
@DTO
public interface Field extends Member {
  /**
   * Returns the simple name of this field.
   *
   * @return the simple name of this field.
   */
  String getElementName();

  void setElementName(String elementName);
}

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
 * Highlighted Positions. Mostly used for semantic highlighting.
 *
 * @author Evgen Vidolob
 */
@DTO
public interface HighlightedPosition {

  /** A type that do the highlighting of static final fields. */
  String STATIC_FINAL_FIELD = "staticFinalField";
  /** A type that do the highlighting of static fields. */
  String STATIC_FIELD = "staticField";
  /** A type that do the highlighting of fields. */
  String FIELD = "field";
  /** A type that do the highlighting of static method invocations. */
  String STATIC_METHOD_INVOCATION = "staticMethodInvocation";
  /** A type that do the highlighting of deprecated members. */
  String DEPRECATED_MEMBER = "deprecatedMember";
  /** A type that do the highlighting of type parameters. */
  String TYPE_VARIABLE = "typeParameter";
  /** A type that do the highlighting of method declarations. */
  String METHOD_DECLARATION = "methodDeclaration";

  /**
   * Returns the length of this position.
   *
   * @return the length of this position
   */
  int getLength();

  void setLength(int length);

  /**
   * Returns the offset of this position.
   *
   * @return the offset of this position
   */
  int getOffset();

  void setOffset(int offset);

  /**
   * Type of highlighting. Used for selecting proper css style for this highlighting; Example:<code>
   * staticFinalField, staticField, field
   * </code>
   *
   * @return
   */
  String getType();

  void setType(String type);
}

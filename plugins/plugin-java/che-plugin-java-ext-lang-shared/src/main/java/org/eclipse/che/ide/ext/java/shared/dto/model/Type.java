/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.java.shared.dto.model;

import java.util.List;
import org.eclipse.che.dto.shared.DTO;

/**
 * Represents either a source type in a compilation unit (either a top-level type, a member type, a
 * local type, an anonymous type or a lambda expression) or a binary type in a class file.
 * Enumeration classes and annotation types are subkinds of classes and interfaces, respectively.
 *
 * @author Evgen Vidolob
 */
@DTO
public interface Type extends Member, LabelElement {

  /**
   * Returns the fields declared by this type in the order in which they appear in the source or
   * class file. For binary types, this includes synthetic fields.
   *
   * @return the fields declared by this type
   */
  List<Field> getFields();

  void setFields(List<Field> fields);

  /**
   * Returns the initializers declared by this type. For binary types this is an empty collection.
   * For source types, the results are listed in the order in which they appear in the source.
   *
   * @return the initializers declared by this type
   */
  List<Initializer> getInitializers();

  void setInitializers(List<Initializer> initializers);

  /**
   * Returns the methods and constructors declared by this type. For binary types, this may include
   * the special <code>&lt;clinit&gt;</code> method and synthetic methods.
   *
   * <p>The results are listed in the order in which they appear in the source or class file.
   *
   * @return the methods and constructors declared by this type
   */
  List<Method> getMethods();

  void setMethods(List<Method> methods);

  /**
   * Returns the immediate member types declared by this type. The results are listed in the order
   * in which they appear in the source or class file.
   *
   * @return the immediate member types declared by this type
   */
  List<Type> getTypes();

  void setTypes(List<Type> types);

  /**
   * If this type is primary (that is, the type with the same name as the compilation unit, or the
   * type of a class file) return true.
   *
   * @return true if this type is primary, false otherwise
   */
  boolean isPrimary();

  void setPrimary(boolean primary);
}

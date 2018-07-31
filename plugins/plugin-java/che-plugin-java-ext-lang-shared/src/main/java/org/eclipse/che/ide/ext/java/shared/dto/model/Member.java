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

import org.eclipse.che.ide.ext.java.shared.dto.Region;

/**
 * Common protocol for Java elements that can be members of types. This set consists of <code>Type
 * </code>, <code>Method</code>, <code>Field</code>, and <code>Initializer</code>.
 *
 * @author Evgen Vidolob
 * @author Valeriy Svydenko
 */
public interface Member extends JavaElement, LabelElement {
  /**
   * Returns the modifier flags for this member. The flags can be examined using class <code>Flags
   * </code>.
   *
   * <p>For source members, only flags as indicated in the source are returned. Thus if an interface
   * defines a method <code>void myMethod();</code>, the flags don't include the 'public' flag.
   * Source flags include {@link Flags#AccAnnotationDefault} as well.
   *
   * @return the modifier flags for this member
   */
  int getFlags();

  void setFlags(int flags);

  /**
   * Match region in file.
   *
   * @return the match region.
   */
  Region getFileRegion();

  void setFileRegion(Region region);

  /** Returns true if from a class file, and false if from a compilation unit. */
  boolean isBinary();

  void setBinary(boolean binary);

  /**
   * Returns path to the binary class which is a parent of the current member. Value is not <code>
   * null</code> if member is binary.
   */
  String getRootPath();

  void setRootPath(String rootPath);

  /**
   * Returns id of the library which contains the binary class which is a parent of the current
   * member. Value is not <code>null</code> if member is binary.
   */
  int getLibId();

  void setLibId(int libId);
}

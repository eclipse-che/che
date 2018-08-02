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

import java.util.List;
import org.eclipse.che.dto.shared.DTO;

/**
 * DTO represents java package fragment.
 *
 * @author Evgen Vidolob
 */
@DTO
public interface PackageFragment extends JavaElement, Openable {

  /**
   * Returns all of the class files in this package fragment.
   *
   * <p>Note: it is possible that a package fragment contains only compilation units (in other
   * words, its kind is <code>K_SOURCE</code>), in which case this method returns an empty
   * collection.
   *
   * @return all of the class files in this package fragment
   */
  List<ClassFile> getClassFiles();

  void setClassFiles(List<ClassFile> classFiles);

  /**
   * Returns all of the compilation units in this package fragment.
   *
   * <p>Note: it is possible that a package fragment contains only class files (in other words, its
   * kind is <code>K_BINARY</code>), in which case this method returns an empty collection.
   *
   * @return all of the compilation units in this package fragment
   */
  List<CompilationUnit> getCompilationUnits();

  void setCompilationUnits(List<CompilationUnit> compilationUnits);

  /**
   * Returns this package fragment's root kind encoded as an integer. A package fragment can contain
   * source files (i.e. files with one of the Java-like extensions), or <code>.class</code> files.
   *
   * @return this package fragment's root kind encoded as an integer
   * @see PackageFragmentRoot#K_SOURCE
   * @see PackageFragmentRoot#K_BINARY
   */
  int getKind();

  void setKind(int kind);

  /**
   * Returns whether this package fragment is a default package. This is a handle-only method.
   *
   * @return true if this package fragment is a default package
   */
  boolean isDefaultPackage();

  void setDefaultPackage(boolean defaultPackage);
}

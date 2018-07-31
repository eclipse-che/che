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
 * DTO represents package fragment root.
 *
 * @author Evgen Vidolob
 */
@DTO
public interface PackageFragmentRoot extends JavaElement, Openable {
  /** Kind constant for a source path root. Indicates this root only contains source files. */
  int K_SOURCE = 1;
  /** Kind constant for a binary path root. Indicates this root only contains binary files. */
  int K_BINARY = 2;

  /**
   * All package fragments in this package fragment root.
   *
   * @return list of the package fragments
   */
  List<PackageFragment> getPackageFragments();

  /**
   * Set package fragments
   *
   * @param fragments list of the package fragments
   */
  void setPackageFragments(List<PackageFragment> fragments);

  /**
   * Returns this package fragment root's kind encoded as an integer. A package fragment root can
   * contain source files (i.e. files with one of the Java-like extensions, or <code>.class</code>
   * files, but not both. If the underlying folder or archive contains other kinds of files, they
   * are ignored. In particular, <code>.class</code> files are ignored under a source package
   * fragment root, and source files are ignored under a binary package fragment root.
   *
   * @return this package fragment root's kind encoded as an integer
   * @see PackageFragmentRoot#K_SOURCE
   * @see PackageFragmentRoot#K_BINARY
   */
  int getKind();

  void setKind(int kind);
}

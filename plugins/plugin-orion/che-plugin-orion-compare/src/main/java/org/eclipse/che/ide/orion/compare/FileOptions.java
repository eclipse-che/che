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
package org.eclipse.che.ide.orion.compare;

/**
 * his object describes options of a file. Two instances of this object construct the core
 * parameters of a compare view.
 *
 * @author Evgen Vidolob
 */
public interface FileOptions {

  /**
   * Content the text contents of the file unit.
   *
   * @param content
   */
  void setContent(String content);

  /**
   * Name the file name.
   *
   * @param name
   */
  void setName(String name);

  /**
   * whether or not the file is in readonly mode.
   *
   * @param readOnly
   */
  void setReadOnly(boolean readOnly);
}

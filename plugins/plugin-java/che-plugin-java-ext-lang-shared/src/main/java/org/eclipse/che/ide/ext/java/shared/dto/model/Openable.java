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

/**
 * Common protocol for Java elements that must be opened before they can be navigated or modified.
 *
 * @author Evgen Vidolob
 */
public interface Openable {

  /**
   * Workspace path of this element
   *
   * @return the path
   */
  String getPath();

  void setPath(String path);

  /**
   * Project workspace path
   *
   * @return the path
   */
  String getProjectPath();

  void setProjectPath(String path);
}

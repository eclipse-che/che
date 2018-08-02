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
package org.eclipse.che.api.project.shared;

/**
 * Progress record that holds output information about the importing status.
 *
 * @author Vlad Zhukovskyi
 * @since 5.9.0
 */
public interface ImportProgressRecord {

  /**
   * Record line number.
   *
   * @return record line number
   */
  int getNum();

  /**
   * Record line.
   *
   * @return record line
   */
  String getLine();

  /**
   * Return project name.
   *
   * @return project name
   */
  String getProjectName();
}

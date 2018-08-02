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
package org.eclipse.che.ide.api.project.type.wizard;

/**
 * Provides, to extensions, a way to set the default project type to be pre selected. Project
 * wizards will use {@link PreSelectedProjectTypeManager#getPreSelectedProjectTypeId()} to get the
 * type to be preselected
 */
public interface PreSelectedProjectTypeManager {

  /**
   * To be used by project wizards to get the project type to preselect when no type is selected.
   *
   * @return The project id or an empty string if none.
   */
  String getPreSelectedProjectTypeId();

  /** Set projectType to preselect. lowest priority value will get selected. */
  void setProjectTypeIdToPreselect(String projectTypeId, int priority);
}

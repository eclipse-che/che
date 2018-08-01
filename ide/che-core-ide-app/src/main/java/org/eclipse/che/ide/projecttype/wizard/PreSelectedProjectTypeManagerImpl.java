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
package org.eclipse.che.ide.projecttype.wizard;

import org.eclipse.che.ide.api.project.type.wizard.PreSelectedProjectTypeManager;

public class PreSelectedProjectTypeManagerImpl implements PreSelectedProjectTypeManager {

  protected String projectTypeId = "";
  private int priority = 0;

  @Override
  public String getPreSelectedProjectTypeId() {
    return projectTypeId;
  }

  @Override
  public void setProjectTypeIdToPreselect(String projectTypeId, int priority) {
    if ("".equals(this.projectTypeId) || priority <= this.priority) {
      this.projectTypeId = projectTypeId;
      this.priority = priority;
    }
  }
}

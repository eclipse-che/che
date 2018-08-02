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

import javax.validation.constraints.NotNull;

/**
 * Defines modes used to open a project wizard.
 *
 * @author Artem Zatsarynnyi
 */
public enum ProjectWizardMode {

  /** Project wizard opened for creating new project. */
  CREATE("create"),
  /**
   * Project wizard opened for updating existing project or creating configuration for the existed
   * folder.
   */
  UPDATE("update"),
  /** Project wizard opened for creating new project from template. */
  IMPORT("import");

  private final String value;

  ProjectWizardMode(String value) {
    this.value = value;
  }

  public static ProjectWizardMode parse(@NotNull String mode) {
    for (ProjectWizardMode wizardMode : values()) {
      if (mode.equals(wizardMode.toString())) {
        return wizardMode;
      }
    }

    throw new IllegalArgumentException("Unknown value: " + mode);
  }

  @Override
  public String toString() {
    return value;
  }
}

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
package org.eclipse.che.ide.projecttype.wizard;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar;
import org.eclipse.che.ide.util.loging.Log;

/**
 * Picks-up all bounded {@link ProjectWizardRegistrar}s to be able to return it for the particular
 * project type ID.
 */
@Singleton
public class ProjectWizardRegistry {

  private static final String DEFAULT_CATEGORY = "Other";

  private final Map<String, ProjectWizardRegistrar> registrars = new HashMap<>();

  @Inject(optional = true)
  private void register(Set<ProjectWizardRegistrar> registrars) {
    for (ProjectWizardRegistrar registrar : registrars) {
      final String id = registrar.getProjectTypeId();

      if (this.registrars.containsKey(id)) {
        Log.warn(
            ProjectWizardRegistry.class, "Wizard for project type " + id + " already registered.");
      } else {
        this.registrars.put(id, registrar);
      }
    }
  }

  /**
   * Get a {@link ProjectWizardRegistrar} for the specified project type.
   *
   * @param projectTypeId the ID of the project type to get an appropriate {@link
   *     ProjectWizardRegistrar}
   * @return {@link ProjectWizardRegistrar} for the specified project type ID or {@code
   *     Optional#empty()} if none
   */
  public Optional<ProjectWizardRegistrar> getWizardRegistrar(String projectTypeId) {
    return Optional.ofNullable(registrars.get(projectTypeId));
  }

  /**
   * Returns wizard category of the specified {@code projectTypeId}.
   *
   * @param projectTypeId the ID of the project type to get it's wizard category
   * @return wizard category of the specified {@code projectTypeId} or {@code Optional#empty()}
   */
  public Optional<String> getWizardCategory(String projectTypeId) {
    final ProjectWizardRegistrar registrar = registrars.get(projectTypeId);

    if (registrar != null) {
      final String category = registrar.getCategory();

      return Optional.of(category.isEmpty() ? DEFAULT_CATEGORY : category);
    }

    return Optional.empty();
  }
}

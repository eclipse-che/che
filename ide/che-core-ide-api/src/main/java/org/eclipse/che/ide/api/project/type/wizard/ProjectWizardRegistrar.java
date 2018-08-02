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

import com.google.inject.Provider;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.wizard.WizardPage;

/**
 * Defines the requirements for an object that provides an information for registering project type
 * into project wizard.
 *
 * <p>Implementations of this interface need to be registered using a multibinder in order to be
 * picked-up by project wizard.
 *
 * @author Artem Zatsarynnyi
 */
public interface ProjectWizardRegistrar {

  /** Key allows to get project wizard mode from wizard's context. */
  String WIZARD_MODE_KEY = "ProjectWizard:Mode";

  /**
   * Key allows to get project's name from wizard's context when project wizard opened for updating
   * project.
   */
  String PROJECT_NAME_KEY = "ProjectWizard:CurrentProjectName";

  /** Returns ID of the project type that should be registered in project wizard. */
  String getProjectTypeId();

  /** Returns project type category for the project wizard. */
  @NotNull
  String getCategory();

  /** Returns pages that should be used in project wizard. */
  List<Provider<? extends WizardPage<MutableProjectConfig>>> getWizardPages();
}

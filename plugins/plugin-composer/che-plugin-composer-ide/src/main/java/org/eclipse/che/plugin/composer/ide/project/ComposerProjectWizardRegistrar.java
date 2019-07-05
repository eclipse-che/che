/*
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.composer.ide.project;

import static org.eclipse.che.plugin.composer.shared.Constants.COMPOSER_PROJECT_TYPE_ID;
import static org.eclipse.che.plugin.php.shared.Constants.PHP_CATEGORY;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar;
import org.eclipse.che.ide.api.wizard.WizardPage;

/**
 * Provides information for registering Composer project type into project wizard.
 *
 * @author Kaloyan Raev
 */
public class ComposerProjectWizardRegistrar implements ProjectWizardRegistrar {

  private final List<Provider<? extends WizardPage<MutableProjectConfig>>> wizardPages;

  @Inject
  public ComposerProjectWizardRegistrar(Provider<ComposerPagePresenter> pagePresenter) {
    wizardPages = new ArrayList<>();
    wizardPages.add(pagePresenter);
  }

  @Override
  @NotNull
  public String getProjectTypeId() {
    return COMPOSER_PROJECT_TYPE_ID;
  }

  @Override
  @NotNull
  public String getCategory() {
    return PHP_CATEGORY;
  }

  @Override
  @NotNull
  public List<Provider<? extends WizardPage<MutableProjectConfig>>> getWizardPages() {
    return wizardPages;
  }
}

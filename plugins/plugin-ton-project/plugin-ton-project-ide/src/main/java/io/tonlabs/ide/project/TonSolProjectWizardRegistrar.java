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
package io.tonlabs.ide.project;

import com.google.inject.Provider;
import io.tonlabs.shared.Constants;
import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar;
import org.eclipse.che.ide.api.wizard.WizardPage;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/** The project wizard for creating a new TON C project. */
public class TonSolProjectWizardRegistrar implements ProjectWizardRegistrar {

  private final List<Provider<? extends WizardPage<MutableProjectConfig>>> wizardPagesProviders;

  /** Constructor for TON C Project wizard. */
  public TonSolProjectWizardRegistrar() {
    this.wizardPagesProviders = new ArrayList<>();
  }

  @NotNull
  public String getProjectTypeId() {
    return Constants.TON_SOL_PROJECT_TYPE_ID;
  }

  @NotNull
  public String getCategory() {
    return Constants.TON_CATEGORY;
  }

  @NotNull
  public List<Provider<? extends WizardPage<MutableProjectConfig>>> getWizardPages() {
    return this.wizardPagesProviders;
  }
}

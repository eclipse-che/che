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
package org.eclipse.che.plugin.java.plain.client.wizard;

import static org.eclipse.che.ide.ext.java.shared.Constants.JAVAC;
import static org.eclipse.che.ide.ext.java.shared.Constants.JAVA_CATEGORY;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar;
import org.eclipse.che.ide.api.wizard.WizardPage;

/**
 * Provides information for registering Plain Java project type into project wizard.
 *
 * @author Valeriy Svydenko
 */
public class PlainJavaProjectWizardRegistrar implements ProjectWizardRegistrar {
  private final List<Provider<? extends WizardPage<MutableProjectConfig>>> wizardPages;

  @Inject
  public PlainJavaProjectWizardRegistrar(
      Provider<PlainJavaPagePresenter> plainJavaPagePresenterProvider) {
    wizardPages = new ArrayList<>();
    wizardPages.add(plainJavaPagePresenterProvider);
  }

  @NotNull
  public String getProjectTypeId() {
    return JAVAC;
  }

  @NotNull
  public String getCategory() {
    return JAVA_CATEGORY;
  }

  @NotNull
  public List<Provider<? extends WizardPage<MutableProjectConfig>>> getWizardPages() {
    return wizardPages;
  }
}

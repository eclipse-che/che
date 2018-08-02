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
package org.eclipse.che.plugin.github.ide.importer;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.project.wizard.ImportWizardRegistrar;
import org.eclipse.che.ide.api.wizard.WizardPage;
import org.eclipse.che.plugin.github.ide.importer.page.GithubImporterPagePresenter;

/**
 * Provides information for registering GitHub importer into import wizard.
 *
 * @author Artem Zatsarynnyi
 */
public class GitHubImportWizardRegistrar implements ImportWizardRegistrar {
  private static final String ID = "github";
  private final List<Provider<? extends WizardPage<MutableProjectConfig>>> wizardPages;

  @Inject
  public GitHubImportWizardRegistrar(Provider<GithubImporterPagePresenter> provider) {
    wizardPages = new ArrayList<>();
    wizardPages.add(provider);
  }

  @NotNull
  public String getImporterId() {
    return ID;
  }

  @NotNull
  public List<Provider<? extends WizardPage<MutableProjectConfig>>> getWizardPages() {
    return wizardPages;
  }
}

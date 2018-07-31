/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.git.client.importer;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.project.wizard.ImportWizardRegistrar;
import org.eclipse.che.ide.api.wizard.WizardPage;
import org.eclipse.che.ide.ext.git.client.importer.page.GitImporterPagePresenter;

/**
 * Provides information for registering GIT importer into import wizard.
 *
 * @author Artem Zatsarynnyi
 */
public class GitImportWizardRegistrar implements ImportWizardRegistrar {
  private static final String ID = "git";
  private final List<Provider<? extends WizardPage<MutableProjectConfig>>> wizardPages;

  @Inject
  public GitImportWizardRegistrar(Provider<GitImporterPagePresenter> provider) {
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

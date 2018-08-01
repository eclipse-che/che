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
package org.eclipse.che.plugin.nodejs.ide.project;

import static org.eclipse.che.plugin.nodejs.ide.NodeJsExtension.NODE_JS_CATEGORY;
import static org.eclipse.che.plugin.nodejs.shared.Constants.NODE_JS_PROJECT_TYPE_ID;

import com.google.inject.Provider;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar;
import org.eclipse.che.ide.api.wizard.WizardPage;

/**
 * Provides information for registering Node Js project type into project wizard.
 *
 * @author Dmitry Shnurenko
 */
public class NodeJsProjectWizardRegistrar implements ProjectWizardRegistrar {

  private final List<Provider<? extends WizardPage<MutableProjectConfig>>> wizardPages;

  public NodeJsProjectWizardRegistrar() {
    wizardPages = new ArrayList<>();
  }

  @Override
  public String getProjectTypeId() {
    return NODE_JS_PROJECT_TYPE_ID;
  }

  @Override
  public String getCategory() {
    return NODE_JS_CATEGORY;
  }

  @Override
  public List<Provider<? extends WizardPage<MutableProjectConfig>>> getWizardPages() {
    return wizardPages;
  }
}

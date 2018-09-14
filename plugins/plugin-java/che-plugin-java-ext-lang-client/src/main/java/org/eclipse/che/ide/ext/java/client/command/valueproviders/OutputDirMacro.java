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
package org.eclipse.che.ide.ext.java.client.command.valueproviders;

import static org.eclipse.che.ide.ext.java.client.util.JavaUtil.isJavaProject;
import static org.eclipse.che.ide.ext.java.shared.Constants.OUTPUT_FOLDER;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.macro.Macro;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;

/**
 * Provides a path to the project's output directory.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class OutputDirMacro implements Macro {

  private static final String KEY = "${project.java.output.dir}";

  private final AppContext appContext;
  private final PromiseProvider promises;
  private final JavaLocalizationConstant localizationConstants;

  @Inject
  public OutputDirMacro(
      AppContext appContext,
      PromiseProvider promises,
      JavaLocalizationConstant localizationConstants) {
    this.appContext = appContext;
    this.promises = promises;
    this.localizationConstants = localizationConstants;
  }

  @Override
  public String getName() {
    return KEY;
  }

  @Override
  public String getDescription() {
    return localizationConstants.macroProjectJavaOutputDirDescription();
  }

  @Override
  public Promise<String> expand() {
    final Resource[] resources = appContext.getResources();

    if (resources != null && resources.length == 1) {

      final Resource resource = resources[0];
      Project project = resource.getProject();

      if (!project.exists() || !isJavaProject(project)) {
        return promises.resolve("");
      }

      if (project.getAttributes().containsKey(OUTPUT_FOLDER)) {
        return promises.resolve(
            appContext
                .getProjectsRoot()
                .append(project.getLocation())
                .append(project.getAttributes().get(OUTPUT_FOLDER).get(0))
                .toString());
      } else {
        return promises.resolve(
            appContext.getProjectsRoot().append(project.getLocation()).toString());
      }
    }

    return promises.resolve("");
  }
}

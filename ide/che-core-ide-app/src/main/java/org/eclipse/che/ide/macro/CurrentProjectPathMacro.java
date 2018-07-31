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
package org.eclipse.che.ide.macro;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.macro.Macro;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;

/**
 * Provides current project's path. Path means full absolute path to project on the FS, e.g.
 * /projects/project_name
 *
 * @author Artem Zatsarynnyi
 * @author Vlad Zhukovskyi
 */
@Singleton
public class CurrentProjectPathMacro implements Macro {

  private static final String KEY = "${current.project.path}";

  private final AppContext appContext;
  private final PromiseProvider promises;
  private final CoreLocalizationConstant localizationConstants;

  @Inject
  public CurrentProjectPathMacro(
      AppContext appContext,
      PromiseProvider promises,
      CoreLocalizationConstant localizationConstants) {
    this.appContext = appContext;
    this.promises = promises;
    this.localizationConstants = localizationConstants;
  }

  @NotNull
  @Override
  public String getName() {
    return KEY;
  }

  @Override
  public String getDescription() {
    return localizationConstants.macroCurrentProjectPathDescription();
  }

  @NotNull
  @Override
  public Promise<String> expand() {
    String value = "";

    Resource[] resources = appContext.getResources();

    if (resources != null && resources.length == 1) {
      Optional<Project> project = appContext.getResource().getRelatedProject();

      if (project.isPresent()) {
        value = appContext.getProjectsRoot().append(project.get().getLocation()).toString();
      }
    }

    return promises.resolve(value);
  }
}

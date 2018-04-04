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
package org.eclipse.che.ide.macro;

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
import org.eclipse.che.ide.resource.Path;

/**
 * Provides current project's parent path. Path means full absolute path to project's parent on the
 * FS, e.g. /projects/project_name
 */
@Singleton
public class CurrentProjectEldestParentPathMacro implements Macro {

  private static final String KEY = "${current.project.eldest.parent.path}";

  private final AppContext appContext;
  private final PromiseProvider promises;
  private final CoreLocalizationConstant localizationConstants;

  @Inject
  public CurrentProjectEldestParentPathMacro(
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
    return localizationConstants.macroCurrentProjectEldestParentPathDescription();
  }

  @NotNull
  @Override
  public Promise<String> expand() {
    String value = "";

    Resource[] resources = appContext.getResources();

    if (resources != null && resources.length == 1) {
      Project project = appContext.getResource().getProject();

      if (project != null) {
        Path location = project.getLocation();
        String eldestParent = location.segment(0);
        if (eldestParent != null) {
          value = appContext.getProjectsRoot().append(eldestParent).toString();
        }
      }
    }

    return promises.resolve(value);
  }
}

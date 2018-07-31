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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.macro.Macro;
import org.eclipse.che.ide.api.resources.Resource;

/**
 * Provides relative path to specific project. Path to project resolves from current workspace root.
 * e.g. /project_name.
 *
 * <p>Need for IDEX-3924 as intermediate solution.
 *
 * @author Vlad Zhukovskiy
 */
@Singleton
public class CurrentProjectRelativePathMacro implements Macro {

  private static final String KEY = "${current.project.relpath}";

  private final PromiseProvider promises;
  private final CoreLocalizationConstant localizationConstants;

  private AppContext appContext;

  @Inject
  public CurrentProjectRelativePathMacro(
      AppContext appContext,
      PromiseProvider promises,
      CoreLocalizationConstant localizationConstants) {
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
    return localizationConstants.macroCurrentProjectRelpathDescription();
  }

  @Override
  public Promise<String> expand() {
    final Resource[] resources = appContext.getResources();

    if (resources != null && resources.length == 1) {
      return promises.resolve(resources[0].getLocation().toString());
    }

    return promises.resolve("");
  }
}

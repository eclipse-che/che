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

import com.google.common.annotations.Beta;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.macro.Macro;

/**
 * Provider which is responsible for retrieving the workspace namespace.
 *
 * <p>Macro provided: <code>${workspace.namespace}</code>
 */
@Beta
@Singleton
public class WorkspaceNamespaceMacro implements Macro {

  public static final String KEY = "${workspace.namespace}";

  private final AppContext appContext;
  private final PromiseProvider promises;
  private final CoreLocalizationConstant localizationConstants;

  @Inject
  public WorkspaceNamespaceMacro(
      AppContext appContext,
      PromiseProvider promises,
      CoreLocalizationConstant localizationConstants) {
    this.appContext = appContext;
    this.promises = promises;
    this.localizationConstants = localizationConstants;
  }

  /** {@inheritDoc} */
  @Override
  public String getName() {
    return KEY;
  }

  @Override
  public String getDescription() {
    return localizationConstants.macroWorkspaceNamespaceDescription();
  }

  /** {@inheritDoc} */
  @Override
  public Promise<String> expand() {
    return promises.resolve(appContext.getWorkspace().getNamespace());
  }
}

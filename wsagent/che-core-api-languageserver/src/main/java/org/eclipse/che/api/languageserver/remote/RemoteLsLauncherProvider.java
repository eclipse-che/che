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
package org.eclipse.che.api.languageserver.remote;

import java.util.Set;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncher;

/**
 * Provides remote language server launcher instances constructed according to workspace
 * configuration. The launchers (depending on context) may in fact not physically launch the
 * language servers but establish a remote connection to already launched servers.
 */
public interface RemoteLsLauncherProvider {
  /**
   * Get all remote language server launchers that are mentioned in a workspace configuration.
   *
   * @param workspace workspace configuration
   * @return set of language server launchers
   */
  Set<LanguageServerLauncher> getAll(Workspace workspace);
}

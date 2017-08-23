/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.bootstrap;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;

/**
 * Represents strategy for initializing IDE application.
 *
 * @see DefaultIdeInitializationStrategy
 * @see FactoryIdeInitializationStrategy
 */
interface IdeInitializationStrategy {

  /** Performs the essential initialization routines of IDE application. */
  Promise<Void> init();

  /** Returns the opening workspace. */
  Promise<WorkspaceImpl> getWorkspaceToStart();
}

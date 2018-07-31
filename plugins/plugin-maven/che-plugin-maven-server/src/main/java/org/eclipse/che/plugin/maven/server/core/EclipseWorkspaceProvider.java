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
package org.eclipse.che.plugin.maven.server.core;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * Provider for {@link IWorkspace}
 *
 * @author Evgen Vidolob
 */
@Singleton
public class EclipseWorkspaceProvider implements Provider<IWorkspace> {
  @Override
  public IWorkspace get() {
    return ResourcesPlugin.getWorkspace();
  }
}

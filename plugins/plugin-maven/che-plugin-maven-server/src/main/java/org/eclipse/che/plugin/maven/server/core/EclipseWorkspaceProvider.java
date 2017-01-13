/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.maven.server.core;

import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * Provider for {@link IWorkspace}
 * @author Evgen Vidolob
 */
@Singleton
public class EclipseWorkspaceProvider implements Provider<IWorkspace> {
    @Override
    public IWorkspace get() {
        return ResourcesPlugin.getWorkspace();
    }
}

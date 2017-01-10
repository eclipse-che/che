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
package org.eclipse.che.ide.workspace;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.eclipse.che.ide.context.BrowserQueryFieldRenderer;

/**
 * Provides an appropriate implementation of {@link WorkspaceComponent} depending on URL parameters.
 * <p>May provide: {@link DefaultWorkspaceComponent} or {@link FactoryWorkspaceComponent}.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class WorkspaceComponentProvider implements Provider<WorkspaceComponent> {

    private final Provider<DefaultWorkspaceComponent> workspaceComponentProvider;
    private final Provider<FactoryWorkspaceComponent> factoryComponentProvider;
    private final BrowserQueryFieldRenderer           browserQueryFieldRenderer;

    @Inject
    public WorkspaceComponentProvider(Provider<DefaultWorkspaceComponent> workspaceComponentProvider,
                                      Provider<FactoryWorkspaceComponent> factoryComponentProvider,
                                      BrowserQueryFieldRenderer browserQueryFieldRenderer) {
        this.workspaceComponentProvider = workspaceComponentProvider;
        this.factoryComponentProvider = factoryComponentProvider;
        this.browserQueryFieldRenderer = browserQueryFieldRenderer;
    }

    @Override
    public WorkspaceComponent get() {
        final String factoryParams = browserQueryFieldRenderer.getParameterFromURLByName("factory");
        return factoryParams.isEmpty() ? workspaceComponentProvider.get() : factoryComponentProvider.get();
    }
}

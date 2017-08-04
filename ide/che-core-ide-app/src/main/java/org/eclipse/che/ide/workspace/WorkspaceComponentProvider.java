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

import org.eclipse.che.ide.context.QueryParameters;

/**
 * Provides an appropriate implementation of {@link WorkspaceComponent} depending on URL parameters.
 * <p>May provide: {@link DefaultWorkspaceComponent} or {@link FactoryWorkspaceComponent}.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class WorkspaceComponentProvider implements Provider<WorkspaceComponent> {

    private final Provider<DefaultWorkspaceComponent>       workspaceComponentProvider;
    private final Provider<FactoryWorkspaceComponent>       factoryComponentProvider;
    private final Provider<PredefinedWorkspaceComponent>    predefinedWorkspaceComponentProvider;
    private final QueryParameters                           queryParameters;

    @Inject
    public WorkspaceComponentProvider(Provider<DefaultWorkspaceComponent> workspaceComponentProvider,
                                      Provider<FactoryWorkspaceComponent> factoryComponentProvider,
                                      Provider<PredefinedWorkspaceComponent> predefinedWorkspaceComponentProvider,
                                      QueryParameters queryParameters) {
        this.workspaceComponentProvider = workspaceComponentProvider;
        this.factoryComponentProvider = factoryComponentProvider;
        this.predefinedWorkspaceComponentProvider = predefinedWorkspaceComponentProvider;
        this.queryParameters = queryParameters;
    }

    @Override
    public WorkspaceComponent get() {
        if (!queryParameters.getByName("factory").isEmpty()) {
            return factoryComponentProvider.get();
        }

        if (!queryParameters.getByName("init").isEmpty()) {
            return predefinedWorkspaceComponentProvider.get();
        }

        return workspaceComponentProvider.get();
    }

}

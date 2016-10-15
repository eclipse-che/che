/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.workspace.macro;

import com.google.common.annotations.Beta;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.macro.Macro;

/**
 * Provider which is responsible for retrieving the workspace name.
 *
 * Macro provided: <code>${workspace.name}</code>
 *
 * @author Vlad Zhukovskyi
 * @see Macro
 * @since 4.7.0
 */
@Beta
@Singleton
public class WorkspaceNameMacro implements Macro {

    public static final String KEY = "${workspace.name}";

    private final AppContext      appContext;
    private final PromiseProvider promises;
    private final CoreLocalizationConstant localizationConstants;

    @Inject
    public WorkspaceNameMacro(AppContext appContext, PromiseProvider promises, CoreLocalizationConstant localizationConstants) {
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
        return localizationConstants.macroWorkspaceNameDescription();
    }

    /** {@inheritDoc} */
    @Override
    public Promise<String> expand() {
        return promises.resolve(appContext.getWorkspaceName());
    }
}

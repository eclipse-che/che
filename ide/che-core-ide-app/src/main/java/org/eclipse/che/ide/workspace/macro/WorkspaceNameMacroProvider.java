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
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.CommandPropertyValueProvider;

/**
 * Provider which is responsible for retrieving the workspace name.
 *
 * Macro provided: <code>${workspace.name}</code>
 *
 * @author Vlad Zhukovskyi
 * @see CommandPropertyValueProvider
 * @since 4.7.0
 */
@Beta
@Singleton
public class WorkspaceNameMacroProvider implements CommandPropertyValueProvider {

    public static final String KEY = "${workspace.name}";

    private final AppContext      appContext;
    private final PromiseProvider promises;

    @Inject
    public WorkspaceNameMacroProvider(AppContext appContext, PromiseProvider promises) {
        this.appContext = appContext;
        this.promises = promises;
    }

    /** {@inheritDoc} */
    @Override
    public String getKey() {
        return KEY;
    }

    /** {@inheritDoc} */
    @Override
    public Promise<String> getValue() {
        return promises.resolve(appContext.getWorkspaceName());
    }
}

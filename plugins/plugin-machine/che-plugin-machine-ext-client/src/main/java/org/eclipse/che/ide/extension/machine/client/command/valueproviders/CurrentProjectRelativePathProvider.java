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
package org.eclipse.che.ide.extension.machine.client.command.valueproviders;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.app.AppContext;

/**
 * Provides relative path to specific project. Path to project resolves from current workspace root.
 * e.g. /project_name.
 *
 * Need for IDEX-3924 as intermediate solution.
 *
 * @author Vlad Zhukovskiy
 */
@Singleton
public class CurrentProjectRelativePathProvider implements CommandPropertyValueProvider {

    private static final String KEY = "${current.project.relpath}";

    private AppContext appContext;

    @Inject
    public CurrentProjectRelativePathProvider(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getValue() {
        return appContext.getCurrentProject().getProjectConfig().getPath().substring(1);
    }
}

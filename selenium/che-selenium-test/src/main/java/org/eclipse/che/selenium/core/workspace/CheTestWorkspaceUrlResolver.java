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
package org.eclipse.che.selenium.core.workspace;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.selenium.core.provider.TestIdeUrlProvider;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

/**
 * @author Anatolii Bazko
 */
@Singleton
public class CheTestWorkspaceUrlResolver implements TestWorkspaceUrlResolver {

    @Inject
    private TestIdeUrlProvider testIdeUrlProvider;

    @Override
    public URL resolve(TestWorkspace testWorkspace) throws MalformedURLException {
        try {
            return new URL(testIdeUrlProvider.get() + "che/" + testWorkspace.getName());
        } catch (ExecutionException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }
}

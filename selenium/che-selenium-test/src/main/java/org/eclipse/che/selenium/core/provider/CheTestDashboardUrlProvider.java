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
package org.eclipse.che.selenium.core.provider;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.inject.Named;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Anatolii Bazko
 */
@Singleton
public class CheTestDashboardUrlProvider implements TestDashboardUrlProvider {
    @Inject
    @Named("sys.protocol")
    private String protocol;
    @Inject
    @Named("sys.host")
    private String host;

    @Override
    public URL get() {
        try {
            return new URL(protocol, host, 8080, "/dashboard/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}

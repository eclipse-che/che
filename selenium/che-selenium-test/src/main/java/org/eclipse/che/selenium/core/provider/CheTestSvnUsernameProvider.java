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

/**
 * @author Dmytro Nochevnov
 */
@Singleton
public class CheTestSvnUsernameProvider implements TestSvnUsernameProvider {
    // Default password for svn server krisdavison/svn-server:v2.0
    @Inject(optional = true)
    @Named("svn.server.username")
    private String username = "user";

    @Override
    public String get() {
        return username;
    }
}

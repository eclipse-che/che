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
package org.eclipse.che.api.installer;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.installer.server.model.impl.BasicInstaller;
import org.eclipse.che.api.installer.shared.model.Installer;

import java.io.IOException;

/**
 * Language server Php installer.
 *
 * @author Anatolii Bazko
 * @see Installer
 */
@Singleton
public class LSPhpInstaller extends BasicInstaller {
    private static final String AGENT_DESCRIPTOR = "org.eclipse.che.ls.php.json";
    private static final String AGENT_SCRIPT     = "org.eclipse.che.ls.php.script.sh";

    @Inject
    public LSPhpInstaller() throws IOException {
        super(AGENT_DESCRIPTOR, AGENT_SCRIPT);
    }
}

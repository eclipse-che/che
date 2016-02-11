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
package org.eclipse.che.plugin.docker.machine.local.provider;

import org.eclipse.che.api.core.util.SystemInfo;
import org.eclipse.che.plugin.docker.machine.WindowsHostUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Provide path to the project folder on hosted machine
 *
 * <p>On Unix managed by che.user.workspaces.storage.<br>
 * On Windows MUST be locate in "user.home" directory in case limitation windows+docker
 *
 * @author Vitalii Parfonov
 * @author Alexander Garagatyi
 */
@Singleton
public class CheHostVfsRootDirProvider implements Provider<String> {

    private static final Logger LOG = LoggerFactory.getLogger(CheHostVfsRootDirProvider.class);

    @Inject
    @Named("che.user.workspaces.storage")
    private String fsRootDir;

    @Override
    public String get() {
        if (SystemInfo.isWindows()) {
            try {
                final Path cheHome = WindowsHostUtils.ensureCheHomeExist();
                final Path vfs = cheHome.resolve("vfs");
                vfs.toFile().mkdir();
                return vfs.toString();
            } catch (IOException e) {
                LOG.warn(e.getMessage());
                throw new RuntimeException(e);
            }
        } else {
            return fsRootDir;
        }
    }
}

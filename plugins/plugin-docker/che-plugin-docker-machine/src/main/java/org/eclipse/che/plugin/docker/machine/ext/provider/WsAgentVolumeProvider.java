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
package org.eclipse.che.plugin.docker.machine.ext.provider;

import org.eclipse.che.api.core.util.SystemInfo;
import org.eclipse.che.plugin.docker.machine.WindowsHostUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Reads path to extensions server archive to mount it to docker machine
 *
 * <p>On Windows hosts MUST be locate in "user.home" directory in case limitation windows+docker.
 *
 * @author Vitalii Parfonov
 * @author Alexander Garagatyi
 */
@Singleton
public class WsAgentVolumeProvider implements Provider<String> {

    private static final String CONTAINER_TARGET = ":/mnt/che/ws-agent.tar.gz";
    private static final String WS_AGENT         = "ws-agent.tar.gz";

    private static final Logger LOG = LoggerFactory.getLogger(WsAgentVolumeProvider.class);

    @Inject
    @Named("che.workspace.agent.dev")
    private String wsAgentArchivePath;

    @Inject
    @Named("che.docker.volumes_agent_options")
    private String volumeOptions;

    @Override
    public String get() {

        if (SystemInfo.isWindows()) {
            try {
                final Path cheHome = WindowsHostUtils.ensureCheHomeExist();
                final Path path = Files.copy(Paths.get(wsAgentArchivePath), cheHome.resolve(WS_AGENT), REPLACE_EXISTING);
                return getTargetOptions(path.toString());
            } catch (IOException e) {
                LOG.warn(e.getMessage());
                throw new RuntimeException(e);
            }
        } else {
            return getTargetOptions(wsAgentArchivePath);
        }
    }

    private String getTargetOptions(final String path) {
        return path + CONTAINER_TARGET + ":" + volumeOptions;
    }
}

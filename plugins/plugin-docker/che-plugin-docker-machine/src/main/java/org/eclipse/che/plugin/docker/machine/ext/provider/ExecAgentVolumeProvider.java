/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.docker.machine.ext.provider;

import com.google.common.base.Strings;

import org.eclipse.che.api.core.util.SystemInfo;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.plugin.docker.machine.WindowsHostUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Provides volumes configuration of machine for exec agent
 *
 * <p>On Windows MUST be locate in "user.home" directory in case limitation windows+docker.
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class ExecAgentVolumeProvider implements Provider<String> {

    private static final String CONTAINER_TARGET = ":/mnt/che/exec-agent";
    private static final String EXEC             = "exec";
    private static final Logger LOG              = LoggerFactory.getLogger(ExecAgentVolumeProvider.class);

    private final String execArchivePath;

    private final String agentVolumeOptions;

    @Inject
    public ExecAgentVolumeProvider(@Nullable @Named("che.docker.volumes_agent_options") String agentVolumeOptions,
                                   @Named("che.workspace.exec_linux_amd64") String execArchivePath) {
        if (!Strings.isNullOrEmpty(agentVolumeOptions)) {
            this.agentVolumeOptions = ":" + agentVolumeOptions;
        } else {
            this.agentVolumeOptions = "";
        }
        this.execArchivePath = execArchivePath;
    }

    @Override
    public String get() {
        if (SystemInfo.isWindows()) {
            try {
                final Path cheHome = WindowsHostUtils.ensureCheHomeExist();
                final Path execPath = cheHome.resolve(EXEC);
                IoUtil.copy(Paths.get(execArchivePath).toFile(), execPath.toFile(), null, true);
                return getTargetOptions(execPath.toString());
            } catch (IOException e) {
                LOG.warn(e.getMessage());
                throw new RuntimeException(e);
            }
        } else {
            return getTargetOptions(execArchivePath);
        }
    }

    private String getTargetOptions(final String path) {
        return path + CONTAINER_TARGET + agentVolumeOptions;
    }

}

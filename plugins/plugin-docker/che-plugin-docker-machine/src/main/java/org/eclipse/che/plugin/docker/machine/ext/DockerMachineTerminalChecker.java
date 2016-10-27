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
package org.eclipse.che.plugin.docker.machine.ext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;

import static java.nio.file.Files.notExists;

/**
 * Check that ext terminal zip is present in configuration specified place.
 *
 * @author Max Shaposhnik
 */
public class DockerMachineTerminalChecker {

    public static final String TERMINAL_ARCHIVE_LOCATION = "che.workspace.terminal_linux_amd64";

    private static final Logger LOG = LoggerFactory.getLogger(DockerMachineTerminalChecker.class);

    private final String terminalArchiveLocation;

    @Inject
    public DockerMachineTerminalChecker(@Named(TERMINAL_ARCHIVE_LOCATION) String terminalArchiveLocation) {
        this.terminalArchiveLocation = terminalArchiveLocation;
    }

    @PostConstruct
    public void start() {
        if (notExists(new File(terminalArchiveLocation).toPath())) {
            String msg = String.format("Terminal archive not found at %s", terminalArchiveLocation);
            LOG.error(msg);
            throw new RuntimeException(msg);
        }
    }
}

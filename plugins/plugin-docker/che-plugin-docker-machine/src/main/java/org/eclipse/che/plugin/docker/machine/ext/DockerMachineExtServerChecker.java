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
 * Check that ext service zip is present in configuration specified place.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com) on 9/23/15.
 *
 */
public class DockerMachineExtServerChecker {

    private static final Logger LOG = LoggerFactory.getLogger(DockerMachineExtServerChecker.class);

    public static final String EXT_SERVER_ARCHIVE_LOCATION = "che.workspace.agent.dev";

    private final String extServerArchiveLocation;


    @Inject
    public DockerMachineExtServerChecker(@Named(EXT_SERVER_ARCHIVE_LOCATION) String extServerArchiveLocation) {
        this.extServerArchiveLocation = extServerArchiveLocation;
    }

    @PostConstruct
    public void start() {
        if (notExists(new File(extServerArchiveLocation).toPath())) {
            String msg = String.format("Ext server archive not found at %s", extServerArchiveLocation);
            LOG.error(msg);
            throw new RuntimeException(msg);
        }
    }
}

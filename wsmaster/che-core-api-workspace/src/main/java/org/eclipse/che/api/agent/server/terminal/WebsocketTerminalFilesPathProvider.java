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
package org.eclipse.che.api.agent.server.terminal;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.inject.ConfigurationProperties;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

/**
 * Provides path to websocket terminal archive.
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class WebsocketTerminalFilesPathProvider {
    private static final String CONFIGURATION_PREFIX         = "machine.server.terminal.path_to_archive.";
    private static final String CONFIGURATION_PREFIX_PATTERN = "machine\\.server\\.terminal\\.path_to_archive\\..+";

    private Map<String, String> archivesPaths;

    @Inject
    public WebsocketTerminalFilesPathProvider(ConfigurationProperties configurationProperties) {
        archivesPaths = configurationProperties.getProperties(CONFIGURATION_PREFIX_PATTERN)
                                               .entrySet()
                                               .stream()
                                               .collect(toMap(entry -> entry.getKey().replaceFirst(CONFIGURATION_PREFIX, ""),
                                                              Map.Entry::getValue));
    }

    @Nullable
    public String getPath(String architecture) {
        return archivesPaths.get(architecture);
    }
}

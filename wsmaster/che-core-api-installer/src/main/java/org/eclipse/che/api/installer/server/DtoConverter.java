/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.installer.server;

import static java.util.stream.Collectors.toMap;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.installer.shared.dto.InstallerDto;
import org.eclipse.che.api.installer.shared.model.Installer;
import org.eclipse.che.api.workspace.shared.dto.ServerConfigDto;

/** @author Anatolii Bazko */
public class DtoConverter {

  public static InstallerDto asDto(Installer installer) {
    return newDto(InstallerDto.class)
        .withId(installer.getId())
        .withName(installer.getName())
        .withVersion(installer.getVersion())
        .withDescription(installer.getDescription())
        .withProperties(installer.getProperties())
        .withScript(installer.getScript())
        .withDependencies(installer.getDependencies())
        .withServers(
            installer
                .getServers()
                .entrySet()
                .stream()
                .collect(toMap(Map.Entry::getKey, entry -> asDto(entry.getValue()))));
  }

  /** Converts {@link ServerConfig} to {@link ServerConfigDto}. */
  public static ServerConfigDto asDto(ServerConfig serverConf) {
    return newDto(ServerConfigDto.class)
        .withPort(serverConf.getPort())
        .withProtocol(serverConf.getProtocol())
        .withPath(serverConf.getPath())
        .withAttributes(serverConf.getAttributes());
  }

  private DtoConverter() {}
}

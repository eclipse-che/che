/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.devfile.server;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

import org.eclipse.che.api.core.model.workspace.devfile.UserDevfile;
import org.eclipse.che.api.devfile.shared.dto.UserDevfileDto;
import org.eclipse.che.api.workspace.shared.dto.devfile.DevfileDto;

public class DtoConverter {
  public static UserDevfileDto asDto(UserDevfile userDevfiledevfile) {
    DevfileDto devfileDto =
        org.eclipse.che.api.workspace.server.DtoConverter.asDto(userDevfiledevfile);
    return newDto(UserDevfileDto.class)
        .withId(userDevfiledevfile.getId())
        .withApiVersion(userDevfiledevfile.getApiVersion())
        .withCommands(devfileDto.getCommands())
        .withComponents(devfileDto.getComponents())
        .withProjects(devfileDto.getProjects())
        .withAttributes(devfileDto.getAttributes())
        .withMetadata(devfileDto.getMetadata());
  }
}

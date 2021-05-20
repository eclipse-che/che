/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
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

/** Helps to convert to/from DTOs related to user devfile. */
public class DtoConverter {
  public static UserDevfileDto asDto(UserDevfile userDevfile) {
    DevfileDto devfileDto =
        org.eclipse.che.api.workspace.server.DtoConverter.asDto(userDevfile.getDevfile());
    return newDto(UserDevfileDto.class)
        .withId(userDevfile.getId())
        .withDevfile(devfileDto)
        .withNamespace(userDevfile.getNamespace())
        .withName(userDevfile.getName())
        .withDescription(userDevfile.getDescription());
  }
}

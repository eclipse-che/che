/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import javax.inject.Singleton;
import javax.ws.rs.HttpMethod;
import org.eclipse.che.api.core.rest.ServiceContext;
import org.eclipse.che.api.core.util.LinksHelper;
import org.eclipse.che.api.devfile.shared.Constants;
import org.eclipse.che.api.devfile.shared.dto.UserDevfileDto;

/** Helps to inject {@link DevfileService} related links. */
@Beta
@Singleton
public class DevfileServiceLinksInjector {
  public UserDevfileDto injectLinks(UserDevfileDto userDevfileDto, ServiceContext serviceContext) {
    return userDevfileDto.withLinks(
        ImmutableList.of(
            LinksHelper.createLink(
                HttpMethod.GET,
                serviceContext
                    .getBaseUriBuilder()
                    .clone()
                    .path(DevfileService.class)
                    .path(DevfileService.class, "getById")
                    .build(userDevfileDto.getId())
                    .toString(),
                null,
                APPLICATION_JSON,
                Constants.LINK_REL_SELF)));
  }
}

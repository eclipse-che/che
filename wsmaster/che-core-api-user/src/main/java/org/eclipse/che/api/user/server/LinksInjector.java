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
package org.eclipse.che.api.user.server;

import org.eclipse.che.api.core.rest.ServiceContext;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.core.util.LinksHelper;
import org.eclipse.che.api.user.shared.dto.UserDescriptor;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.UriBuilder;
import java.util.LinkedList;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.che.api.user.server.Constants.LINK_REL_GET_CURRENT_USER;
import static org.eclipse.che.api.user.server.Constants.LINK_REL_GET_CURRENT_USER_PROFILE;
import static org.eclipse.che.api.user.server.Constants.LINK_REL_GET_USER_BY_EMAIL;
import static org.eclipse.che.api.user.server.Constants.LINK_REL_GET_USER_BY_ID;
import static org.eclipse.che.api.user.server.Constants.LINK_REL_GET_USER_PROFILE_BY_ID;
import static org.eclipse.che.api.user.server.Constants.LINK_REL_REMOVE_USER_BY_ID;
import static org.eclipse.che.api.user.server.Constants.LINK_REL_UPDATE_PASSWORD;

/**
 * Helps to inject {@link UserService} related links.
 *
 * @author Anatoliy Bazko
 */
public final class LinksInjector {

    public static UserDescriptor injectLinks(UserDescriptor userDescriptor, ServiceContext serviceContext) {
        final Subject currentSubject = EnvironmentContext.getCurrent().getSubject();
        final UriBuilder uriBuilder = serviceContext.getBaseUriBuilder();

        final List<Link> links = new LinkedList<>();
        if (currentSubject.isMemberOf("user")) {
            links.add(LinksHelper.createLink(HttpMethod.GET,
                                             uriBuilder.clone().path(UserProfileService.class)
                                                       .path(UserProfileService.class, "getCurrent")
                                                       .build()
                                                       .toString(),
                                             null,
                                             APPLICATION_JSON,
                                             LINK_REL_GET_CURRENT_USER_PROFILE));
            links.add(LinksHelper.createLink(HttpMethod.GET,
                                             uriBuilder.clone().path(UserService.class)
                                                       .path(UserService.class, "getCurrent")
                                                       .build()
                                                       .toString(),
                                             null,
                                             APPLICATION_JSON,
                                             LINK_REL_GET_CURRENT_USER));
            links.add(LinksHelper.createLink(HttpMethod.POST,
                                             uriBuilder.clone().path(UserService.class)
                                                       .path(UserService.class, "updatePassword")
                                                       .build()
                                                       .toString(),
                                             APPLICATION_FORM_URLENCODED,
                                             null,
                                             LINK_REL_UPDATE_PASSWORD));
        }
        if (currentSubject.isMemberOf("system/admin") || currentSubject.isMemberOf("system/manager")) {
            links.add(LinksHelper.createLink(HttpMethod.GET,
                                             uriBuilder.clone().path(UserService.class)
                                                       .path(UserService.class, "getById")
                                                       .build(userDescriptor.getId())
                                                       .toString(),
                                             null,
                                             APPLICATION_JSON,
                                             LINK_REL_GET_USER_BY_ID));
            links.add(LinksHelper.createLink(HttpMethod.GET,
                                             uriBuilder.clone().path(UserProfileService.class)
                                                       .path(UserProfileService.class, "getById")
                                                       .build(userDescriptor.getId())
                                                       .toString(),
                                             null,
                                             APPLICATION_JSON,
                                             LINK_REL_GET_USER_PROFILE_BY_ID));
            if (userDescriptor.getEmail() != null) {
                links.add(LinksHelper.createLink(HttpMethod.GET,
                                                 uriBuilder.clone().path(UserService.class)
                                                           .path(UserService.class, "getByAlias")
                                                           .queryParam("email", userDescriptor.getEmail())
                                                           .build()
                                                           .toString(),
                                                 null,
                                                 APPLICATION_JSON,
                                                 LINK_REL_GET_USER_BY_EMAIL));
            }
        }
        if (currentSubject.isMemberOf("system/admin")) {
            links.add(LinksHelper.createLink(HttpMethod.DELETE,
                                             uriBuilder.clone().path(UserService.class)
                                                       .path(UserService.class, "remove")
                                                       .build(userDescriptor.getId())
                                                       .toString(),
                                             null,
                                             null,
                                             LINK_REL_REMOVE_USER_BY_ID));
        }

        return userDescriptor.withLinks(links);
    }

    private LinksInjector() {}
}

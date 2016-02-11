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
package org.eclipse.che.api.local;

import org.eclipse.che.api.auth.AuthenticationDao;
import org.eclipse.che.api.auth.AuthenticationException;
import org.eclipse.che.api.auth.shared.dto.Credentials;
import org.eclipse.che.api.auth.shared.dto.Token;
import org.eclipse.che.dto.server.DtoFactory;

import javax.inject.Singleton;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Singleton
public class LocalAuthenticationDaoImpl implements AuthenticationDao {
    @Override
    public Response login(Credentials credentials, Cookie tokenAccessCookie, UriInfo uriInfo) throws AuthenticationException {
        return Response.ok().entity((DtoFactory.getInstance().createDto(Token.class).withValue("123123"))).build();
    }

    @Override
    public Response logout(String token, Cookie tokenAccessCookie, UriInfo uriInfo) {
        return Response.ok().build();
    }
}

/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.auth;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.eclipse.che.api.auth.shared.dto.Credentials;

/** @author gazarenkov */
public interface AuthenticationDao {

  Response login(Credentials credentials, Cookie tokenAccessCookie, UriInfo uriInfo)
      throws AuthenticationException;

  Response logout(String token, Cookie tokenAccessCookie, UriInfo uriInfo);
}

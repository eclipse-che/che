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
package org.eclipse.che.multiuser.api.authentication.commons.token;

import javax.servlet.http.HttpServletRequest;

/** Allows to extract sso token from request. */
public interface RequestTokenExtractor {
  /**
   * Extract token from request.
   *
   * @param req - request object.
   * @return - token if it was found, null otherwise.
   */
  String getToken(HttpServletRequest req);
}

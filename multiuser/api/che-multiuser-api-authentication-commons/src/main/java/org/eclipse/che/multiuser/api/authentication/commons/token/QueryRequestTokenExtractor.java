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

/** @author Max Shaposhnik (mshaposh@redhat.com) */
public class QueryRequestTokenExtractor implements RequestTokenExtractor {
  @Override
  public String getToken(HttpServletRequest req) {
    String query = req.getQueryString();
    if (query != null) {
      int start = query.indexOf("&token=");
      if (start != -1 || query.startsWith("token=")) {
        int end = query.indexOf('&', start + 7);
        if (end == -1) {
          end = query.length();
        }
        if (end != start + 7) {
          return query.substring(start + 7, end);
        }
      }
    }
    return null;
  }
}

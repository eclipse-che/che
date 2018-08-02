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
package org.eclipse.che.multiuser.keycloak.token.provider.util;

import java.util.HashMap;
import java.util.Map;

public final class UrlHelper {

  private UrlHelper() {}

  public static Map<String, String> splitQuery(String query) {
    Map<String, String> queryPairs = new HashMap<String, String>();
    String[] pairs = query.split("&");
    for (String pair : pairs) {
      int delimiterIndex = pair.indexOf("=");
      queryPairs.put(pair.substring(0, delimiterIndex), pair.substring(delimiterIndex + 1));
    }
    return queryPairs;
  }
}

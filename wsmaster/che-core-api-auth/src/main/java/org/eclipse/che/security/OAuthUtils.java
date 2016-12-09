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

package org.eclipse.che.security;

import javax.ws.rs.core.UriInfo;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Utility class for oAuth methods.
 */
public class OAuthUtils {

    public static String getParameter(Map<String, List<String>> params, String name) {
        List<String> l = params.get(name);
        if (!(l == null || l.isEmpty())) {
            return l.get(0);
        }
        return null;
    }

    public static Map<String, List<String>> getRequestParameters(URL requestUrl) {
        String state = getState(requestUrl);
        Map<String, List<String>> params = new HashMap<>();
        if (!(state == null || state.isEmpty())) {
            String decodedState;
            try {
                decodedState = URLDecoder.decode(state, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // should never happen, UTF-8 supported.
                throw new RuntimeException(e.getMessage(), e);
            }

            for (String pair : decodedState.split("&")) {
                if (!pair.isEmpty()) {
                    String name;
                    String value;
                    int eq = pair.indexOf('=');
                    if (eq < 0) {
                        name = pair;
                        value = "";
                    } else {
                        name = pair.substring(0, eq);
                        value = pair.substring(eq + 1);
                    }

                    List<String> paramValues = params.get(name);
                    if (paramValues == null) {
                        paramValues = new ArrayList<>();
                        params.put(name, paramValues);
                    }
                    paramValues.add(value);
                }
            }
        }
        return params;
    }

    private static String getState(URL requestUrl) {
        final String query = requestUrl.getQuery();
        if (!isNullOrEmpty(query)) {
            int start = query.indexOf("state=");
            if (start < 0) {
                return null;
            }
            int end = query.indexOf('&', start);
            if (end < 0) {
                end = query.length();
            }
            return query.substring(start + 6, end);
        }
        return null;
    }

    public static URL getRequestUrl(UriInfo uriInfo) {
        try {
            return uriInfo.getRequestUri().toURL();
        } catch (MalformedURLException e) {
            // should never happen
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}

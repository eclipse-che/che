/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.commons.lang;

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

/** TODO replace this class with URLEncodedUtils */
public class UrlUtils {
    /**
     * Retrieve query parameters map from String representation of url
     *
     * @param url
     * @return - <code>Map</code> with parameters names as map keys and parameters values as map values
     * @throws UnsupportedEncodingException
     */
    public static Map<String, List<String>> getQueryParameters(URL url) throws UnsupportedEncodingException {
        Map<String, List<String>> params = new HashMap<>();
        String query = url.getQuery();
        if (query != null) {
            for (String param : query.split("&")) {
                String pair[] = param.split("=");
                String key = URLDecoder.decode(pair[0], "UTF-8");
                String value = null;
                if (pair.length > 1) {
                    value = URLDecoder.decode(pair[1], "UTF-8");
                }
                List<String> values = params.get(key);
                if (values == null) {
                    values = new ArrayList<>();
                    params.put(key, values);
                }
                values.add(value);
            }
        }
        return params;
    }

    /**
     * Get state query parameter from request url.
     *
     * @return state parameter encoded in application/x-www-form-urlencoded MIME format
     */
    public static String getState(URL requestUrl) {
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

    /**
     * Retrieve parameters map from state parameter.
     *
     * @param state
     *         state parameter encoded in application/x-www-form-urlencoded MIME format
     * @return - <code>Map</code> with parameters names as map keys and parameters values as map values
     */
    public static Map<String, List<String>> getQueryParametersFromState(String state) {
        Map<String, List<String>> params = new HashMap<>();
        if (isNullOrEmpty(state)) {
            return params;
        }
        try {
            for (String pair : URLDecoder.decode(state, "UTF-8").split("&")) {
                if (pair.isEmpty()) {
                    continue;
                }
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
                List<String> paramValues = params.computeIfAbsent(name, values -> new ArrayList<>());
                paramValues.add(value);
            }
        } catch (UnsupportedEncodingException ignored) {
            // should never happen, UTF-8 supported.
        }
        return params;
    }

    /**
     * Extract parameter value from given parameters map.
     *
     * @param parameters
     *         <code>Map</code> with parameters names as map keys and parameters values as map values
     * @param name
     *         name of the requested parameter
     * @return parameter value or null if it was not found in parameters map
     */
    public static String getParameter(Map<String, List<String>> parameters, String name) {
        List<String> l = parameters.get(name);
        if (!(l == null || l.isEmpty())) {
            return l.get(0);
        }
        return null;
    }

    /**
     * Extract URL from given URI information.
     */
    public static URL getRequestUrl(UriInfo uriInfo) {
        try {
            return uriInfo.getRequestUri().toURL();
        } catch (MalformedURLException e) {
            // should never happen
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private UrlUtils() {
    }
}

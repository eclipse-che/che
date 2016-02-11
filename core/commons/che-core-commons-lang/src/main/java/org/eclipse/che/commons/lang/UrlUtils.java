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
package org.eclipse.che.commons.lang;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private UrlUtils() {
    }
}

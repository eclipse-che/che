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
package org.eclipse.che.ide.rest;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;

/**
 * Extended {@link com.google.gwt.http.client.UrlBuilder} with constructor that consumes string url.
 *
 * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
 * @version $Id: Aug 29, 2011 evgen $
 */
public class UrlBuilder extends com.google.gwt.http.client.UrlBuilder {

  private JSONObject o;

  /** */
  public UrlBuilder() {}

  /**
   * Parse url and set initial parameters(protocol, host, port, path)<br>
   *
   * @param url
   */
  public UrlBuilder(String url) {
    JavaScriptObject jso = parseUrl(url);
    o = new JSONObject(jso);
    setHost(o.get("host").isString().stringValue());
    setProtocol(o.get("protocol").isString().stringValue());
    if (o.containsKey("port")) {
      final String port = o.get("port").isString().stringValue();
      if (!port.isEmpty()) {
        setPort(Integer.valueOf(port));
      }
    }
    setPath(o.get("path").isString().stringValue());
    // fill query parameters
    JSONObject query = o.get("queryKey").isObject();
    for (String key : query.keySet()) {
      setParameter(key, query.get(key).isString().stringValue());
    }
  }

  public String getHost() {
    return o.get("host").toString();
  }

  public String getProtocol() {
    return o.get("protocol").toString();
  }

  public String getPort() {
    return o.get("port").toString();
  }

  public String getPath() {
    return o.get("path").toString();
  }

  public boolean containsPort() {
    return getPort() != null && !getPort().isEmpty();
  }

  public String getUrl() {
    return buildString();
  }

  private native JavaScriptObject parseUrl(String url) /*-{

        options = {
            strictMode: false,
            key: [ "source", "protocol", "authority", "userInfo", "user",
                "password", "host", "port", "relative", "path",
                "directory", "file", "query", "anchor" ],
            q: {
                name: "queryKey",
                parser: /(?:^|&)([^&=]*)=?([^&]*)/g
            },
            parser: {
                strict: /^(?:([^:\/?#]+):)?(?:\/\/((?:(([^:@]*)(?::([^:@]*))?)?@)?([^:\/?#]*)(?::(\d*))?))?((((?:[^?#\/]*\/)*)([^?#]*))(?:\?([^#]*))?(?:#(.*))?)/,
                loose: /^(?:(?![^:@]+:[^:@\/]*@)([^:\/?#.]+):)?(?:\/\/)?((?:(([^:@]*)(?::([^:@]*))?)?@)?([^:\/?#]*)(?::(\d*))?)(((\/(?:[^?#](?![^?#\/]*\.[^?#\/.]+(?:[?#]|$)))*\/?)?([^?#\/]*))(?:\?([^#]*))?(?:#(.*))?)/
            }
        }
        var o = options, m = o.parser[o.strictMode ? "strict" : "loose"]
            .exec(url), uri = {}, i = 14;

        while (i--)
            uri[o.key[i]] = m[i] || "";

        uri[o.q.name] = {};
        uri[o.key[12]].replace(o.q.parser, function ($0, $1, $2) {
            if ($1)
                uri[o.q.name][$1] = $2;
        });
        return uri;
    }-*/;
}

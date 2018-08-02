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
package org.eclipse.che.ide.websocket.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import javax.inject.Singleton;

/**
 * Defines the mepping between high level identifiers that are used be upper services (e.g. json
 * rpc) and low level identifier (which is a URL) for internal web socket components.
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class UrlResolver {
  private final Map<String, String> idMapping = new HashMap<>();

  /**
   * Sets a mapping for a specified id
   *
   * @param id id to be mapped
   * @param url url mapping
   */
  public void setMapping(String id, String url) {
    idMapping.put(id, url);
  }

  /**
   * Resolve URL, provide identifier that is mapped to specified URL.
   *
   * @param url low leve identifier
   * @return high level identifier
   */
  public String resolve(String url) {
    for (Entry<String, String> entry : idMapping.entrySet()) {
      if (Objects.equals(entry.getValue(), url)) {
        return entry.getKey();
      }
    }

    return null;
  }

  /**
   * Gets a URL by high level identifier
   *
   * @param id identifier
   * @return URL
   */
  public String getUrl(String id) {
    return idMapping.get(id);
  }

  /**
   * Remove a mepping from resolver's registry
   *
   * @param id identifier of the mapping
   * @return removed URL mapped to specified id
   */
  public String removeMapping(String id) {
    return idMapping.remove(id);
  }
}

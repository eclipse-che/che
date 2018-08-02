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
package org.eclipse.che.ide.api.workspace.model;

import static com.google.common.base.Strings.nullToEmpty;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;

public class ServerImpl implements Server {

  private String name;
  private String url;
  private ServerStatus status;
  private Map<String, String> attributes;

  public ServerImpl(String name, Server server) {
    this.name = name;
    this.url = nullToEmpty(server.getUrl()); // some servers doesn't have URL
    this.status = server.getStatus();
    if (server.getAttributes() != null) {
      this.attributes = new HashMap<>(server.getAttributes());
    } else {
      this.attributes = new HashMap<>();
    }
  }

  public String getName() {
    return name;
  }

  @Override
  public String getUrl() {
    return url;
  }

  @Override
  public ServerStatus getStatus() {
    return this.status;
  }

  @Override
  public Map<String, String> getAttributes() {
    return attributes;
  }
}

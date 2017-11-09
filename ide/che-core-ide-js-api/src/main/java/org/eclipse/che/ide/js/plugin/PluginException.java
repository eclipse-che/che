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

package org.eclipse.che.ide.js.plugin;

/** @author Yevhen Vydolob */
public class PluginException extends RuntimeException {

  private String pluginId;

  public PluginException(String message, String pluginId) {
    super(message);
    this.pluginId = pluginId;
  }

  public PluginException(String message, String pluginId, Throwable cause) {
    super(message, cause);
    this.pluginId = pluginId;
  }

  public PluginException() {}

  public String getPluginId() {
    return pluginId;
  }

  @Override
  public String getMessage() {
    String message = super.getMessage();
    if (message == null) {
      message = "";
    }
    return message += "{Plugin: " + pluginId + "}";
  }
}

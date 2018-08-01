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
package org.eclipse.che.plugin.yaml.shared;

/**
 * Establishes the format for Yaml Preferences
 *
 * @author Joshua Pinkney
 */
public class YamlPreference {

  private String url;
  private String glob;

  public YamlPreference(String url, String glob) {
    this.url = url;
    this.glob = glob;
  }

  public String getUrl() {
    return this.url;
  }

  public String getGlob() {
    return this.glob;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public void setGlob(String glob) {
    this.glob = glob;
  }
}

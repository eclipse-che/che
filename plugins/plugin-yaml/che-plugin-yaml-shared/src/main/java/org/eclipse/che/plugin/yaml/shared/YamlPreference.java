/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

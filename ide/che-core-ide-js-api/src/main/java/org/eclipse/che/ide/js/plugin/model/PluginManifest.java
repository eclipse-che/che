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

package org.eclipse.che.ide.js.plugin.model;

import java.util.List;

/** @author Yevhen Vydolob */
public class PluginManifest {

  private final String name;
  private final String publisher;
  private final String version;
  private final String displayName;
  private final String description;
  private final String main;
  private final List<String> pluginDependencies;
  private final PluginContributions contributions;

  public PluginManifest(
      String name,
      String publisher,
      String version,
      String displayName,
      String description,
      String main,
      List<String> pluginDependencies,
      PluginContributions contributions) {
    this.name = name;
    this.publisher = publisher;
    this.version = version;
    this.displayName = displayName;
    this.description = description;
    this.main = main;
    this.pluginDependencies = pluginDependencies;
    this.contributions = contributions;
  }

  public String getName() {
    return name;
  }

  public String getPublisher() {
    return publisher;
  }

  public String getVersion() {
    return version;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getDescription() {
    return description;
  }

  public String getMain() {
    return main;
  }

  public List<String> getPluginDependencies() {
    return pluginDependencies;
  }

  public PluginContributions getContributions() {
    return contributions;
  }

  public String getPluginId() {
    return publisher + "." + name;
  }
}

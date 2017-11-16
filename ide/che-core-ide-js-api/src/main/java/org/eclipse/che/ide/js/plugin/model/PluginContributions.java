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

import elemental.json.JsonArray;

/** @author Yevhen Vydolob */
public class PluginContributions {

  private final JsonArray actions;
  private final JsonArray images;
  private final JsonArray themes;

  public PluginContributions(JsonArray actions, JsonArray images, JsonArray themes) {
    this.actions = actions;
    this.images = images;
    this.themes = themes;
  }

  public JsonArray getActions() {
    return actions;
  }

  public JsonArray getImages() {
    return images;
  }

  public JsonArray getThemes() {
    return themes;
  }
}

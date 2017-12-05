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

package org.eclipse.che.ide.js.impl.resources;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Image;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Singleton;
import org.eclipse.che.ide.js.api.Disposable;
import org.eclipse.che.ide.js.api.context.PluginContext;
import org.eclipse.che.ide.js.api.resources.ImageElementFactory;
import org.eclipse.che.ide.js.api.resources.ImageRegistry;
import org.eclipse.che.ide.js.plugin.model.PluginManifest;

/** @author Yevhen Vydolob */
@Singleton
public class ImageRegistryImpl implements ImageRegistry {

  private Map<String, ImageFactory> imageFactories = new HashMap<>();

  @Override
  public Disposable registerUrl(String id, String url) {
    imageFactories.put(id, () -> new Image(url).getElement());
    return () -> imageFactories.remove(id);
  }

  @Override
  public Disposable registerHtml(String id, String html) {
    imageFactories.put(
        id,
        () -> {
          // TODO we should use elemental2 DOM library to get access to <template> element
          Element template = Document.get().createElement("div");
          template.setInnerHTML(html);
          return template.getFirstChildElement();
        });
    return () -> imageFactories.remove(id);
  }

  @Override
  public Disposable registerFactory(String id, ImageElementFactory factory) {
    imageFactories.put(id, factory::create);
    return () -> imageFactories.remove(id);
  }

  @Override
  public Element getImage(String id) {
    if (imageFactories.containsKey(id)) {
      return imageFactories.get(id).create();
    }
    return null;
  }

  public void registerPluginImages(
      List<PluginManifest> plugins, Map<String, PluginContext> activePlugins) {
    for (PluginManifest plugin : plugins) {
      if (plugin.getContributions().getImages() != null) {
        PluginContext context = activePlugins.get(plugin.getPluginId());
        handlePluginImages(plugin.getContributions().getImages(), context);
      }
    }
  }

  private void handlePluginImages(JsonArray images, PluginContext context) {
    for (int i = 0; i < images.length(); i++) {
      JsonObject imageObject = images.getObject(i);
      if (imageObject.hasKey("html")) {
        context.addDisposable(
            registerHtml(imageObject.getString("id"), imageObject.getString("html")));

      } else if (imageObject.hasKey("url")) {
        context.addDisposable(
            registerUrl(imageObject.getString("id"), imageObject.getString("url")));
      }
    }
  }

  interface ImageFactory {

    Element create();
  }
}

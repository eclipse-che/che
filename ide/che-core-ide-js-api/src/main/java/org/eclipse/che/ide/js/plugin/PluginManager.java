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

import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.user.client.rpc.AsyncCallback;
import elemental.json.JsonArray;
import elemental.json.JsonFactory;
import elemental.json.JsonObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.js.api.JsApi;
import org.eclipse.che.ide.js.impl.action.JsActionManager;
import org.eclipse.che.ide.js.impl.resources.ImageRegistryImpl;
import org.eclipse.che.ide.js.plugin.model.PluginContributions;
import org.eclipse.che.ide.js.plugin.model.PluginManifest;
import org.eclipse.che.requirejs.RequireJsLoader;

/** @author Yevhen Vydolob */
@Singleton
public class PluginManager {

  private final PluginServiceClient client;
  private final PromiseProvider promiseProvider;
  private final JsonFactory jsonFactory;
  private final RequireJsLoader requireJs;
  private final AppContext appContext;
  private final JsActionManager jsActionManager;
  private final ImageRegistryImpl imageRegistry;
  private final JsApi jsApi;
  private final List<PluginManifest> plugins = new ArrayList<>();

  @Inject
  public PluginManager(
      PluginServiceClient client,
      PromiseProvider promiseProvider,
      JsonFactory jsonFactory,
      RequireJsLoader requireJs,
      AppContext appContext,
      JsActionManager jsActionManager,
      ImageRegistryImpl imageRegistry,
      JsApi jsApi) {
    this.client = client;
    this.promiseProvider = promiseProvider;
    this.jsonFactory = jsonFactory;
    this.requireJs = requireJs;
    this.appContext = appContext;
    this.jsActionManager = jsActionManager;
    this.imageRegistry = imageRegistry;
    this.jsApi = jsApi;
  }

  public Promise<Void> loadPlugins() {

    return promiseProvider.create(
        callback -> {
          Promise<List<String>> plugins = client.getPlugins();
          plugins
              .then(
                  arg -> {
                    handlePluginMeta(callback, arg);
                  })
              .catchError(
                  arg -> {
                    callback.onFailure(arg.getCause());
                  });
        });
  }

  private void handlePluginMeta(AsyncCallback<Void> callback, List<String> arg) {
    try {
      parsePluginMeta(arg);
      imageRegistry.registerPluginImages(plugins);
      jsActionManager.registerPluginActions(plugins);
      doLoadPlugins(callback);
    } catch (PluginException e) {
      callback.onFailure(e);
    }
  }

  private void doLoadPlugins(AsyncCallback<Void> callback) {
    doLoadPlugin(callback, plugins.iterator());
  }

  private void doLoadPlugin(AsyncCallback<Void> callback, Iterator<PluginManifest> iterator) {
    if (iterator.hasNext()) {
      PluginManifest pluginManifest = iterator.next();
      //      RequirejsConfig config = RequirejsConfig.create();
      String baseUrl = appContext.getMasterApiEndpoint() + "/plugin/";
      ScriptInjector.fromUrl(
              baseUrl
                  + pluginManifest.getPublisher()
                  + "."
                  + pluginManifest.getName()
                  + "-"
                  + pluginManifest.getVersion()
                  + "/"
                  + pluginManifest.getMain())
          .setWindow(ScriptInjector.TOP_WINDOW)
          .inject();
      doLoadPlugin(callback, iterator);
      //      config.setBaseUrl(baseUrl);
      //      requireJs.require(
      //          modules ->
      //              requireJs.require(
      //                  namedModules -> {
      //                    for (int i = 0; i < namedModules.length(); i++) {
      //                      RequirejsModule module = namedModules.get(i);
      //                      try {
      //                        Plugin.of(module).activate(jsApi);
      //                      } catch (Throwable throwable) {
      //                        Log.error(getClass(), throwable);
      //                      }
      //                    }
      //
      //                    doLoadPlugin(callback, iterator);
      //                  },
      //                  error -> {
      //                    Log.error(getClass(), error);
      //                    callback.onFailure(new
      // PluginException(error.getMessage(),pluginManifest.getPluginId()));
      //                  },
      //                  config,
      //                  new String[] {pluginManifest.getPluginId()},
      //                  new String[] {pluginManifest.getPluginId()}),
      //          error -> {
      //            Log.error(getClass(), error);
      //            callback.onFailure(new
      // PluginException(error.getMessage(),pluginManifest.getPluginId()));
      //          },
      //          config,
      //          new String[] {
      //            pluginManifest.getPublisher()
      //                + "."
      //                + pluginManifest.getName()
      //                + "-"
      //                + pluginManifest.getVersion()
      //                + "/"
      //                + pluginManifest.getMain().substring(0,
      // pluginManifest.getMain().lastIndexOf("."))
      //          },
      //          new String[] {pluginManifest.getPublisher() + "." + pluginManifest.getName()});
    } else {
      callback.onSuccess(null);
    }
  }

  private void parsePluginMeta(List<String> pluginList) {
    for (String meta : pluginList) {
      JsonObject parse = jsonFactory.parse(meta);
      String name = parse.getString("name");
      String publisher = parse.getString("publisher");
      String version = parse.getString("version");
      String displayName = parse.getString("displayName");
      String description = parse.getString("description");
      String main = parse.getString("main");
      List<String> pluginDependencies = getStringList(parse.getArray("pluginDependencies"));
      JsonObject contributes = parse.getObject("contributes");
      JsonArray actions;
      if (contributes.hasKey("actions")) {
        actions = contributes.getArray("actions");
      } else {
        actions = jsonFactory.createArray();
      }
      JsonArray images;
      if (contributes.hasKey("images")) {
        images = contributes.getArray("images");
      } else {
        images = jsonFactory.createArray();
      }
      PluginContributions contributions = new PluginContributions(actions, images);
      plugins.add(
          new PluginManifest(
              name,
              publisher,
              version,
              displayName,
              description,
              main,
              pluginDependencies,
              contributions));
    }
  }

  private List<String> getStringList(JsonArray jsonArray) {
    List<String> result = new ArrayList<>(jsonArray.length());
    for (int i = 0; i < jsonArray.length(); i++) {
      result.add(jsonArray.getString(i));
    }

    return result;
  }
}

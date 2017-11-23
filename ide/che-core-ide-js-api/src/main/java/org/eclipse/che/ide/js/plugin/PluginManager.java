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

import com.google.gwt.user.client.rpc.AsyncCallback;
import elemental.js.util.JsArrayOf;
import elemental.json.JsonArray;
import elemental.json.JsonFactory;
import elemental.json.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.theme.ThemeAgent;
import org.eclipse.che.ide.js.api.Disposable;
import org.eclipse.che.ide.js.api.JsApi;
import org.eclipse.che.ide.js.api.context.PluginContext;
import org.eclipse.che.ide.js.impl.action.JsActionManager;
import org.eclipse.che.ide.js.impl.resources.ImageRegistryImpl;
import org.eclipse.che.ide.js.plugin.model.PluginContributions;
import org.eclipse.che.ide.js.plugin.model.PluginEntryPoint;
import org.eclipse.che.ide.js.plugin.model.PluginManifest;
import org.eclipse.che.ide.js.plugin.model.theme.JsThemeProvider;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.requirejs.RequireJsLoader;
import org.eclipse.che.requirejs.RequirejsModule;
import org.eclipse.che.requirejs.conf.RequirejsConfig;

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
  private final ThemeAgent themeAgent;
  private final AsyncRequestFactory asyncRequestFactory;
  private final JsApi jsApi;
  private final List<PluginManifest> plugins = new ArrayList<>();
  private final Map<String, PluginContext> activePlugins = new HashMap<>();

  @Inject
  public PluginManager(
      PluginServiceClient client,
      PromiseProvider promiseProvider,
      JsonFactory jsonFactory,
      RequireJsLoader requireJs,
      AppContext appContext,
      JsActionManager jsActionManager,
      ImageRegistryImpl imageRegistry,
      ThemeAgent themeAgent,
      AsyncRequestFactory asyncRequestFactory,
      JsApi jsApi) {
    this.client = client;
    this.promiseProvider = promiseProvider;
    this.jsonFactory = jsonFactory;
    this.requireJs = requireJs;
    this.appContext = appContext;
    this.jsActionManager = jsActionManager;
    this.imageRegistry = imageRegistry;
    this.themeAgent = themeAgent;
    this.asyncRequestFactory = asyncRequestFactory;
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
      imageRegistry.registerPluginImages(plugins, activePlugins);
      jsActionManager.registerPluginActions(plugins, activePlugins);
      handleTheme(plugins);
      callback.onSuccess(null);
    } catch (PluginException e) {
      callback.onFailure(e);
    }
  }

  private void handleTheme(List<PluginManifest> plugins) {
    for (PluginManifest plugin : plugins) {
      JsonArray themes = plugin.getContributions().getThemes();
      for (int i = 0; i < themes.length(); i++) {
        JsonObject themeObject = themes.getObject(i);
        String id = themeObject.getString("id");
        String description = themeObject.getString("description");
        String path = themeObject.getString("path");
        String baseUrl = getPluginBaseUrl(plugin);
        JsThemeProvider themeProvider =
            new JsThemeProvider(
                id, description, baseUrl + "/" + path, promiseProvider, asyncRequestFactory);
        themeAgent.addTheme(themeProvider);
      }
    }
  }

  private void doLoadPlugins(AsyncCallback<Void> callback) {
    doLoadPlugin(callback, plugins.iterator());
  }

  private void doLoadPlugin(AsyncCallback<Void> callback, Iterator<PluginManifest> iterator) {
    if (iterator.hasNext()) {
      PluginManifest pluginManifest = iterator.next();
      RequirejsConfig config = RequirejsConfig.create();
//      String baseUrl = getPluginBaseUrl(pluginManifest);
//      config.setBaseUrl(baseUrl);
      requireJs.require(
          modules -> {
            // Log.error(getClass(), modules);
            for (int i = 0; i < modules.length(); i++) {
              RequirejsModule module = modules.get(i);
              try {
                PluginContext context = activePlugins.get(pluginManifest.getPluginId());
                PluginEntryPoint.of(module).activate(context);
              } catch (Throwable throwable) {
                Log.error(getClass(), throwable);
                callback.onFailure(
                    new PluginException(
                        "Can't initialize plugin", pluginManifest.getPluginId(), throwable));
              }
            }

            doLoadPlugin(callback, iterator);
          },
          error -> {
            Log.error(getClass(), error);
            doLoadPlugin(callback, iterator);
          },
          config,
          new String[] {
              appContext.getMasterApiEndpoint()
                  + "/plugin/"
                  + pluginManifest.getPublisher()
                  + "."
                  + pluginManifest.getName()
                  + "-"
                  + pluginManifest.getVersion() + "/" + pluginManifest.getMain()/*.substring(0, pluginManifest.getMain().lastIndexOf("."))*/
          },
          new String[0]);
    } else {
      callback.onSuccess(null);
    }
  }

  private String getPluginBaseUrl(PluginManifest pluginManifest) {
    return appContext.getMasterApiEndpoint()
        + "/plugin/"
        + pluginManifest.getPublisher()
        + "."
        + pluginManifest.getName()
        + "-"
        + pluginManifest.getVersion();
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
      JsonArray themes;
      if (contributes.hasKey("themes")) {
        themes = contributes.getArray("themes");
      } else {
        themes = jsonFactory.createArray();
      }
      PluginContributions contributions = new PluginContributions(actions, images, themes);
      PluginManifest manifest =
          new PluginManifest(
              name,
              publisher,
              version,
              displayName,
              description,
              main,
              pluginDependencies,
              contributions);
      plugins.add(manifest);
      activePlugins.put(manifest.getPluginId(), new PluginContext(jsApi));
    }
  }

  private List<String> getStringList(JsonArray jsonArray) {
    List<String> result = new ArrayList<>(jsonArray.length());
    for (int i = 0; i < jsonArray.length(); i++) {
      result.add(jsonArray.getString(i));
    }

    return result;
  }

  public void disablePlugin() {
    activePlugins.forEach(
        (s, pluginContext) -> {
          JsArrayOf<Disposable> disposables = pluginContext.getDisposables();
          for (int i = 0; i < disposables.length(); i++) {
            disposables.get(i).dispose();
          }
        });
  }

  public Promise<Void> initPlugins() {
    return promiseProvider.create(this::doLoadPlugins);
  }
}

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
package org.eclipse.che.requirejs;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.ScriptInjector;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.requirejs.conf.RequirejsConfig;

/**
 * Loads javascript modules with requirejs.
 *
 * @author "Mickaël Leduque"
 */
@Singleton
public class RequireJsLoader {

  private static boolean requirejsLoaded = false;
  private static boolean requirejsLoadFailed = false;

  private ModuleHolder moduleHolder;

  @Inject
  public RequireJsLoader(final ModuleHolder moduleHolder) {
    this.moduleHolder = moduleHolder;
  }

  public void require(
      final Callback<JavaScriptObject[], Throwable> callback,
      final String[] requiredScripts,
      final String[] moduleKeys) {
    // require with default config
    final RequirejsConfig defaultConfig = RequirejsConfig.create();
    /** Using GWT.getModuleBaseForStaticFiles() blocks CodeMirror to run under Super Dev Mode */
    defaultConfig.setBaseUrl(GWT.getModuleBaseURL());
    defaultConfig.setWaitSeconds(0);

    require(
        new RequirejsCallback() {

          @Override
          public void onReady(final JsArray<RequirejsModule> modules) {
            final JavaScriptObject[] result = new JavaScriptObject[modules.length()];
            for (int i = 0; i < modules.length(); i++) {
              result[i] = modules.get(i);
            }
            callback.onSuccess(result);
          }
        },
        new RequirejsErrorHandler() {

          @Override
          public void onError(final RequireError error) {
            callback.onFailure(new JavaScriptException(error));
          }
        },
        defaultConfig,
        requiredScripts,
        moduleKeys);
  }

  public void require(
      final RequirejsCallback callback,
      final RequirejsErrorHandler errorHandler,
      final RequirejsConfig config,
      final String[] requiredScripts,
      final String[] moduleKeys) {
    if (!requirejsLoaded && !requirejsLoadFailed) {
      Log.debug(RequireJsLoader.class, "Loading require.js.");
      /** Using GWT.getModuleBaseForStaticFiles() blocks CodeMirror to run under Super Dev Mode */
      ScriptInjector.fromUrl(GWT.getModuleBaseURL() + "require.js")
          .setWindow(ScriptInjector.TOP_WINDOW)
          .setCallback(
              new Callback<Void, Exception>() {
                @Override
                public void onSuccess(final Void result) {
                  Log.debug(RequireJsLoader.class, "require.js loaded.");
                  configureGlobalErrorCallback();
                  requireScripts(callback, errorHandler, config, requiredScripts, moduleKeys);
                  requirejsLoaded = true;
                }

                @Override
                public void onFailure(final Exception e) {
                  Log.error(RequireJsLoader.class, "Unable to load require.js", e);
                  requirejsLoadFailed = true;
                }
              })
          .inject();
    } else if (!requirejsLoadFailed) {
      requireScripts(callback, null, config, requiredScripts, moduleKeys);
    } else {
      Log.error(RequireJsLoader.class, "Require.js load failed, cannot require scripts.");
    }
  }

  private void requireScripts(
      final RequirejsCallback callback,
      final RequirejsErrorHandler errorHandler,
      final RequirejsConfig config,
      final String[] requiredScripts,
      final String[] moduleKeys) {
    final JsArrayString jsReqScripts = (JsArrayString) JavaScriptObject.createArray();
    for (final String script : requiredScripts) {
      jsReqScripts.push(script);
    }

    Requirejs.config(config)
        .require(
            jsReqScripts,
            new RequirejsCallback() {

              @Override
              public void onReady(final JsArray<RequirejsModule> result) {
                for (int i = 0; i < Math.min(result.length(), moduleKeys.length); i++) {
                  String itemtoString = "null";
                  if (result.get(i) != null) {
                    itemtoString = result.get(i).toString();
                  }
                  if (itemtoString.length() > 30) {
                    itemtoString = itemtoString.substring(0, 27) + "...";
                  }
                  Log.debug(
                      RequireJsLoader.class,
                      "Add module reference - name=" + moduleKeys[i] + " object=" + itemtoString);
                  moduleHolder.setModule(moduleKeys[i], result.get(i));
                }
                callback.onReady(result);
              }
            },
            errorHandler);
  }

  protected static void configureGlobalErrorCallback() {
    Requirejs.get()
        .setOnError(
            new RequirejsErrorHandler() {

              @Override
              public void onError(final RequireError err) {
                final String type = err.getRequireType();
                if ("scripterror".equals(type)) {
                  // leave the module as-is
                  final JsArrayString modules = err.getRequireModules();
                  if (modules != null && modules.length() > 0) {
                    final String failed = modules.get(0);
                    String formattedMsg = "";
                    if (err.getMessage() != null) {
                      formattedMsg = formattedMsg.replace("\n", "\n\t\t");
                    }
                    consoleWarn(
                        "Required module '%s' load failed with script error "
                            + "(nonexistant script or error in the loaded script)\n"
                            + "\t- error message = '%s'\n"
                            + "\t- original error = %o",
                        failed, formattedMsg, err);
                  } else {
                    consoleWarn(
                        "Unexpected requirejs of type 'scripterror' without requireModules property: %o",
                        err);
                    throw new RuntimeException(err.toString());
                  }

                } else if ("timeout".equals(type)) {
                  // we'll retry next time
                  final JsArrayString modules = err.getRequireModules();
                  if (modules != null && modules.length() > 0) {
                    final String failed = modules.get(0);
                    consoleWarn("Required module '%s' load failed on timeout.", failed);
                    Requirejs.get().undef(failed);
                  } else {
                    consoleWarn(
                        "Unexpected requirejs of type 'timeout' without requireModules property: %o",
                        err);
                    throw new RuntimeException(err.toString());
                  }

                } else {
                  throw new RuntimeException(err.toString());
                }
              }
            });
  }

  private static final native void consoleWarn(String base, Object param1) /*-{
        $wnd.console.warn(base, param1);
    }-*/;

  private static final native void consoleWarn(
      String base, Object param1, Object param2, Object param3) /*-{
        $wnd.console.warn(base, param1, param2, param3);
    }-*/;

  public void require(
      final Callback<JavaScriptObject[], Throwable> callback, final String[] requiredScripts) {
    require(callback, requiredScripts, new String[0]);
  }
}

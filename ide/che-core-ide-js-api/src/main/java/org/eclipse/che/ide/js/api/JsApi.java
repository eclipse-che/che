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

package org.eclipse.che.ide.js.api;

import javax.inject.Inject;
import javax.inject.Singleton;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import org.eclipse.che.ide.js.api.action.ActionManager;
import org.eclipse.che.ide.js.api.resources.ImageRegistry;

/** */
@Singleton
@JsType(namespace = JsPackage.GLOBAL, name = "che")
public class JsApi {

  @JsIgnore private static JsApi bootstrap;

  @JsProperty private final ActionManager actionManager;

  @JsProperty private final ImageRegistry imageRegistry;

  @Inject
  @JsIgnore
  public JsApi(ActionManager actionManager, ImageRegistry imageRegistry) {
    this.actionManager = actionManager;
    this.imageRegistry = imageRegistry;
    bootstrap = this;
  }

  @JsMethod(name = "api")
  public static JsApi getBootstrap() {
    return bootstrap;
  }

  @JsIgnore
  public void initApi() {
    //    new Timer() {
    //      @Override
    //      public void run() {
    //        ScriptInjector.fromUrl("/_app/plugins/FirstJsGWTPlugin/FirstJsGWTPlugin.nocache.js")
    //            .inject();
    //        ScriptElement element = Browser.getWindow().getDocument().createScriptElement();
    //        element.setSrc("/_app/plugins/FirstJsGWTPlugin/FirstJsGWTPlugin.nocache.js");
    //        element.setType("text/javascript");
    //        element.setLang("javascript");
    //        Browser.getDocument().getHead().appendChild(element);
    //      }
    //    }.schedule(2000);
  }
}

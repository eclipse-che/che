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
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.eventbus.EventBus;
import org.eclipse.che.ide.js.api.action.ActionManager;
import org.eclipse.che.ide.js.api.editor.EditorManager;
import org.eclipse.che.ide.js.api.parts.PartManager;
import org.eclipse.che.ide.js.api.resources.ImageRegistry;

/** */
@Singleton
@JsType(namespace = JsPackage.GLOBAL, name = "che")
public class JsApi {

  @JsIgnore private static JsApi instance;

  @JsProperty private final ActionManager actionManager;

  @JsProperty private final ImageRegistry imageRegistry;

  @JsProperty private final PartManager partManager;

  @JsProperty private final EditorManager editorManager;

  @JsProperty private final AppContext appContext;

  @JsProperty private final EventBus eventBus;

  @Inject
  @JsIgnore
  public JsApi(
      ActionManager actionManager,
      ImageRegistry imageRegistry,
      PartManager partManager,
      EditorManager editorManager,
      AppContext appContext,
      EventBus eventBus) {
    this.actionManager = actionManager;
    this.imageRegistry = imageRegistry;
    this.partManager = partManager;
    this.editorManager = editorManager;
    this.appContext = appContext;
    this.eventBus = eventBus;
    instance = this;
  }

  @JsMethod(name = "api")
  public static JsApi getApi() {
    return instance;
  }
}

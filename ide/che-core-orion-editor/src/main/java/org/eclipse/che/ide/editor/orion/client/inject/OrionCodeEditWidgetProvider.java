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
package org.eclipse.che.ide.editor.orion.client.inject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.che.ide.editor.orion.client.jso.OrionCodeEditWidgetOverlay;
import org.eclipse.che.requirejs.ModuleHolder;

/**
 * Provider of Orion CodeEdit widget instance.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class OrionCodeEditWidgetProvider implements Provider<OrionCodeEditWidgetOverlay> {

  private final ModuleHolder moduleHolder;
  private final Set<OrionPlugin> orionPlugins;
  private OrionCodeEditWidgetOverlay orionCodeEditWidgetOverlay;

  @Inject
  public OrionCodeEditWidgetProvider(ModuleHolder moduleHolder) {
    this.moduleHolder = moduleHolder;
    orionPlugins = new HashSet<>();
  }

  @Inject(optional = true)
  private void registerPlugins(Set<OrionPlugin> plugins) {
    for (OrionPlugin registrar : plugins) {
      orionPlugins.add(registrar);
    }
  }

  @Override
  public OrionCodeEditWidgetOverlay get() {
    if (orionCodeEditWidgetOverlay == null) {
      JsArrayString plugins = JavaScriptObject.createArray().cast();
      for (OrionPlugin orionPlugin : orionPlugins) {
        plugins.push(GWT.getModuleBaseURL() + orionPlugin.getRelPath());
      }

      OrionCodeEditWidgetOverlay codeEditWidgetModule =
          moduleHolder.getModule("CodeEditWidget").cast();
      orionCodeEditWidgetOverlay = codeEditWidgetModule.create(plugins);
    }
    return orionCodeEditWidgetOverlay;
  }
}

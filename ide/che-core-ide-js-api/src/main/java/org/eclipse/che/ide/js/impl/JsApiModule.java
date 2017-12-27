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

package org.eclipse.che.ide.js.impl;

import com.google.gwt.inject.client.AbstractGinModule;
import org.eclipse.che.ide.js.api.action.ActionManager;
import org.eclipse.che.ide.js.api.dialog.DialogManager;
import org.eclipse.che.ide.js.api.editor.EditorManager;
import org.eclipse.che.ide.js.api.parts.PartManager;
import org.eclipse.che.ide.js.api.resources.ImageRegistry;
import org.eclipse.che.ide.js.impl.action.JsActionManager;
import org.eclipse.che.ide.js.impl.dialog.JsDialogManager;
import org.eclipse.che.ide.js.impl.editor.JsEditorManager;
import org.eclipse.che.ide.js.impl.eventbus.EventBusAdapter;
import org.eclipse.che.ide.js.impl.parts.JsPartManager;
import org.eclipse.che.ide.js.impl.resources.ImageRegistryImpl;

/** */
public class JsApiModule extends AbstractGinModule {

  @Override
  protected void configure() {
    bind(EventBusAdapter.class).asEagerSingleton();
    bind(ActionManager.class).to(JsActionManager.class);
    bind(ImageRegistry.class).to(ImageRegistryImpl.class);
    bind(PartManager.class).to(JsPartManager.class);
    bind(EditorManager.class).to(JsEditorManager.class);
    bind(DialogManager.class).to(JsDialogManager.class);
  }
}

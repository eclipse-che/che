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

package org.eclipse.che.ide.js.plugin.action;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.js.plugin.PluginManager;

/** @author Yevhen Vydolob */
@Singleton
public class DisablePluginsAction extends BaseAction {

  private final PluginManager pluginManager;

  @Inject
  public DisablePluginsAction(PluginManager pluginManager) {
    super("Disable Plugins");
    this.pluginManager = pluginManager;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    pluginManager.disablePlugin();
  }
}

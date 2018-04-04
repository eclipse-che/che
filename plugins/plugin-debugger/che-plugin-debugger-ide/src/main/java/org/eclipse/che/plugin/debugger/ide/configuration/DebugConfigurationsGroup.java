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
package org.eclipse.che.plugin.debugger.ide.configuration;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationsManager;
import org.eclipse.che.ide.api.debug.DebugConfigurationsManager.ConfigurationChangedListener;

/**
 * Group of {@link DebugConfigurationAction}s.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class DebugConfigurationsGroup extends DefaultActionGroup
    implements ConfigurationChangedListener {

  private final DebugConfigurationsManager configurationsManager;
  private final DebugConfigurationActionFactory debugConfigurationActionFactory;

  @Inject
  public DebugConfigurationsGroup(
      ActionManager actionManager,
      DebugConfigurationsManager debugConfigurationsManager,
      DebugConfigurationActionFactory debugConfigurationActionFactory) {
    super(actionManager);
    configurationsManager = debugConfigurationsManager;
    this.debugConfigurationActionFactory = debugConfigurationActionFactory;

    debugConfigurationsManager.addConfigurationsChangedListener(this);

    fillActions();
  }

  @Override
  public void onConfigurationAdded(DebugConfiguration configuration) {
    fillActions();
  }

  @Override
  public void onConfigurationRemoved(DebugConfiguration configuration) {
    fillActions();
  }

  private void fillActions() {
    removeAll();

    for (DebugConfiguration configuration : configurationsManager.getConfigurations()) {
      add(debugConfigurationActionFactory.createAction(configuration));
    }
  }
}

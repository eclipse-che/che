/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.gdb.ide.configuration;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationPage;
import org.eclipse.che.ide.api.debug.DebugConfigurationType;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.plugin.gdb.ide.GdbDebugger;
import org.eclipse.che.plugin.gdb.ide.GdbResources;

/**
 * GDB debug configuration type.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class GdbConfigurationType implements DebugConfigurationType {

  public static final String DISPLAY_NAME = "GDB";

  private final GdbConfigurationPagePresenter page;

  @Inject
  public GdbConfigurationType(
      GdbConfigurationPagePresenter page, IconRegistry iconRegistry, GdbResources resources) {
    this.page = page;
    iconRegistry.registerIcon(
        new Icon(
            GdbDebugger.ID + ".debug.configuration.type.icon",
            resources.gdbDebugConfigurationType()));
  }

  @Override
  public String getId() {
    return GdbDebugger.ID;
  }

  @Override
  public String getDisplayName() {
    return DISPLAY_NAME;
  }

  @Override
  public DebugConfigurationPage<? extends DebugConfiguration> getConfigurationPage() {
    return page;
  }
}

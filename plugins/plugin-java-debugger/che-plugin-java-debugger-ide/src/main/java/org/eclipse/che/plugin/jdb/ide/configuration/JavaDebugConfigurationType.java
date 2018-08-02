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
package org.eclipse.che.plugin.jdb.ide.configuration;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationPage;
import org.eclipse.che.ide.api.debug.DebugConfigurationType;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.plugin.jdb.ide.JavaDebuggerResources;
import org.eclipse.che.plugin.jdb.ide.debug.JavaDebugger;

/**
 * Java debug configuration type.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class JavaDebugConfigurationType implements DebugConfigurationType {

  public static final String DISPLAY_NAME = "Java";

  private final JavaDebugConfigurationPagePresenter page;

  @Inject
  public JavaDebugConfigurationType(
      JavaDebugConfigurationPagePresenter page,
      IconRegistry iconRegistry,
      JavaDebuggerResources resources) {
    this.page = page;
    iconRegistry.registerIcon(
        new Icon(
            JavaDebugger.ID + ".debug.configuration.type.icon",
            resources.javaDebugConfigurationType()));
  }

  @Override
  public String getId() {
    return JavaDebugger.ID;
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

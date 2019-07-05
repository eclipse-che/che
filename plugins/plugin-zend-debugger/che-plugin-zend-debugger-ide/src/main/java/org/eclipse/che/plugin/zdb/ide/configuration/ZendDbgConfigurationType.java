/*
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.zdb.ide.configuration;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationPage;
import org.eclipse.che.ide.api.debug.DebugConfigurationType;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.plugin.zdb.ide.ZendDbgResources;
import org.eclipse.che.plugin.zdb.ide.ZendDebugger;

/**
 * Zend debugger configuration type.
 *
 * @author Bartlomiej Laczkowski
 */
@Singleton
public class ZendDbgConfigurationType implements DebugConfigurationType {

  public static final String DISPLAY_NAME = "PHP";

  public static final String ATTR_BREAK_AT_FIRST_LINE = "break-at-first-line";
  public static final String ATTR_CLIENT_HOST_IP = "client-host-ip";
  public static final String ATTR_DEBUG_PORT = "debug-port";
  public static final String ATTR_USE_SSL_ENCRYPTION = "use-ssl-encryption";

  public static final String DEFAULT_BREAK_AT_FIRST_LINE = "true";
  public static final String DEFAULT_CLIENT_HOST_IP = "localhost";
  public static final String DEFAULT_DEBUG_PORT = "10137";
  public static final String DEFAULT_USE_SSL_ENCRYPTION = "false";

  private final ZendDbgConfigurationPagePresenter page;

  @Inject
  public ZendDbgConfigurationType(
      ZendDbgConfigurationPagePresenter page,
      IconRegistry iconRegistry,
      ZendDbgResources resources) {
    this.page = page;
    iconRegistry.registerIcon(
        new Icon(
            ZendDebugger.ID + ".debug.configuration.type.icon",
            resources.zendDbgConfigurationType()));
  }

  @Override
  public String getId() {
    return ZendDebugger.ID;
  }

  @Override
  public String getDisplayName() {
    return DISPLAY_NAME;
  }

  @Override
  public DebugConfigurationPage<? extends DebugConfiguration> getConfigurationPage() {
    return page;
  }

  static void setDefaults(DebugConfiguration configuration) {
    configuration
        .getConnectionProperties()
        .put(ATTR_BREAK_AT_FIRST_LINE, DEFAULT_BREAK_AT_FIRST_LINE);
    configuration.getConnectionProperties().put(ATTR_CLIENT_HOST_IP, DEFAULT_CLIENT_HOST_IP);
    configuration.getConnectionProperties().put(ATTR_DEBUG_PORT, DEFAULT_DEBUG_PORT);
    configuration
        .getConnectionProperties()
        .put(ATTR_USE_SSL_ENCRYPTION, DEFAULT_USE_SSL_ENCRYPTION);
  }
}
